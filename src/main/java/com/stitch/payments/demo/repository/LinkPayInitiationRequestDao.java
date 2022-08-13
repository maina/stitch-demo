package com.stitch.payments.demo.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.stitch.payments.demo.model.LinkPayInitiationRequest;

public interface LinkPayInitiationRequestDao extends CrudRepository<LinkPayInitiationRequest, Long> {

	Optional<LinkPayInitiationRequest> findFirstByOrderByIdDesc();

}
