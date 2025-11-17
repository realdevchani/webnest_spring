package com.app.webnest.service;

import com.app.webnest.domain.dto.LastWordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LastWordServiceImpl implements LastWordService {
    
    private final SimpMessagingTemplate simpMessagingTemplate;
    
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
            log.info("첫 단어입니다. currentWord: {}", currentWord);
            return true;
        }
        
        // 이전 단어의 마지막 글자
        String lastChar = previousWord.substring(previousWord.length() - 1);
        
        // 현재 단어의 첫 글자
        String firstChar = currentWord.substring(0, 1);
        
        // 끝말잇기 체크 (마지막 글자와 첫 글자가 같은지)
        boolean isChained = lastChar.equals(firstChar);
        
        log.info("끝말잇기 체크 - previousWord: {}, currentWord: {}, lastChar: {}, firstChar: {}, isChained: {}", 
                previousWord, currentWord, lastChar, firstChar, isChained);
        
        return isChained;
    }

    @Override
    public boolean isRealWord(LastWordDTO lastWordDTO) {
        String word = lastWordDTO.getWord();
        
        // 기본 검증: 한글 2글자 이상 또는 영문 3글자 이상
        // 한글 검증
        if (word.matches("^[가-힣]+$")) {
            boolean isValid = word.length() >= 2;
            log.info("한글 단어 검증 - word: {}, length: {}, isValid: {}", word, word.length(), isValid);
            return isValid;
        }
        
        // 영문 검증
        if (word.matches("^[a-zA-Z]+$")) {
            boolean isValid = word.length() >= 3;
            log.info("영문 단어 검증 - word: {}, length: {}, isValid: {}", word, word.length(), isValid);
            return isValid;
        }
        
        // 한글과 영문 혼합 또는 기타 문자
        log.warn("유효하지 않은 단어 형식 - word: {}", word);
        return false;
    }

    @Override
    public boolean isDuplicated(LastWordDTO lastWordDTO) {
        Long gameRoomId = lastWordDTO.getGameRoomId();
        String word = lastWordDTO.getWord();
        
        if (gameRoomId == null) {
            log.warn("게임방 ID가 없습니다.");
            return false;
        }
        
        // 정규화: 한글은 그대로, 영문은 소문자로 변환
        String normalizedWord = word;
        if (word.matches("^[a-zA-Z]+$")) {
            normalizedWord = word.toLowerCase();
        }
        
        // 해당 게임방의 사용된 단어 목록 가져오기
        List<String> usedWords = usedWordsByRoom.getOrDefault(gameRoomId, new ArrayList<>());
        
        // 중복 체크
        boolean isDuplicated = usedWords.contains(normalizedWord);
        
        log.info("중복 단어 체크 - gameRoomId: {}, word: {}, normalizedWord: {}, usedWords: {}, isDuplicated: {}", 
                gameRoomId, word, normalizedWord, usedWords, isDuplicated);
        
        return isDuplicated;
    }
    
    @Override
    public boolean validateWord(LastWordDTO lastWordDTO) {
        log.info("단어 검증 시작 - word: {}, gameRoomId: {}", 
                lastWordDTO.getWord(), lastWordDTO.getGameRoomId());
        
        // 1. 실제 단어인지 확인
        if (!isRealWord(lastWordDTO)) {
            log.warn("유효하지 않은 단어입니다. word: {}", lastWordDTO.getWord());
            return false;
        }
        
        // 2. 중복 단어인지 확인
        if (isDuplicated(lastWordDTO)) {
            log.warn("이미 사용된 단어입니다. word: {}", lastWordDTO.getWord());
            return false;
        }
        
        // 3. 끝말잇기 체인 확인
        if (!isChain(lastWordDTO)) {
            log.warn("끝말잇기가 이어지지 않습니다. previousWord: {}, currentWord: {}", 
                    lastWordDTO.getPreviousWord(), lastWordDTO.getWord());
            return false;
        }
        
        log.info("단어 검증 성공 - word: {}", lastWordDTO.getWord());
        return true;
    }
    
    @Override
    public void broadcastWord(LastWordDTO lastWordDTO) {
        Long gameRoomId = lastWordDTO.getGameRoomId();
        String word = lastWordDTO.getWord();
        
        if (gameRoomId == null) {
            log.error("게임방 ID가 없어 브로드캐스트를 할 수 없습니다.");
            return;
        }
        
        // 게임방의 이전 단어 가져오기 (없으면 null)
        String previousWord = lastWordByRoom.get(gameRoomId);
        lastWordDTO.setPreviousWord(previousWord);
        
        // 단어 검증
        if (!validateWord(lastWordDTO)) {
            log.warn("단어 검증 실패 - 브로드캐스트하지 않습니다. word: {}, previousWord: {}", word, previousWord);
            
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
            return;
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
        
        // 성공 응답 메시지 구성
        Map<String, Object> response = new HashMap<>();
        response.put("type", "WORD_SUBMITTED");
        response.put("word", lastWordDTO.getWord());  // 원본 단어 (대소문자 유지)
        response.put("explanation", lastWordDTO.getExplanation());
        response.put("color", lastWordDTO.getColor());
        response.put("isFocus", lastWordDTO.isFocus());
        response.put("previousWord", previousWord);
        response.put("gameRoomId", gameRoomId);
        
        // 브로드캐스트
        simpMessagingTemplate.convertAndSend(
                "/sub/game/last-word/room/" + gameRoomId,
                response
        );
        
        log.info("단어 브로드캐스트 완료 - gameRoomId: {}, word: {}, previousWord: {}", 
                gameRoomId, normalizedWord, previousWord);
    }
    
    // 게임 방별 사용된 단어 목록 초기화 (게임 종료 시 사용)
    public void clearUsedWords(Long gameRoomId) {
        if (gameRoomId != null) {
            usedWordsByRoom.remove(gameRoomId);
            lastWordByRoom.remove(gameRoomId);
            log.info("게임방의 사용된 단어 목록 초기화 - gameRoomId: {}", gameRoomId);
        }
    }
}
