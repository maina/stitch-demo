package com.stitch.payments.demo.services;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Component
public class CryptoUtils {
	@Value("${stitch.client-id:test-0bc5a12d-de6c-4b2f-b4c1-f0bab8246016}")
	private String clientId;
	@Value("${stitch.audience:https://secure.stitch.money/connect/token}")
	private String audience;
	
	public String generatePrivateKeyJwt() throws IOException {
		RSAPublicKey publicKey = (RSAPublicKey) PemUtils.readPublicKeyFromFile("certificate.public", "RSA");
		RSAPrivateKey privateKey = (RSAPrivateKey) PemUtils.readPrivateKeyFromFile("certificate.private", "RSA");

		Algorithm algorithmRS = Algorithm.RSA256(publicKey, privateKey);

		Instant currentInstant = Instant.now();

		String issuer = clientId;
		String subject = clientId;
		String jti = UUID.randomUUID().toString(); // Needs to be a unique value each time
		Date issuedAt = Date.from(currentInstant);
		Date notBefore = issuedAt;
		Date expiresAt = Date.from(currentInstant.plusSeconds(60)); // Should be a small value after now

		return JWT.create().withAudience(audience).withIssuer(issuer).withSubject(subject).withJWTId(jti)
				.withIssuedAt(issuedAt).withNotBefore(notBefore).withExpiresAt(expiresAt).sign(algorithmRS);
	}

}
