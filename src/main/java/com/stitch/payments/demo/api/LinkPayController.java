package com.stitch.payments.demo.api;

import java.io.IOException;
import java.util.HashMap;
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
	//authorization is redirected here
	@GetMapping("/linkpay-authorizations")
	public AccessToken getUserAccounts(@RequestParam("code") String code,@RequestParam("state") String state) throws IOException {
		return linkPayService.generateUserToken(code, state);
	}
	
	@GetMapping("/linkpay-account")
	public Map<String,Object> linkedAccountInfo(){
		return linkPayService.linkedAccountInfo();
	}
	
	@GetMapping("/linkpay-payments")
	public Map<String,Object> viewInitiatedPayments(){
		return linkPayService.viewInitiatedPayments();
	}
	
	@GetMapping("/linkpay-user-payments")
	public Map<String,Object> initiatePayment(HttpServletResponse httpServletResponse){
		var response= linkPayService.initiatePayment();
		if(response.containsKey("USER_INTERACTION_REQUIRED")) {
			httpServletResponse.setHeader("Location", response.get("USER_INTERACTION_REQUIRED").toString());
			httpServletResponse.setStatus(302);
			return null;
		}
		return response;
	}
	
	@GetMapping("/linkpay-user-interactions")
	public Map<String,Object> userInteraction(@RequestParam("id") String id,@RequestParam("status") String status,@RequestParam("externalReference") String externalReference){
		
		var map= new HashMap<String,Object>();
		map.put("id", id);
		map.put("status", status);
		map.put("externalReference", externalReference);
		
		
		return map;
	}

}
