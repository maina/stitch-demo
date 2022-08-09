package com.stitch.payments.demo.services;

import java.util.Map;

import com.stitch.payments.demo.dto.UserAccount;

public interface FinancialDataService {
	UserAccount accounts();
	Map<String,Object> accountHolders();
	Map<String, Object> balances();
	Map<String, Object> transactions();
	Map<String, Object> debitOrderPayments();
	

}
