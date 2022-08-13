package com.stitch.payments.demo.api;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stitch.payments.demo.dto.AccessToken;
import com.stitch.payments.demo.services.LinkPayService;
import com.stitch.payments.demo.services.PemUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LinkPayController {
	
	private final LinkPayService linkPayService;
	@Value("${stitch.webhook-secret}")
	private String webhookSecret;
	
	ObjectMapper objectMapper= new ObjectMapper();
	
	@GetMapping("/linkpay-accounts")
	public void getUserAccounts(HttpServletResponse httpServletResponse) {
		var url=linkPayService.linkAccountRequest();
		httpServletResponse.setHeader("Location", url);
		httpServletResponse.setStatus(302);
	}
	//authorization is redirected here
	@GetMapping("/linkpay-authorizations")
	public AccessToken getUserAccounts(@RequestParam("code") String code,@RequestParam("state") String state) throws IOException {
		return linkPayService.generateUserToken(code, state);
	}
	
	@GetMapping("/linkpay-account")
	public Map<String,Object> linkedAccountInfo(){
		return linkPayService.linkedAccountInfo();
	}
	
	@GetMapping("/linkpay-payments")
	public Map<String,Object> viewInitiatedPayments(){
		return linkPayService.viewInitiatedPayments();
	}
	
	@GetMapping("/linkpay-user-payments")
	public Map<String,Object> initiatePayment(HttpServletResponse httpServletResponse){
		var response= linkPayService.initiatePayment();
		if(response.containsKey("USER_INTERACTION_REQUIRED")) {
			httpServletResponse.setHeader("Location", response.get("USER_INTERACTION_REQUIRED").toString());
			httpServletResponse.setStatus(302);
			return Collections.emptyMap();
		}
		return response;
	}
	
	@GetMapping("/linkpay-user-interactions")
	public Map<String,Object> userInteraction(@RequestParam("id") String id,@RequestParam("status") String status,@RequestParam("externalReference") String externalReference){
		
		var map= new HashMap<String,Object>();
		map.put("id", id);
		map.put("status", status);
		map.put("externalReference", externalReference);
		
		
		return map;
	}
	
	@GetMapping("/linkpay-subscriptions")
	public Map<String, Object> LinkPaySubscription() {
		return linkPayService.webhookSubscription();
	}
	
	@PostMapping("/linkpay-notifications")
	public String receiveSignedWebhook(
			@RequestHeader(name = "X-Stitch-Signature") Map<String, Object> signature,
			@RequestBody String payload)
			throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException {
		String lineSeparator = System.getProperty("line.separator");
		log.info("Start LinkPay Webhook Payload  {}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);

		log.info("X-Stitch-Signature {}", objectMapper.writeValueAsString(signature));
		log.info("Payload {}", payload);

		log.info("End LinkPay Webhook Payload  {}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);

		//sample x-stitch-signature value
		//t=1660208022,hmac_sha256=f86b879619af00d2aed11e656325412755a660adf7517b8c363ca7f7e33998f6
		String receivedSignatureMap = (String) signature.get("x-stitch-signature");
		var receivedSignatureMapArray = receivedSignatureMap.split(",");
		String receivedSignatureTime = receivedSignatureMapArray[0].split("=")[1];
		String receivedSignature = receivedSignatureMapArray[1].split("=")[1];
		
		var hashComputeInput=receivedSignatureTime+"."+payload;

		String expectedSignature = PemUtils.hmacSha256(hashComputeInput, webhookSecret);

		log.info("receivedSignatureTime {}", receivedSignatureTime);
		log.info("receivedSignature {}", receivedSignature);
		log.info("expectedSignature {}", expectedSignature);

		if (!receivedSignature.equals(expectedSignature)) {
			throw new IllegalStateException("Signature failed to match");
		}

		return payload;

	}

}
