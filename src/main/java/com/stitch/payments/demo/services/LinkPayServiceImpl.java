package com.stitch.payments.demo.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.stitch.payments.demo.dto.AccessToken;
import com.stitch.payments.demo.model.StitchAuthorizationRequest;
import com.stitch.payments.demo.model.UserToken;
import com.stitch.payments.demo.repository.ClientTokenDao;
import com.stitch.payments.demo.repository.StitchAuthorizationRequestDao;
import com.stitch.payments.demo.repository.UserTokenDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LinkPayServiceImpl extends BaseService implements LinkPayService {
	@Autowired
	ClientTokenDao clientTokenDao;

	Base64StringKeyGenerator keyGenerator = new Base64StringKeyGenerator();

	@Value("${stitch.client-id}")
	private String clientId;

	@Value("${stitch.response_type}")
	private String responseType;

	@Value("${stitch.linkpay-redirect-uri}")
	private String linkpayRedirectUri;

	@Value("${spring.security.oauth2.client.registration.stitch.scope}")
	private String scope;

	@Value("${stitch.token-grant-type}")
	private String tokenGrantType;

	@Value("${spring.security.oauth2.client.provider.stitch.token-uri}")
	private String tokenUri;

	@Value("${stitch.client_assertion_type}")
	private String clientAssertionType;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private StitchAuthorizationRequestDao stitchAuthorizationRequestDao;
	@Autowired
	private CryptoUtils cryptoUtils;

	public LinkPayServiceImpl(RestTemplate restTemplate, UserTokenDao userTokenDao) {
		super(restTemplate, userTokenDao);
	}

	private String clientToken() {
		return clientTokenDao.findFirstByOrderByIdDesc().get().getAccessToken();
	}
	
	private String getAccessToken() {
		return userTokenDao.findFirstByOrderByIdDesc().get().getAccessToken();
	}

	@Override
	public String linkAccountRequest() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("accountName", "Sample Account");
		variables.put("bankId", "absa");
		variables.put("accountNumber", "1234567890");
		variables.put("beneficiaryReference", UUID.randomUUID().toString());
		variables.put("payerEmail", "sampleuser@example.com");
		variables.put("payerName", "Sample User");
		variables.put("payerReference", UUID.randomUUID());
		variables.put("payerPhoneNumber", "27821234567");

		var query = "mutation CreateAccountLinkingRequest {\n"
				+ "  clientPaymentAuthorizationRequestCreate(input: {\n"
				+ "    beneficiary: {\n"
				+ "      bankAccount: {\n"
				+ "        name: \"Sample Account\", \n"
				+ "        bankId: absa, \n"
				+ "        accountNumber: \"1234567890\", \n"
				+ "        accountType: current, \n"
				+ "        beneficiaryType: private, \n"
				+ "        reference: \"TestBeneficiary\"\n"
				+ "      }\n"
				+ "    }, payer: {        \n"
				+ "      email: \"sampleuser@example.com\",       \n"
				+ "      name: \"Sample User\", \n"
				+ "      reference: \"TestPayer\",\n"
				+ "      phoneNumber: \"27821234567\"\n"
				+ "  }}) {\n"
				+ "    authorizationRequestUrl\n"
				+ "  }\n"
				+ "}";

		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(query, variables,
				"CreateAccountLinkingRequest");
		log.info("CreateAccountLinkingRequest {}", graphQLResponse.getData());

		Map<String, Object> authData = graphQLResponse.getData();
		Map<String, Object> authDataRequest = (Map<String, Object>) authData
				.get("clientPaymentAuthorizationRequestCreate");
		String authorizationRequestUrl = (String) authDataRequest.get("authorizationRequestUrl");

		log.info("CreateAccountLinkingRequest {}", graphQLResponse.getJson());
		return buildAuthorizationUri(authorizationRequestUrl);
	}

	@Override
	public Map<String, Object> linkedAccountInfo() {
		var query="query GetLinkedAccountAndIdentityInfo {  \n"
				+ "  user {\n"
				+ "    paymentAuthorization {\n"
				+ "      bankAccount {\n"
				+ "        id\n"
				+ "        name\n"
				+ "        accountNumber\n"
				+ "        accountType\n"
				+ "        bankId\n"
				+ "        accountHolder {\n"
				+ "          __typename\n"
				+ "          ... on Individual {\n"
				+ "            fullName\n"
				+ "            identifyingDocument {\n"
				+ "              ... on IdentityDocument {\n"
				+ "                __typename\n"
				+ "                country\n"
				+ "                number\n"
				+ "              }\n"
				+ "              ... on Passport {\n"
				+ "                __typename\n"
				+ "                country\n"
				+ "                number\n"
				+ "              }\n"
				+ "            }\n"
				+ "          }\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}\n"
				+ "";
		GraphQLResponse graphQLResponse = client(getAccessToken()).executeQuery(query, Collections.emptyMap(),
				"GetLinkedAccountAndIdentityInfo");
		
		log.info("GetLinkedAccountAndIdentityInfo {}", graphQLResponse.getData());

		return  graphQLResponse.getData();
	}

	@Override
	public Map<String, Object> initiatePayment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> cancelPaymentInitiation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> webhookSubscription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> viewInitiatedPayments() {
		// TODO Auto-generated method stub
		return null;
	}

	private String buildAuthorizationUri(String authorizationRequestUrl) {
		final String codeVerifier = PemUtils.generateCodeVerifier();

		final String codeChallenge = PemUtils.generateCodeChallenge(codeVerifier);

		final String state = Base64.getEncoder().encodeToString(keyGenerator.generateKey().getBytes());

		final String nonce = PemUtils.nonceGenerator();
		MultiValueMap<String, String> arg = new LinkedMultiValueMap<>();
		arg.add("client_id", clientId);
		arg.add("scope", scope.replaceAll(",", " "));
		arg.add("response_type", responseType);
		arg.add("redirect_uri", linkpayRedirectUri);
		arg.add("state", state);
		arg.add("nonce", nonce);
		arg.add("code_challenge", codeChallenge);
		arg.add("code_challenge_method", PemUtils.CODE_CHALLENGE_METHOD);

		String linkedAccountUrl = UriComponentsBuilder.fromUriString(authorizationRequestUrl).queryParams(arg).build()
				.toUriString();

		saveStitchAuthorizationRequest(codeChallenge, codeVerifier, state);

		return linkedAccountUrl;

	}

	@Override
	public AccessToken generateUserToken(String code, String state) throws IOException {
		// Create a RestTemplate to describe the request
		// Specify the http headers that we want to attach to the request
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		log.info("Generate token using state>>>>>>>>> ",state);
		var lastAuthorizationRequestOp = stitchAuthorizationRequestDao.findByStitchState(state);
		if (!lastAuthorizationRequestOp.isPresent()) {
			throw new IllegalStateException("Code verifier not found");
		}

		// Create a map of the key/value pairs that we want to supply in the body of the
		// request
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", tokenGrantType);
		map.add("client_id", clientId);
		map.add("code", code);
		map.add("redirect_uri", linkpayRedirectUri);
		map.add("code_verifier", lastAuthorizationRequestOp.get().getCodeVerifier());
		map.add("client_assertion_type", clientAssertionType);
		map.add("client_assertion", cryptoUtils.generatePrivateKeyJwt());

		// Create an HttpEntity object, wrapping the body and headers of the request
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		// Execute the request, as a POSt, and expecting a TokenResponse object in
		// return
		ResponseEntity<AccessToken> response = restTemplate.exchange(tokenUri, HttpMethod.POST, entity,
				AccessToken.class);
		log.info("Received token response>>>>>>>> {}", response.getBody());
		AccessToken accessToken = response.getBody();
		var userToken = new UserToken();
		userToken.setAccessToken(accessToken.getAccessToken());
		userToken.setRefreshToken(accessToken.getRefreshToken());

		userTokenDao.save(userToken);
		return accessToken;
	}

	private void saveStitchAuthorizationRequest(String codeChallenge, String codeVerifier, String state) {
		var authRequest = new StitchAuthorizationRequest();
		authRequest.setCodeChallenge(codeChallenge);
		authRequest.setCodeVerifier(codeVerifier);
		authRequest.setStitchState(state);
		authRequest.setUserId(UUID.randomUUID());
		stitchAuthorizationRequestDao.save(authRequest);
	}

}