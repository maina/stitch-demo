package com.stitch.payments.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientPaymentInitiationRequestCreate {
	PaymentInitiationRequest paymentInitiationRequest;

}
