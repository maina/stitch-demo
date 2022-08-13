package com.stitch.payments.demo.services;

import java.util.Map;

import com.stitch.payments.demo.dto.InitiatePaymentRequestResponse;

public interface InstantPayService {
	
	InitiatePaymentRequestResponse initiatePaymentRequest();
	Map<String,Object> paymentRequestStatus(String paymentRequestId);
	
	Map<String,Object> instantPaySignedWebhook();

}
