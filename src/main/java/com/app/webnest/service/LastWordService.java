package com.app.webnest.service;

import com.app.webnest.domain.dto.LastWordDTO;

public interface LastWordService {
//    받은 문자가 이전의 값과 이어지는지
    public boolean isChain(LastWordDTO lastWordDTO);

//    정말 있는 단어인지
    public boolean isRealWord(LastWordDTO lastWordDTO);

//    중복인 단어인지
    public boolean isDuplicated(LastWordDTO lastWordDTO);

//    끝말잇기 단어 검증 (통합)
    public boolean validateWord(LastWordDTO lastWordDTO);

//    단어를 게임방에 저장하고 브로드캐스트
    public void broadcastWord(LastWordDTO lastWordDTO, Long gameRoomId);
}
