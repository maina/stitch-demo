package com.stitch.payments.demo.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.stitch.payments.demo.model.StitchAuthorizationRequest;

public interface StitchAuthorizationRequestDao extends CrudRepository<StitchAuthorizationRequest, Long> {

	Optional<StitchAuthorizationRequest> findByStitchState(String state);

}
