package com.app.webnest.service;

import com.app.webnest.domain.dto.GameRoomDTO;

import java.util.List;

public interface GameRoomService {
    public List<GameRoomDTO> getRooms();
    
    public List<GameRoomDTO> getRooms(Long userId);

    public GameRoomDTO getRoom(Long id);
}
