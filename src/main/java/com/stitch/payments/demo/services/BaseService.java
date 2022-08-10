package com.stitch.payments.demo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.graphql.dgs.client.GraphQLClient;
import com.netflix.graphql.dgs.client.HttpResponse;
import com.stitch.payments.demo.repository.UserTokenDao;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BaseService {
	private final RestTemplate restTemplate;
	protected final UserTokenDao userTokenDao;
	@Value("${stitch.graph-ql}")
	private String baseUrl;
	
	protected GraphQLClient client(String token) {
		return GraphQLClient.createCustom(baseUrl, (url, headers, body) -> {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Authorization", "Bearer " + token);
			headers.forEach(httpHeaders::addAll);
			ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.POST,
					new HttpEntity<>(body, httpHeaders), String.class);
			return new HttpResponse(exchange.getStatusCodeValue(), exchange.getBody());
		});
	}

}
