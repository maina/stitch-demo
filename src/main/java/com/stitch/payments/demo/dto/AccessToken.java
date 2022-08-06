package com.stitch.payments.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AccessToken {
	@JsonProperty("id_token")
	String idToken;
	@JsonProperty("expires_in")
	Long expiresIn;
	@JsonProperty("access_token")
	String accessToken;
	@JsonProperty("refresh_token")
	String refreshToken;
	@JsonProperty("token_type")
	String tokenType;
	String scope;
}
