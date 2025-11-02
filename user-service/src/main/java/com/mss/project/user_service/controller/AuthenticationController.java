package com.mss.project.user_service.controller;

import com.mss.project.user_service.dto.request.AuthenticationRequest;
import com.mss.project.user_service.dto.request.VerifyTokenRequest;
import com.mss.project.user_service.dto.response.AuthenticationResponse;
import com.mss.project.user_service.dto.response.VerifyTokenResponse;
import com.mss.project.user_service.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest request) {
        return ResponseEntity.ok(authenticationService.verifyToken(request.getToken()));
    }

    @PostMapping("/login-google")
    public ResponseEntity<AuthenticationResponse> loginWithGoogle(@RequestParam("code") String code) {
        return ResponseEntity.ok(authenticationService.loginWithGoogle(code));
    }
}
