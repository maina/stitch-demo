package com.stitch.payments.demo.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StitchAuthorizationRequest {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	@Column
	private String stitchState;
	@Column
	private UUID userId;
	@Column
	private String codeVerifier;
	@Column
	private String codeChallenge;

}
