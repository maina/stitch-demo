package com.stitch.payments.demo.services;

import java.io.IOException;

import com.nimbusds.oauth2.sdk.TokenResponse;
import com.stitch.payments.demo.dto.ClientToken;

public interface AuthService {
	
	String generatePrivateKeyJwt () throws IOException;
	
	ClientToken retrieveClientToken() throws IOException;
	
	TokenResponse generateUserToken(String code,String redirectUri) throws IOException;

}
