package com.app.webnest.service;

import com.app.webnest.domain.vo.ChatMessageVO;

public interface GameRoomLifecycleService {
    void handleJoin(ChatMessageVO chatMessageVO);
    void handleLeave(ChatMessageVO chatMessageVO);
    void handleDisconnect(String sessionId);
    void registerSession(String sessionId, Long userId, Long gameRoomId);
    void unregisterSession(String sessionId);
}
