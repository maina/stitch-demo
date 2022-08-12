package com.stitch.payments.demo.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitch.payments.demo.dto.AccessToken;
import com.stitch.payments.demo.services.LinkPayService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LinkPayController {
	
	private final LinkPayService linkPayService;
	
	@GetMapping("/linkpay-accounts")
	public void getUserAccounts(HttpServletResponse httpServletResponse) {
		var url=linkPayService.linkAccountRequest();
		httpServletResponse.setHeader("Location", url);
		httpServletResponse.setStatus(302);
	}
	@GetMapping("/linkpay-authorizations")
	public AccessToken getUserAccounts(@RequestParam("code") String code,@RequestParam("state") String state) throws IOException {
		return linkPayService.generateUserToken(code, state);
	}
	
	@GetMapping("/linkpay-account")
	public Map<String,Object> linkedAccountInfo(){
		return linkPayService.linkedAccountInfo();
	}

}
