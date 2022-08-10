package com.stitch.payments.demo.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.stitch.payments.demo.model.PaymentRequest;

public interface PaymentRequestDao extends CrudRepository<PaymentRequest, Long> {

	Optional<PaymentRequest> findFirstByOrderByIdDesc();

}
