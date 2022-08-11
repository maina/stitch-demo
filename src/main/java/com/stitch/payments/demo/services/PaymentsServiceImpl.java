package com.stitch.payments.demo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.stitch.payments.demo.dto.InitiatePaymentRequestResponse;
import com.stitch.payments.demo.model.PaymentRequest;
import com.stitch.payments.demo.repository.ClientTokenDao;
import com.stitch.payments.demo.repository.PaymentRequestDao;
import com.stitch.payments.demo.repository.UserTokenDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentsServiceImpl extends BaseService implements PaymentsService {
	@Autowired
	ClientTokenDao clientTokenDao;
	@Autowired
	PaymentRequestDao paymentRequestDao;
	
	@Value("${stitch.webhook-instant-pay}")
	private String instantPayWebhookUrl;
	@Value("${stitch.webhook-secret}")
	private String webhookSecret;

	public PaymentsServiceImpl(RestTemplate restTemplate, UserTokenDao userTokenDao) {
		super(restTemplate, userTokenDao);

	}

	private String clientToken() {
		return clientTokenDao.findFirstByOrderByIdDesc().get().getAccessToken();
	}

	@Override
	public InitiatePaymentRequestResponse initiatePaymentRequest() {
		var mutation = "mutation CreatePaymentRequest(\n" + "    $amount: MoneyInput!,\n"
				+ "    $payerReference: String!,\n" + "    $beneficiaryReference: String!,\n"
				+ "    $externalReference: String,\n" + "    $beneficiaryName: String!,\n"
				+ "    $beneficiaryBankId: BankBeneficiaryBankId!,\n" + "    $beneficiaryAccountNumber: String!) {\n"
				+ "  clientPaymentInitiationRequestCreate(input: {\n" + "      amount: $amount,\n"
				+ "      payerReference: $payerReference,\n" + "      beneficiaryReference: $beneficiaryReference,\n"
				+ "      externalReference: $externalReference,\n" + "      beneficiary: {\n"
				+ "          bankAccount: {\n" + "              name: $beneficiaryName,\n"
				+ "              bankId: $beneficiaryBankId,\n"
				+ "              accountNumber: $beneficiaryAccountNumber\n" + "          }\n" + "      }\n"
				+ "    }) {\n" + "    paymentInitiationRequest {\n" + "      id\n" + "      url\n" + "    }\n" + "  }\n"
				+ "}";

		Map<String, Object> variables = new HashMap<>();
		Map<String, Object> moneyInput = new HashMap<>();
		moneyInput.put("quantity", 1);
		moneyInput.put("currency", "ZAR");
		variables.put("amount", moneyInput);
		variables.put("payerReference", "SambarSnacks");
		variables.put("beneficiaryReference", "SambarSnacks");
		variables.put("externalReference",UUID.randomUUID().toString());

		variables.put("beneficiaryName", "ABSA");
		variables.put("beneficiaryBankId", "absa");
		variables.put("beneficiaryAccountNumber", "12345678901");

		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(mutation, variables,
				"CreatePaymentRequest");

		log.info("CreatePaymentResponse {}", graphQLResponse);

		var response = graphQLResponse.dataAsObject(InitiatePaymentRequestResponse.class);

		var paymentRequest = new PaymentRequest();
		paymentRequest.setReferenceId(
				response.getClientPaymentInitiationRequestCreate().getPaymentInitiationRequest().getId());
		paymentRequest
				.setUrl(response.getClientPaymentInitiationRequestCreate().getPaymentInitiationRequest().getUrl());

		paymentRequestDao.save(paymentRequest);

		return response;

	}

	@Override
	public Map<String, Object> paymentRequestStatus(String paymentRequestId) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("paymentRequestId", paymentRequestId);

		var query = "query GetPaymentRequestStatus($paymentRequestId: ID!) {\n"
				+ "  node(id: $paymentRequestId) {\n"
				+ "    ... on PaymentInitiationRequest {\n"
				+ "      id\n"
				+ "      url\n"
				+ "      payerReference\n"
				+ "      state {\n"
				+ "        __typename\n"
				+ "        ... on PaymentInitiationRequestCompleted {\n"
				+ "          date\n"
				+ "          amount\n"
				+ "          payer {\n"
				+ "            ... on PaymentInitiationBankAccountPayer {\n"
				+ "              accountNumber\n"
				+ "              bankId\n"
				+ "            }\n"
				+ "          }\n"
				+ "          beneficiary {\n"
				+ "            ... on BankBeneficiary {\n"
				+ "              bankId\n"
				+ "            }\n"
				+ "          }\n"
				+ "        }\n"
				+ "        ... on PaymentInitiationRequestCancelled {\n"
				+ "          date\n"
				+ "          reason\n"
				+ "        }\n"
				+ "        ... on PaymentInitiationRequestPending {\n"
				+ "          __typename\n"
				+ "          paymentInitiationRequest {\n"
				+ "            id\n"
				+ "          }\n"
				+ "        }\n"
				+ "      }\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";

		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(query, variables, "GetPaymentRequestStatus");

		log.info("GetPaymentRequestStatus {}", graphQLResponse);
		return graphQLResponse.getData();
	}

	@Override
	public Map<String, Object> instantPaySignedWebhook() {
		var query="subscription InstantPayUpdatesWithHmac($webhookUrl: URL!, $secret: String!) {\n"
				+ "  client(webhook: { url: $webhookUrl, secret: { hmacSha256Key: $secret }}) {\n"
				+ "    paymentInitiationRequests {\n"
				+ "      # Like edges in collections, subscriptions event edges \n"
				+ "      # have nodes containing the data. Addjacent to this is metadata\n"
				+ "      node {\n"
				+ "        id\n"
				+ "        externalReference\n"
				+ "        state {\n"
				+ "          __typename\n"
				+ "          ... on PaymentInitiationRequestCompleted {\n"
				+ "            date\n"
				+ "          }\n"
				+ "          ... on PaymentInitiationRequestCancelled {\n"
				+ "            __typename\n"
				+ "            date\n"
				+ "            reason\n"
				+ "          }\n"
				+ "        }\n"
				+ "      }\n"
				+ "      eventId\n"
				+ "      subscriptionId\n"
				+ "      time\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		
		Map<String, Object> variables = new HashMap<>();
		variables.put("webhookUrl", instantPayWebhookUrl);
		variables.put("secret", webhookSecret);
		GraphQLResponse graphQLResponse = client(clientToken()).executeQuery(query, variables, "InstantPayUpdatesWithHmac");

		log.info("InstantPayUpdatesWithHmac {}", graphQLResponse);
		return graphQLResponse.getData();
	}

}
