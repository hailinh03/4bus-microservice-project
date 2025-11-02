package com.mss.project.user_service.service.impl;

import com.mss.project.user_service.dto.request.AuthenticationRequest;
import com.mss.project.user_service.dto.request.ExchangeTokenRequest;
import com.mss.project.user_service.dto.request.NotificationRequest;
import com.mss.project.user_service.dto.response.AuthenticationResponse;
import com.mss.project.user_service.dto.response.VerifyTokenResponse;
import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.enums.Role;
import com.mss.project.user_service.repository.UserRepository;
import com.mss.project.user_service.service.GoogleIdentityService;
import com.mss.project.user_service.service.GoogleUserService;
import com.mss.project.user_service.service.IAuthenticationService;
import com.mss.project.user_service.service.INotificationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final GoogleIdentityService googleIdentityService;
    private final GoogleUserService googleUserService;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String secretKey;
    @NonFinal
    @Value("${jwt.validDuration}")
    protected Long duration;
    @NonFinal
    @Value("${google.identity.clientId}")
    protected String CLIENT_ID;
    @NonFinal
    @Value("${google.identity.clientSecret}")
    protected String CLIENT_SECRET;
    @NonFinal
    @Value("${google.identity.redirectUri}")
    protected String REDIRECT_URI;
    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";
    private final INotificationService notificationService;
    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.emailExchange}")
    private String emailExchangeName;
    @Value("${rabbitmq.routing.emailKey}")
    private String emailRoutingKey;

    @Override
    public VerifyTokenResponse verifyToken(String token) {
        boolean isValid = true;
        try {
            SignedJWT signedJWT = verifyAuthToken(token);
        }catch (Exception e) {
            isValid = false;
        }
        return VerifyTokenResponse.builder()
                .valid(isValid)
                .build();
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {

        User authUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập không đúng"));
        if (!passwordEncoder.matches(request.getPassword(), authUser.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng");
        }else if (!authUser.isActive()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa");
        } else if (authUser.isDeleted()) {
            throw new RuntimeException("Tài khoản của bạn đã bị xóa");
        }else if (authUser.isGoogleAccount()){
            throw new RuntimeException("Bạn đã đăng nhập bằng tài khoản Google, vui lòng sử dụng tính năng đăng nhập bằng Google");
        }

        String authToken = generateAuthToken(authUser);

        return AuthenticationResponse.builder()
                .token(authToken)
                .build();
    }

    @Transactional
    @Override
    public AuthenticationResponse loginWithGoogle(String code) {

        var response = googleIdentityService.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());
        var userInfo = googleUserService.getUserInfo("json", response.getAccessToken());

        var user = userRepository.findByUsername(userInfo.getEmail()).orElseGet(
                () -> {
                    User newUser = new User();
                    newUser.setFirstName(userInfo.getGivenName());
                    newUser.setLastName(userInfo.getFamilyName());
                    newUser.setEmail(userInfo.getEmail());
                    newUser.setUsername(userInfo.getEmail());
                    newUser.setPassword(passwordEncoder.encode(String.valueOf(new Date().getTime())));
                    newUser.setRole(Role.PASSENGER);
                    newUser.setAvatarUrl(userInfo.getPicture());
                    newUser.setGoogleAccount(true);
                    User savedUser = userRepository.save(newUser);

                    NotificationRequest request = new NotificationRequest();
                    request.setTitle("Chào mừng bạn đến với 4Bus");
                    request.setContent("Vui lòng cập nhật thông tin cá nhân của bạn để có trải nghiệm tốt nhất");
                    request.setUrl("/profile");
                    request.setUserId(savedUser.getId());

                    rabbitTemplate.convertAndSend(emailExchangeName, emailRoutingKey, newUser.getEmail());
                    notificationService.saveNotification(request);

                    return savedUser;
                }
        );

        if(!user.isActive()){
            throw new RuntimeException("Tài khoản của bạn đã bị khóa");
        }else if(user.isDeleted()){
            throw new RuntimeException("Tài khoản của bạn đã bị xóa");
        }else if(!user.isGoogleAccount()){
            throw new RuntimeException("Ban đã đăng nhập bằng tài khoản thường, vui lòng sử dụng tính năng đăng nhập bằng tài khoản thường");
        }

        String authToken = generateAuthToken(user);

        return AuthenticationResponse.builder()
                .token(authToken)
                .build();
    }

    private String generateAuthToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        User thisUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(thisUser.getId()))
                .issuer("4bus")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(duration, ChronoUnit.DAYS).toEpochMilli()))
                .claim("username", thisUser.getUsername())
                .claim("scope", thisUser.getRole())
                .jwtID(String.valueOf(UUID.randomUUID()))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        }catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyAuthToken(String token) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier = new MACVerifier(secretKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(jwsVerifier);
        if(!(verified && expTime.after(new Date()))){
            throw new JOSEException("Invalid token");
        }
        return signedJWT;
    }
}
