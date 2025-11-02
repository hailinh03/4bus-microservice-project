package com.mss.project.user_service.service;

import com.mss.project.user_service.dto.request.AuthenticationRequest;
import com.mss.project.user_service.dto.response.AuthenticationResponse;
import com.mss.project.user_service.dto.response.VerifyTokenResponse;

public interface IAuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);
    AuthenticationResponse loginWithGoogle(String code);
    VerifyTokenResponse verifyToken(String token);
}
