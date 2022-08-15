package com.stitch.payments.demo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${stitch.webhook-refunds}")
	private String linkPayWebhookUrl;
	
	@Value("${stitch.webhook-secret}")
	private String webhookSecret;

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
		variables.put("beneficiaryReference", "Refundsnacks");
		variables.put("nonce", DigestUtils.sha256Hex(UUID.randomUUID().toString()));

		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(mutation, variables,
				"CreateRefund");

		log.info("CreateRefund {}", graphQLResponse);
		
		return graphQLResponse.getData();
	}

	@Override
	public Map<String, Object> refundSignedWebhook() {
		var query="subscription RefundUpdates($webhookUrl: URL!, $headers: [InputHeader!])  {\n"
				+ "  client(webhook: {url: $webhookUrl, headers: $headers}) {\n"
				+ "    refunds {\n"
				+ "      node {\n"
				+ "        status {\n"
				+ "          ... on RefundSubmitted {\n"
				+ "            __typename\n"
				+ "            date\n"
				+ "          }\n"
				+ "          ... on RefundCompleted {\n"
				+ "            __typename\n"
				+ "            date\n"
				+ "            expectedSettlement\n"
				+ "          }\n"
				+ "          ... on RefundError {\n"
				+ "            __typename\n"
				+ "            date\n"
				+ "            reason\n"
				+ "          }\n"
				+ "        }\n"
				+ "        reason\n"
				+ "        id\n"
				+ "        created\n"
				+ "        amount\n"
				+ "        beneficiaryReference\n"
				+ "      }\n"
				+ "      eventId\n"
				+ "      subscriptionId\n"
				+ "      time\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		Map<String, Object> variables = new HashMap<>();
		variables.put("webhookUrl", linkPayWebhookUrl);
		variables.put("secret", webhookSecret);
		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(query, variables, "RefundUpdates");

		log.info("RefundUpdates {}", graphQLResponse);
		return graphQLResponse.getData();
	}
	
	private String clientToken() {
		return clientTokenDao.findFirstByOrderByIdDesc().get().getAccessToken();
	}

	@Override
	public Map<String, Object> refundStatus(String id) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("refundId", id);
		var query="query GetRefundStatus($refundId: ID!) {\n"
				+ "  node(id: $refundId) {\n"
				+ "    ... on Refund {\n"
				+ "      id\n"
				+ "      status {\n"
				+ "        ... on RefundPending {\n"
				+ "          __typename\n"
				+ "          date\n"
				+ "        }\n"
				+ "        ... on RefundSubmitted {\n"
				+ "          __typename\n"
				+ "          date\n"
				+ "        }\n"
				+ "        ... on RefundCompleted {\n"
				+ "          __typename\n"
				+ "          date\n"
				+ "          expectedSettlement\n"
				+ "        }\n"
				+ "        ... on RefundError {\n"
				+ "          __typename\n"
				+ "          date\n"
				+ "          reason\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		
		
		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(query, variables,
				"GetRefundStatus");

		log.info("GetRefundStatus {}", graphQLResponse);
		
		return graphQLResponse.getData();
	}

}
