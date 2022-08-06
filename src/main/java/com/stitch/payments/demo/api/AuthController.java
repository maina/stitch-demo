package com.stitch.payments.demo.api;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitch.payments.demo.TokenManager;
import com.stitch.payments.demo.dto.ClientToken;
import com.stitch.payments.demo.services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	private final AuthService authService;
	private final TokenManager tokenManager;

	@PostMapping("/client-tokens")
	public ClientToken getClientToken() throws IOException {
		//var token=tokenManager.getAccessToken();
		//log.info("Token {}",token);
		return authService.retrieveClientToken();
	}
	
	@GetMapping("/user-tokens")
	public String getUserToken(@RequestParam("code") String code) {
		
		return code;
	}

}
