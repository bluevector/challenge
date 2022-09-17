package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InvalidTranferAmountServiceException;
import com.dws.challenge.exception.NotEnoughBalanceServiceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {
  private Account fromAccount;
  private Account toAccount;

  private String tranferAmount = "500";

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private NotificationService notificationService;


  @Test
  void addAccount() {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  void addAccount_failsOnDuplicateId() {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
  }

  void setupAccount(String fromAccountId, String toAccountId){

    fromAccount = new Account(fromAccountId);
    toAccount = new Account(toAccountId);

    fromAccount.setBalance(new BigDecimal(1000));
    toAccount.setBalance(new BigDecimal(1000));

    accountsService.createAccount(fromAccount);
    accountsService.createAccount(toAccount);
  }


  @Test
  void tranferAmountNegative(){
    String fromAccountId = "Id-111";
    String toAccountId = "Id-222";

    setupAccount(fromAccountId, toAccountId);


    BigDecimal transferAmount = new BigDecimal(-500);
    try {
      this.accountsService.tranferAmount(fromAccountId, toAccountId, transferAmount);
      fail("Test should fail as transfer amount should be positive");
    }catch (InvalidTranferAmountServiceException itase){
      assertThat(itase.getMessage()).isEqualTo(String.format("Transfer %s is invalid as amout should be positive!", transferAmount));
    }
  }

  @Test
  void tranferAmountInsufficientBalance(){
    String fromAccountId = "Id-333";
    String toAccountId = "Id-444";

    setupAccount(fromAccountId, toAccountId);


    try {
      this.accountsService.tranferAmount(fromAccountId, toAccountId, new BigDecimal(1500));
      fail("Test should fail as from account should have sufficient balance to transfer");
    } catch(NotEnoughBalanceServiceException nebse){
      assertThat(nebse.getMessage()).isEqualTo(String.format("Transfer failed as the account id %s does not have sufficient balance!", fromAccountId));
    }
  }

  @Test
  void tranferAmountValid(){
    String fromAccountId = "Id-555";
    String toAccountId = "Id-666";

    setupAccount(fromAccountId, toAccountId);

    doNothing().when(notificationService).notifyAboutTransfer(fromAccount, String.format("Transferred %s to account %s", tranferAmount, toAccountId));
    doNothing().when(notificationService).notifyAboutTransfer(toAccount, String.format("Transferred %s from account %s", tranferAmount, fromAccountId));

    this.accountsService.tranferAmount(fromAccountId, toAccountId, new BigDecimal(tranferAmount));

    Mockito.verify(notificationService).notifyAboutTransfer(fromAccount, String.format("Transferred %s to account %s", tranferAmount, toAccountId));
    Mockito.verify(notificationService).notifyAboutTransfer(toAccount, String.format("Transferred %s from account %s", tranferAmount, fromAccountId));
  }
}
