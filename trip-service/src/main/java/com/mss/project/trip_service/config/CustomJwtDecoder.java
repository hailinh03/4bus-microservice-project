package com.mss.project.trip_service.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if (verifyAuthToken(token)) {
                SignedJWT signedJWT = SignedJWT.parse(token);
                return new Jwt(
                        token,
                        signedJWT.getJWTClaimsSet().getIssueTime().toInstant(),
                        signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(),
                        signedJWT.getHeader().toJSONObject(),
                        signedJWT.getJWTClaimsSet().getClaims()
                );
            }
        } catch (ParseException | JOSEException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token", e.getMessage(), null)
            );
        }
        throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token", "Invalid JWT token", null)
        );
    }

    private boolean verifyAuthToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        boolean verified = signedJWT.verify(verifier);
        Date expTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!verified) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token", "Signature verification failed", null)
            );
        }

        if (expTime == null || expTime.before(new Date())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token", "Token is expired", null)
            );
        }

        return true;
    }
}
