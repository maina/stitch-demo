package com.stitch.payments.demo.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.stitch.payments.demo.model.ClientToken;

public interface ClientTokenDao extends CrudRepository<ClientToken, Long> {

	Optional<ClientToken> findFirstByOrderByIdDesc();

}
