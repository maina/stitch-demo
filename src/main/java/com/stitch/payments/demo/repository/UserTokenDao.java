package com.stitch.payments.demo.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.stitch.payments.demo.model.UserToken;

public interface UserTokenDao extends CrudRepository<UserToken, Long> {

	Optional<UserToken> findFirstByOrderByIdDesc();

}
