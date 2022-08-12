package com.stitch.payments.demo.services;

import java.io.IOException;
import java.util.Map;

import com.stitch.payments.demo.dto.AccessToken;

public interface LinkPayService {

	String linkAccountRequest();
	AccessToken generateUserToken(String code, String state) throws IOException;
	Map<String,Object> linkedAccountInfo();
	Map<String,Object> initiatePayment();
	Map<String,Object> cancelPaymentInitiation();
	Map<String,Object> webhookSubscription();
	Map<String,Object> viewInitiatedPayments();
}
