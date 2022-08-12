package com.stitch.payments.demo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.stitch.payments.demo.repository.ClientTokenDao;
import com.stitch.payments.demo.repository.UserTokenDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RefundServiceImpl extends BaseService implements RefundService {
	@Autowired
	ClientTokenDao clientTokenDao;

	public RefundServiceImpl(RestTemplate restTemplate, UserTokenDao userTokenDao) {
		super(restTemplate, userTokenDao);
	}

	@Override
	public Map<String, Object> initiateRefund(String paymentRequestId) {
		var mutation="mutation CreateRefund(\n"
				+ "    $amount: MoneyInput!,\n"
				+ "    $reason: RefundReason!,\n"
				+ "    $nonce: String!,\n"
				+ "    $beneficiaryReference: String!,\n"
				+ "    $paymentRequestId: ID!\n"
				+ ") {\n"
				+ "  clientRefundInitiate(input: {\n"
				+ "      amount: $amount,\n"
				+ "      reason: $reason,\n"
				+ "      nonce: $nonce,\n"
				+ "      beneficiaryReference: $beneficiaryReference,\n"
				+ "      paymentRequestId: $paymentRequestId\n"
				+ "    }) {\n"
				+ "    refund {\n"
				+ "      id\n"
				+ "      paymentInitiationRequest {\n"
				+ "        id\n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		Map<String, Object> variables = new HashMap<>();
		Map<String, Object> moneyInput = new HashMap<>();
		moneyInput.put("quantity", 1);
		moneyInput.put("currency", "ZAR");
		variables.put("amount", moneyInput);
		variables.put("paymentRequestId", paymentRequestId);
		variables.put("reason", "fraudulent");
		variables.put("beneficiaryReference", "Refund-"+paymentRequestId);
		variables.put("nonce", DigestUtils.sha256Hex(UUID.randomUUID().toString()));

		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(mutation, variables,
				"CreateRefund");

		log.info("CreateRefund {}", graphQLResponse);
		
		return graphQLResponse.getData();
	}

	@Override
	public Map<String, Object> refundSignedWebhook() {
		return null;
	}
	
	private String clientToken() {
		return clientTokenDao.findFirstByOrderByIdDesc().get().getAccessToken();
	}

	@Override
	public Map<String, Object> refundStatus() {
		return null;
	}

}
