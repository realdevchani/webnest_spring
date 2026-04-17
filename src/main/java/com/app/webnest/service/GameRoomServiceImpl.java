package com.app.webnest.service;

import com.app.webnest.domain.dto.FollowDTO;
import com.app.webnest.domain.dto.GameRoomDTO;
import com.app.webnest.domain.vo.GameRoomVO;
import com.app.webnest.domain.vo.GameJoinVO;
import com.app.webnest.exception.GameJoinException;
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
        List<GameRoomDTO> rooms = gameRoomDAO.getRooms();
        rooms.stream().forEach(room -> {
            room.setPlayers(gameJoinDAO.getPlayers(room.getId()));
        });
        return rooms;
    }
    
    // 게임방 목록 (userId 포함 - 팔로워 정보 포함)
    @Override
    public List<GameRoomDTO> getRooms(Long userId) {
        List<GameRoomDTO> rooms = gameRoomDAO.getRooms();
        rooms.forEach(room -> room.setPlayers(gameJoinDAO.getPlayers(room.getId())));

        if (userId != null) {
            List<FollowDTO> followers = followDAO.findFollowersByUserId(userId);
            rooms.forEach(room -> room.setFollowers(followers));
        }

        return rooms;
    }

    // 게임방
    @Override
    public GameRoomDTO getRoom(Long id) {
        GameRoomDTO room = gameRoomDAO.getRoom(id).orElseThrow(() -> new GameJoinException("게임방 조회 중 오류 발생"));
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
        // 게임방 생성
        gameRoomDAO.save(gameRoomVO);
        Long createdRoomId = gameRoomVO.getId();
        if (createdRoomId == null) {
            throw new GameJoinException("게임방 생성에 실패했습니다. ID가 생성되지 않았습니다.");
        }
        // 호스트를 게임방에 추가
        if (hostUserId == null) {
            throw new GameJoinException("호스트 유저 ID가 없습니다.");
        }

        GameJoinVO hostJoinVO = new GameJoinVO();
        hostJoinVO.setUserId(hostUserId);
        hostJoinVO.setGameRoomId(createdRoomId);
        hostJoinVO.setGameJoinIsHost(1); // 호스트로 설정
        hostJoinVO.setGameJoinTeamcolor(null); // 팀 컬러는 나중에 설정 가능


        gameJoinDAO.save(hostJoinVO);

        // 같은 트랜잭션 내에서 생성된 게임방 조회
        GameRoomDTO room = gameRoomDAO.getRoom(createdRoomId).orElseThrow(() -> new GameJoinException("게임방 조회 중 오류 발생"));

        room.setPlayers(gameJoinDAO.getPlayers(createdRoomId));
        return room;
    }
    
    // 게임방 삭제 (관련 자식 레코드 먼저 삭제)
    @Override
    public void delete(Long id) {
        chatMessageDAO.deleteByGameRoomId(id);
        gameJoinDAO.deleteAllByGameRoomId(id);
        gameRoomDAO.delete(id);
    }
    
    // 게임방 수정
    @Override
    public void update(GameRoomVO gameRoomVO) {
        gameRoomDAO.update(gameRoomVO);
    }
}
