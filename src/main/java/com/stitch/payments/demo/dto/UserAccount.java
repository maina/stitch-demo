package com.stitch.payments.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserAccount {

  private UserData user;

  @Data
  public class UserData {

    private List<BankAccount> bankAccounts;

   
  }
}
