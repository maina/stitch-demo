package com.stitch.payments.demo.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
	String grantType;
	String clientId;
	String refreshToken;
	String clientAssertionType;
	String clientAssertion;
}
