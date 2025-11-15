package com.app.webnest.service;

import com.app.webnest.domain.dto.GameRoomDTO;
import com.app.webnest.exception.RoomException;
import com.app.webnest.mapper.GameRoomMapper;
import com.app.webnest.repository.FollowDAO;
import com.app.webnest.repository.GameJoinDAO;
import com.app.webnest.repository.GameRoomDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomDAO gameRoomDAO;
    private final GameJoinDAO gameJoinDAO;
    private final FollowDAO followDAO;

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
        GameRoomDTO room = gameRoomDAO.getRoom(id).orElseThrow(() -> new RuntimeException("채팅방 조회 오류"));
        room.setPlayers(gameJoinDAO.getPlayers(id));
        return room;
    }
}
