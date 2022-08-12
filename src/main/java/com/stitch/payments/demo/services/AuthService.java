package com.stitch.payments.demo.services;

import java.io.IOException;

import com.stitch.payments.demo.dto.AccessToken;
import com.stitch.payments.demo.dto.ClientToken;

public interface AuthService {
	
	
	ClientToken retrieveClientToken() throws IOException;
	
	AccessToken generateUserToken(String code,String state) throws IOException;
	
	AccessToken refreshUserToken() throws IOException;

}
