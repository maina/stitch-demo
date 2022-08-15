package com.stitch.payments.demo.services;

import java.util.Map;

public interface RefundService {
	
	Map<String,Object> initiateRefund(String paymentRequestId);
	
	Map<String,Object> refundSignedWebhook();
	
	Map<String,Object> refundStatus(String id);


}
