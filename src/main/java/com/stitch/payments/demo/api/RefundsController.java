package com.stitch.payments.demo.api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stitch.payments.demo.services.PemUtils;
import com.stitch.payments.demo.services.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RefundsController {
	@Value("${stitch.webhook-secret}")
	private String webhookSecret;
	
	ObjectMapper objectMapper= new ObjectMapper();
	
	private final RefundService refundService;
	
	@GetMapping("/refunds")
	public Map<String, Object> initiateRefund(@RequestParam("id") String id) {
		log.info("refund id {}", id);
		return refundService.initiateRefund(id);
	}
	
	@GetMapping("/refunds-status")
	public Map<String, Object> refundStatus(@RequestParam("id") String id) {
		log.info("refund id {}", id);
		return refundService.refundStatus(id);
	}
	
	@GetMapping("/refunds-subscriptions")
	public Map<String, Object> RefundsSubscription() {
		return refundService.refundSignedWebhook();
	}
	
	@PostMapping("/refunds-notifications")
	public String receiveSignedWebhook(
			@RequestHeader(name = "X-Stitch-Signature") Map<String, Object> signature,
			@RequestBody String payload)
			throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException {
		String lineSeparator = System.getProperty("line.separator");
		log.info("Start Refunds Webhook Payload  {}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);

		log.info("X-Stitch-Signature {}", objectMapper.writeValueAsString(signature));
		log.info("Payload {}", payload);

		log.info("End Refunds Webhook Payload  {}", lineSeparator);
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
 