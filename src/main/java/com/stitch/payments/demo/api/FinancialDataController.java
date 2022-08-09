package com.stitch.payments.demo.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stitch.payments.demo.dto.UserAccount;
import com.stitch.payments.demo.services.FinancialDataService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FinancialDataController {
	private final FinancialDataService financialDataService;
	@GetMapping("/user-accounts")
	public UserAccount getUserAccounts() {
		return financialDataService.accounts();
	}
	@GetMapping("/account-holders")
	public Map<String,Object> getAccountHolders() {
		return financialDataService.accountHolders();
	}
	@GetMapping("/account-transactions")
	public Map<String,Object> getAccountTransactions() {
		return financialDataService.transactions();
	}
	@GetMapping("/account-balances")
	public Map<String,Object> getAccountBalances() {
		return financialDataService.balances();
	}

}
