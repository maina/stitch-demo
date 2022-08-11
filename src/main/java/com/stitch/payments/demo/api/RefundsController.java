package com.stitch.payments.demo.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stitch.payments.demo.services.RefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RefundsController {
	
	private final RefundService refundService;
	
	@GetMapping("/refunds")
	public Map<String, Object> initiateRefund(@RequestParam("id") String id) {
		log.info("refund id {}", id);
		return refundService.initiateRefund(id);
	}
}
 