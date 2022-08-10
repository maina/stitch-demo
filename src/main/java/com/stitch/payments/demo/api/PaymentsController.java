package com.stitch.payments.demo.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitch.payments.demo.services.PaymentsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PaymentsController {
	private final PaymentsService paymentsService;
	@Value("${stitch.initiate-payment-redirect-uri}")
	private String redirectUri;

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

}
