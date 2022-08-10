package com.stitch.payments.demo.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.stitch.payments.demo.dto.UserAccount;
import com.stitch.payments.demo.repository.UserTokenDao;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FinancialDataServiceImpl  extends BaseService implements FinancialDataService {
	public FinancialDataServiceImpl(RestTemplate restTemplate, UserTokenDao userTokenDao) {
		super(restTemplate, userTokenDao);

	}
	
	private String getAccessToken() {
		return userTokenDao.findFirstByOrderByIdDesc().get().getAccessToken();
	}

	@Override
	public UserAccount accounts() {
		var query = "query GetAccounts {\n" + "  user {\n" + "    bankAccounts {\n" + "      accountNumber\n"
				+ "      accountType\n" + "      bankId\n" + "      branchCode\n" + "      id\n" + "      name\n"
				+ "    }\n" + "  }\n" + "  \n" + "}";
		GraphQLResponse graphQLResponse = client(getAccessToken()).executeQuery(query, Collections.emptyMap(), "GetAccounts");

		log.info("Accounts {}", graphQLResponse);

		return graphQLResponse.dataAsObject(UserAccount.class);
	}

	@Override
	public Map<String, Object> accountHolders() {
		var query = "query GetAccountHolders {\n" + "  user {\n" + "    bankAccounts {\n" + "      accountHolder {\n"
				+ "        __typename\n" + "        ... on Individual {\n" + "          gender\n"
				+ "          fullName\n" + "          email\n" + "          familyName\n" + "          givenName\n"
				+ "          identifyingDocument {\n" + "            ... on IdentityDocument {\n"
				+ "              __typename\n" + "              country\n" + "              number\n"
				+ "            }\n" + "            ... on Passport {\n" + "              __typename\n"
				+ "              country\n" + "              number\n" + "            }\n" + "          }\n"
				+ "          middleName\n" + "          nickname\n" + "          homeAddress {\n"
				+ "            country\n" + "            formatted\n" + "            locality\n"
				+ "            postalCode\n" + "            region\n" + "            streetAddress\n" + "          }\n"
				+ "          contact {\n" + "            name\n" + "            phoneNumber\n" + "          }\n"
				+ "        }\n" + "        ... on Business {\n" + "          registrationNumber\n" + "          name\n"
				+ "          accountContact {\n" + "            name\n" + "            phoneNumber\n" + "          }\n"
				+ "          businessAddress {\n" + "            country\n" + "            formatted\n"
				+ "            locality\n" + "            postalCode\n" + "            streetAddress\n"
				+ "            region\n" + "          }\n" + "          email\n" + "        }\n" + "      }\n"
				+ "    }\n" + "  }\n" + "}\n" + "";

		GraphQLResponse graphQLResponse = client(getAccessToken()).executeQuery(query, Collections.emptyMap(), "GetAccountHolders");

		log.info("Accounts {}", graphQLResponse);
		return graphQLResponse.getData();

	}

	@Override
	public Map<String, Object> balances() {
		var query = "query GetAccountBalances {\n" + "  user {\n" + "    bankAccounts {\n" + "      currentBalance\n"
				+ "      availableBalance\n" + "      id\n" + "      name\n" + "    }\n" + "  }\n" + "}";

		GraphQLResponse graphQLResponse = client(getAccessToken()).executeQuery(query, Collections.emptyMap(), "GetAccountBalances");

		log.info("Accounts {}", graphQLResponse);
		return graphQLResponse.getData();
	}

	@Override
	public Map<String, Object> transactions() {

		var accounts = accounts();

		Map<String, Object> variables = new HashMap<>();
		variables.put("first", 10);
		variables.put("accountId", accounts.getUser().getBankAccounts().get(0).getId());

		var query = "query TransactionsByBankAccount($accountId: ID!, $first: UInt, $after: Cursor) {\n"
				+ "  node(id: $accountId) {\n" + "    ... on BankAccount {\n"
				+ "      transactions(first: $first, after: $after) {\n" + "        pageInfo {\n"
				+ "          hasNextPage\n" + "          endCursor\n" + "        }\n" + "        edges {\n"
				+ "          node {\n" + "            id\n" + "            amount\n" + "            reference\n"
				+ "            description\n" + "            date\n" + "            runningBalance\n" + "          }\n"
				+ "        }\n" + "      }\n" + "    }\n" + "  }\n" + "}";

		GraphQLResponse graphQLResponse = client(getAccessToken()).executeQuery(query, variables, "TransactionsByBankAccount");

		log.info("Accounts {}", graphQLResponse);
		return graphQLResponse.getData();

	}

	@Override
	public Map<String, Object> debitOrderPayments() {
		var accounts = accounts();

		Map<String, Object> variables = new HashMap<>();
		variables.put("first", 10);
		variables.put("accountId", accounts.getUser().getBankAccounts().get(0).getId());

		var query = "query DebitOrderPaymentsByBankAccount($accountId: ID!, $first: UInt, $after: Cursor) {\n"
				+ "  node(id: $accountId) {\n" + "    ... on BankAccount {\n"
				+ "      debitOrderPayments(first: $first, after: $after) {\n" + "        pageInfo {\n"
				+ "          hasNextPage\n" + "          endCursor\n" + "        }\n" + "        edges {\n"
				+ "          node {\n" + "            id\n" + "            amount\n" + "            reference\n"
				+ "            date\n" + "          }\n" + "        }\n" + "      }\n" + "    }\n" + "  }\n" + "}";

		GraphQLResponse graphQLResponse = client(getAccessToken()).executeQuery(query, variables, "DebitOrderPaymentsByBankAccount");

		log.info("Accounts {}", graphQLResponse);
		return graphQLResponse.getData();
	}



}
