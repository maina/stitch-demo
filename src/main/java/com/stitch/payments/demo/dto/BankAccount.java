package com.stitch.payments.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {

  private String accountNumber;
  private String accountType;
  private String bankId;
  private String branchCode;
  private String id;
  private String name;
}