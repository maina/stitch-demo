package com.stitch.payments.demo.api;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitch.payments.demo.dto.AccessToken;
import com.stitch.payments.demo.dto.ClientToken;
import com.stitch.payments.demo.services.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	private final AuthService authService;

	@PostMapping("/client-tokens")
	public ClientToken getClientToken() throws IOException {
		
		return authService.retrieveClientToken();
	}
	@GetMapping("/refresh-token")
	public AccessToken refreshToken() throws IOException {
		
		return authService.refreshUserToken();
	}
	
	@GetMapping("/user-tokens")
	public AccessToken getUserToken(@RequestParam("code") String code,@RequestParam("state") String state) throws IOException {
		log.info("Getting token with code>> {} and state {} ",code, state);
		return authService.generateUserToken(code, state);
	}

}
