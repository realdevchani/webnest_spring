package com.app.webnest.service;

import com.app.webnest.domain.dto.ApiResponseDTO;
import com.app.webnest.util.SmsUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    private final SmsUtil smsUtil;

    @Override
    public ApiResponseDTO sendAuthentificationCodeBySms(String phoneNumber, HttpSession session) {
        // 세션에 랜덤 코드를 생성해서 저장
        String AuthentificationCode = smsUtil.saveAuthentificationCode(session);
        // 메세지 전송
        smsUtil.sendMessage(phoneNumber, AuthentificationCode);
        // 응답
        return ApiResponseDTO.of("메세지 전송 성공");
    }
}
