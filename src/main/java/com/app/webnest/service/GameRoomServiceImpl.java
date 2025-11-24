package com.app.webnest.service;

import com.app.webnest.domain.dto.GameRoomDTO;
import com.app.webnest.domain.vo.GameRoomVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.RoomException;
import com.app.webnest.mapper.GameRoomMapper;
import com.app.webnest.repository.ChatMessageDAO;
import com.app.webnest.repository.FollowDAO;
import com.app.webnest.repository.GameJoinDAO;
import com.app.webnest.repository.GameRoomDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomDAO gameRoomDAO;
    private final GameJoinDAO gameJoinDAO;
    private final FollowDAO followDAO;
    private final ChatMessageDAO chatMessageDAO;

    // 게임방 목록
    @Override
    public List<GameRoomDTO> getRooms() {
        return gameRoomDAO.getRooms().stream().map(dto -> {
                dto.setPlayers(gameJoinDAO.getPlayers(dto.getId()));
                return dto;
        }).collect(Collectors.toList());
    }
    
    // 게임방 목록 (userId 포함 - 팔로워 정보 포함)
    @Override
    public List<GameRoomDTO> getRooms(Long userId) {
        List<GameRoomDTO> rooms = gameRoomDAO.getRooms().stream().map(dto -> {
                dto.setPlayers(gameJoinDAO.getPlayers(dto.getId()));
                return dto;
        }).collect(Collectors.toList());
        
        // 현재 사용자의 팔로워 목록 조회 (나를 팔로우하는 사람들)
        if (userId != null) {
            var followers = followDAO.findFollowersByUserId(userId);
            // 모든 게임방에 동일한 팔로워 목록 설정
            rooms.forEach(room -> room.setFollowers(followers));
        }
        
        return rooms;
    }

    // 게임방
    @Override
    public GameRoomDTO getRoom(Long id) {
        log.info("게임방 조회 시도 - id: {}", id);
        Optional<GameRoomDTO> roomOpt = gameRoomDAO.getRoom(id);
        if (roomOpt.isEmpty()) {
            log.error("게임방을 찾을 수 없습니다 - id: {}", id);
            throw new RoomException("게임방을 찾을 수 없습니다. ID: " + id);
        }
        GameRoomDTO room = roomOpt.get();
        log.info("게임방 조회 성공 - id: {}, title: {}", id, room.getGameRoomTitle());
        room.setPlayers(gameJoinDAO.getPlayers(id));
        return room;
    }
    
    // 게임방 생성
    @Override
    public Long create(GameRoomVO gameRoomVO) {
        gameRoomDAO.save(gameRoomVO);
        return gameRoomVO.getId(); // selectKey로 설정된 ID 반환
    }
    
    // 게임방 생성과 호스트 추가 (한 트랜잭션에서 처리)
    @Override
    public GameRoomDTO createRoomWithHost(GameRoomVO gameRoomVO, Long hostUserId) {
        try {
            // 게임방 생성
            log.info("게임방 생성 시작 - title: {}, hostUserId: {}", gameRoomVO.getGameRoomTitle(), hostUserId);
            gameRoomDAO.save(gameRoomVO);
            Long createdRoomId = gameRoomVO.getId();
            if (createdRoomId == null) {
                log.error("게임방 생성 실패 - ID가 null입니다");
                throw new RuntimeException("게임방 생성에 실패했습니다. ID가 생성되지 않았습니다.");
            }
            log.info("게임방 생성 완료 - roomId: {}", createdRoomId);
            
            // 호스트를 게임방에 추가
            log.info("호스트 추가 시작 - roomId: {}, hostUserId: {}", createdRoomId, hostUserId);
            if (hostUserId == null) {
                log.error("호스트 추가 실패 - hostUserId가 null입니다");
                throw new RuntimeException("호스트 유저 ID가 없습니다.");
            }
            
            GameJoinVO hostJoinVO = new GameJoinVO();
            hostJoinVO.setUserId(hostUserId);
            hostJoinVO.setGameRoomId(createdRoomId);
            hostJoinVO.setGameJoinIsHost(1); // 호스트로 설정
            hostJoinVO.setGameJoinTeamcolor(null); // 팀 컬러는 나중에 설정 가능
            
            log.info("호스트 정보 - userId: {}, gameRoomId: {}, isHost: {}", 
                    hostJoinVO.getUserId(), hostJoinVO.getGameRoomId(), hostJoinVO.getGameJoinIsHost());
            
            gameJoinDAO.save(hostJoinVO);
            log.info("호스트 추가 완료 - roomId: {}, hostUserId: {}", createdRoomId, hostUserId);
            
            // 같은 트랜잭션 내에서 생성된 게임방 조회
            Optional<GameRoomDTO> roomOpt = gameRoomDAO.getRoom(createdRoomId);
            if (roomOpt.isEmpty()) {
                log.error("게임방 생성 후 조회 실패 - roomId: {}", createdRoomId);
                throw new RuntimeException("게임방 생성 후 조회에 실패했습니다. ID: " + createdRoomId);
            }
            GameRoomDTO room = roomOpt.get();
            room.setPlayers(gameJoinDAO.getPlayers(createdRoomId));
            log.info("게임방 생성 및 호스트 추가 완료 - roomId: {}, title: {}, players: {}", 
                    createdRoomId, room.getGameRoomTitle(), room.getPlayers().size());
            return room;
        } catch (Exception e) {
            log.error("게임방 생성 및 호스트 추가 중 오류 발생 - error: {}", e.getMessage(), e);
            throw new RuntimeException("게임방 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    // 게임방 삭제 (관련 자식 레코드 먼저 삭제)
    @Override
    public void delete(Long id) {
        log.info("🗑️ 게임방 삭제 시작 - gameRoomId: {}", id);
        
        try {
            // 1. 채팅 메시지 삭제
            log.info("📨 채팅 메시지 삭제 중 - gameRoomId: {}", id);
            chatMessageDAO.deleteByGameRoomId(id);
            log.info("✅ 채팅 메시지 삭제 완료 - gameRoomId: {}", id);
            
            // 2. 게임 참여 삭제
            log.info("👥 게임 참여 삭제 중 - gameRoomId: {}", id);
            gameJoinDAO.deleteAllByGameRoomId(id);
            log.info("✅ 게임 참여 삭제 완료 - gameRoomId: {}", id);
            
            // 3. 게임방 삭제
            log.info("🏠 게임방 삭제 중 - gameRoomId: {}", id);
            gameRoomDAO.delete(id);
            log.info("✅ 게임방 삭제 완료 - gameRoomId: {}", id);
        } catch (Exception e) {
            log.error("❌ 게임방 삭제 중 오류 발생 - gameRoomId: {}, error: {}", id, e.getMessage(), e);
            throw new RuntimeException("게임방 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    // 게임방 수정
    @Override
    public void update(GameRoomVO gameRoomVO) {
        gameRoomDAO.update(gameRoomVO);
    }
}
