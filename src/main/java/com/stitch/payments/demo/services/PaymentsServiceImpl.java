package com.stitch.payments.demo.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

}
