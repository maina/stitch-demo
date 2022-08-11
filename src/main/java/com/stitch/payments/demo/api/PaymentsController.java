package com.stitch.payments.demo.api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import com.stitch.payments.demo.services.PaymentsService;
import com.stitch.payments.demo.services.PemUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {
	private final PaymentsService paymentsService;
	@Value("${stitch.initiate-payment-redirect-uri}")
	private String redirectUri;
	@Value("${stitch.webhook-secret}")
	private String webhookSecret;

	ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping("/payment-requests")
	public void paymentRequest(HttpServletResponse httpServletResponse) {

		var response = paymentsService.initiatePaymentRequest();

		var url = response.getClientPaymentInitiationRequestCreate().getPaymentInitiationRequest().getUrl()
				+ "?redirect_uri=" + redirectUri;
		httpServletResponse.setHeader("Location", url);
		httpServletResponse.setStatus(302);
	}

	@GetMapping("/user-payment-confirmations")
	public Map<String, Object> paymentConfirmation(@RequestParam("id") String id, @RequestParam("status") String status,
			@RequestParam("externalReference") String externalReference) {
		Map<String, Object> response = new HashMap<>();
		response.put("id", id);
		response.put("status", status);
		response.put("externalReference", externalReference);
		return response;
	}

	@GetMapping("/payment-status")
	public Map<String, Object> paymentRequestStatus(@RequestParam("id") String id) {
		return paymentsService.paymentRequestStatus(id);
	}

	@GetMapping("/instantpay-subscriptions")
	public Map<String, Object> instantPaySubscription() {
		return paymentsService.instantPaySignedWebhook();
	}

	@PostMapping("/instantpay-notifications")
	public String receiveSignedWebhook(
			@RequestHeader(name = "X-Stitch-Signature") Map<String, Object> signature,
			@RequestBody String payload)
			throws JsonProcessingException, InvalidKeyException, NoSuchAlgorithmException {
		String lineSeparator = System.getProperty("line.separator");
		log.info("Start InstantPay Webhook Payload  {}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);
		log.info("{}", lineSeparator);

		log.info("X-Stitch-Signature {}", objectMapper.writeValueAsString(signature));
		log.info("Payload {}", payload);

		log.info("End InstantPay Webhook Payload  {}", lineSeparator);
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
