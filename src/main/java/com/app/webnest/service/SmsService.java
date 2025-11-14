package com.app.webnest.service;

import com.app.webnest.domain.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpSession;

public interface SmsService {
    public ApiResponseDTO sendAuthentificationCodeBySms(String phoneNumber, HttpSession session);
}
