package com.stitch.payments.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ClientToken {
	@JsonProperty("access_token")
	String accessToken;
	@JsonProperty("expires_in")
	Long expiresIn;
	@JsonProperty("token_type")
	String tokenType;

	String scope;
}
