package com.stitch.payments.demo.repository;

import org.springframework.data.repository.CrudRepository;

import com.stitch.payments.demo.model.UserToken;

public interface UserTokenDao extends CrudRepository<UserToken, Long> {

	UserToken findFirstByOrderByIdDesc();

}
