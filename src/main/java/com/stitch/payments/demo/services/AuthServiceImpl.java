package com.stitch.payments.demo.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.stitch.payments.demo.dto.AccessToken;
import com.stitch.payments.demo.dto.ClientToken;
import com.stitch.payments.demo.model.UserToken;
import com.stitch.payments.demo.repository.ClientTokenDao;
import com.stitch.payments.demo.repository.StitchAuthorizationRequestDao;
import com.stitch.payments.demo.repository.UserTokenDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	@Value("${stitch.grant_type}")
	private String grantType;

	@Value("${stitch.token-grant-type}")
	private String tokenGrantType;

	@Value("${stitch.client-id:test-0bc5a12d-de6c-4b2f-b4c1-f0bab8246016}")
	private String clientId;
	@Value("${stitch.client_assertion_type}")
	private String clientAssertionType;
	@Value("${stitch.audience}")
	private String retrieveTokenAudience;
	@Value("${stitch.scope}")
	private String scope;
	@Value("${stitch.base-url}")
	private String baseUrl;

	@Value("${spring.security.oauth2.client.registration.stitch.redirect-uri}")
	private String redirectUri;
	@Value("${spring.security.oauth2.client.provider.stitch.token-uri}")
	private String tokenUri;

	private final RestTemplate restTemplate;
	private final StitchAuthorizationRequestDao stitchAuthorizationRequestDao;
	private final UserTokenDao userTokenDao;
	private final ClientTokenDao clientTokenDao;
	private final CryptoUtils cryptoUtils;

	@Override
	public ClientToken retrieveClientToken() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", clientId);
		map.add("client_assertion_type", clientAssertionType);
		map.add("audience", retrieveTokenAudience);
		map.add("scope", scope);
		map.add("grant_type", grantType);
		map.add("client_assertion", cryptoUtils.generatePrivateKeyJwt());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<ClientToken> response = restTemplate.exchange(baseUrl, HttpMethod.POST, request,
				ClientToken.class);

		var clientTokenResponse = response.getBody();
		var clientToken = new com.stitch.payments.demo.model.ClientToken();
		clientToken.setAccessToken(clientTokenResponse.getAccessToken());
		clientTokenDao.save(clientToken);

		return clientTokenResponse;
	}

	@Override
	public AccessToken generateUserToken(String code, String state) throws IOException {
		// Create a RestTemplate to describe the request
		log.info("TOKEN URI>>>>>>>>>>>>>>>>>>>>>>>> {}", tokenUri);
		// Specify the http headers that we want to attach to the request
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		var lastAuthorizationRequestOp = stitchAuthorizationRequestDao.findByStitchState(state);
		if (!lastAuthorizationRequestOp.isPresent()) {
			throw new IllegalStateException("Authorization Code not found");
		}

		// Create a map of the key/value pairs that we want to supply in the body of the
		// request
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", tokenGrantType);
		map.add("client_id", clientId);
		map.add("code", code);
		map.add("redirect_uri", redirectUri);
		map.add("code_verifier", lastAuthorizationRequestOp.get().getCodeVerifier());
		map.add("client_assertion_type", clientAssertionType);
		map.add("client_assertion", cryptoUtils.generatePrivateKeyJwt());

		// Create an HttpEntity object, wrapping the body and headers of the request
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		// Execute the request, as a POSt, and expecting a TokenResponse object in
		// return
		ResponseEntity<AccessToken> response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity,
				AccessToken.class);
		AccessToken accessToken = response.getBody();
		var userToken = new UserToken();
		userToken.setAccessToken(accessToken.getAccessToken());
		userToken.setRefreshToken(accessToken.getRefreshToken());

		userTokenDao.save(userToken);
		return accessToken;
	}

	@Override
	public AccessToken refreshUserToken() throws IOException {
		// Create a RestTemplate to describe the request
		log.info("TOKEN URI>>>>>>>>>>>>>>>>>>>>>>>> {}", tokenUri);
		// Specify the http headers that we want to attach to the request
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		var lastToken = userTokenDao.findFirstByOrderByIdDesc();
		if (!lastToken.isPresent()) {
			throw new IllegalStateException("Authorization Code not found");
		}

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "refresh_token");
		map.add("client_id", clientId);
		map.add("refresh_token", lastToken.get().getRefreshToken());
		map.add("client_assertion_type", clientAssertionType);
		map.add("client_assertion", cryptoUtils.generatePrivateKeyJwt());

		// Create an HttpEntity object, wrapping the body and headers of the request
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		// Execute the request, as a POSt, and expecting a TokenResponse object in
		// return
		ResponseEntity<AccessToken> response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity,
				AccessToken.class);
		AccessToken accessToken = response.getBody();
		var userToken = userTokenDao.findFirstByOrderByIdDesc().get();
		userToken.setAccessToken(accessToken.getAccessToken());
		userToken.setRefreshToken(accessToken.getRefreshToken());

		userTokenDao.save(userToken);
		return accessToken;
	}

}
