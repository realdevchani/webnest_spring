package com.app.webnest.service;

import com.app.webnest.domain.dto.GameJoinDTO;
import com.app.webnest.domain.dto.LastWordDTO;
import com.app.webnest.exception.LastWordException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LastWordServiceImpl implements LastWordService {
    
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GameJoinService gameJoinService;
    
    // 게임방별 사용된 단어 목록 저장 (게임방 ID -> 단어 목록)
    private final Map<Long, List<String>> usedWordsByRoom = new ConcurrentHashMap<>();
    
    // 게임방별 마지막 단어 저장 (게임방 ID -> 마지막 단어)
    private final Map<Long, String> lastWordByRoom = new ConcurrentHashMap<>();
    
    @Override
    public boolean isChain(LastWordDTO lastWordDTO) {
        String currentWord = lastWordDTO.getWord();
        String previousWord = lastWordDTO.getPreviousWord();
        
        // 첫 단어인 경우 (이전 단어가 없으면 true)
        if (previousWord == null || previousWord.isEmpty()) {
            return true;
        }
        
        // 이전 단어의 마지막 글자
        String lastChar = previousWord.substring(previousWord.length() - 1);
        
        // 현재 단어의 첫 글자
        String firstChar = currentWord.substring(0, 1);
        
        // 끝말잇기 체크 (마지막 글자와 첫 글자가 같은지)
        return lastChar.equals(firstChar);
    }

    @Override
    public boolean isRealWord(LastWordDTO lastWordDTO) {
        String word = lastWordDTO.getWord();
        
        // 기본 검증: 한글 2글자 이상 또는 영문 3글자 이상

        // 한글 검증
        if (word.matches("^[가-힣]+$")) {
            boolean isValid = word.length() >= 2;
            return isValid;
        }
        
        // 영문 검증
        if (word.matches("^[a-zA-Z]+$")) {
            boolean isValid = word.length() >= 3;
            return isValid;
        }
        
        // 한글과 영문 혼합 또는 기타 문자
        throw new LastWordException("유효하지 않은 단어 형식 - word");
    }

    @Override
    public boolean isDuplicated(LastWordDTO lastWordDTO) {
        Long gameRoomId = lastWordDTO.getGameRoomId();
        String word = lastWordDTO.getWord();
        
        if (gameRoomId == null) {
            throw new LastWordException("게임방 ID가 없습니다.");
        }
        
        // 정규화: 한글은 그대로, 영문은 소문자로 변환
        String normalizedWord = word;
        if (word.matches("^[a-zA-Z]+$")) {
            normalizedWord = word.toLowerCase();
        }
        
        // 해당 게임방의 사용된 단어 목록 가져오기
        List<String> usedWords = usedWordsByRoom.getOrDefault(gameRoomId, new ArrayList<>());
        
        // 중복 체크
        return usedWords.contains(normalizedWord);
    }
    
    @Override
    public boolean validateWord(LastWordDTO lastWordDTO) {
        // 1. 실제 단어인지 확인
        if (!isRealWord(lastWordDTO)) {
            throw new LastWordException("유효하지 않은 단어입니다.");
        }
        
        // 2. 중복 단어인지 확인
        if (isDuplicated(lastWordDTO)) {
            throw new LastWordException("이미 사용된 단어 입니다.");
        }
        
        // 3. 끝말잇기 체인 확인
        if (!isChain(lastWordDTO)) {
            throw new LastWordException("끝말잇기가 이어지지 않습니다. previousWord: "+lastWordDTO.getPreviousWord()+", currentWord: "+lastWordDTO.getWord());
        }
        return true;
    }
    
    @Override
    public void broadcastWord(LastWordDTO lastWordDTO, Long gameRoomId) {
        String word = lastWordDTO.getWord();
        Long userId = lastWordDTO.getUserId();
        
        if (gameRoomId == null) {
            throw new LastWordException("게임방 ID가 없어 브로드캐스트를 할 수 없습니다.");
        }
        
        // 게임방의 이전 단어 가져오기 (없으면 null)
        String previousWord = lastWordByRoom.get(gameRoomId);
        lastWordDTO.setPreviousWord(previousWord);
        
        // 단어 검증
        if (!validateWord(lastWordDTO)) {
            // 에러 메시지 브로드캐스트
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "WORD_VALIDATION_FAILED");
            errorResponse.put("message", "유효하지 않은 단어입니다.");
            errorResponse.put("word", word);
            errorResponse.put("previousWord", previousWord);
            
            simpMessagingTemplate.convertAndSend(
                    "/sub/game/last-word/room/" + gameRoomId,
                    errorResponse
            );
            throw new LastWordException("단어 검증 실패 - 브로드캐스트하지 않습니다. word: "+word+", previousWord: "+previousWord);
        }
        
        // 단어 정규화: 한글은 그대로, 영문은 소문자로 변환
        String normalizedWord = word;
        if (word.matches("^[a-zA-Z]+$")) {
            normalizedWord = word.toLowerCase();
        }
        
        // 사용된 단어 목록에 추가
        usedWordsByRoom.computeIfAbsent(gameRoomId, k -> new ArrayList<>()).add(normalizedWord);
        
        // 마지막 단어 업데이트
        lastWordByRoom.put(gameRoomId, normalizedWord);
        
        // 제출자 정보 조회 (색상 정보 포함)
        String userColor = null;
        String userName = null;
        List<GameJoinDTO> gameState = gameJoinService.getArrangeUserByTurn(gameRoomId);
        
        if (userId != null) {
            // 게임 상태에서 제출자 정보 찾기
            Optional<GameJoinDTO> submittingPlayer = gameState.stream()
                    .filter(p -> p.getUserId().equals(userId))
                    .findFirst();
            
            if (submittingPlayer.isPresent()) {
                GameJoinDTO player = submittingPlayer.get();
                userColor = player.getGameJoinTeamcolor();
                userName = player.getUserNickname();
            } else {
                throw new LastWordException("제출자를 게임 상태에서 찾을 수 없습니다. userId: " + userId);
            }
        }
        
        // 성공 응답 메시지 구성
        Map<String, Object> response = new HashMap<>();
        response.put("type", "WORD_SUBMITTED");
        response.put("word", lastWordDTO.getWord());  // 원본 단어 (대소문자 유지)
        response.put("explanation", lastWordDTO.getExplanation());  //추후 llm으로 설명 추가
        response.put("color", lastWordDTO.getColor());
        response.put("isFocus", lastWordDTO.isFocus());
        response.put("previousWord", previousWord);
        response.put("gameRoomId", gameRoomId);
        
        // 제출자 정보 추가 (프론트엔드에서 색상 결정을 위해 필수)
        if (userId != null) {
            response.put("userId", userId);
            response.put("submittingUserId", userId);
        }
        if (userColor != null) {
            response.put("userColor", userColor);
        }
        if (userName != null) {
            response.put("userName", userName);
        }

        response.put("gameState", gameState);
        
        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameRoomId,
                response
        );
    }
    
    // 게임 방별 사용된 단어 목록 초기화 (게임 종료 시 사용)
    public void clearUsedWords(Long gameRoomId) {
        if (gameRoomId != null) {
            usedWordsByRoom.remove(gameRoomId);
            lastWordByRoom.remove(gameRoomId);
        }
    }
}
