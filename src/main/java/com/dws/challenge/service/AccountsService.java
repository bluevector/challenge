package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InvalidTranferAmountServiceException;
import com.dws.challenge.exception.NotEnoughBalanceServiceException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;


  @Autowired
  private NotificationService  notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }


  public void tranferAmount(String fromAccountId, String toAccountId, BigDecimal amount){
    //In my simplest scenario i'm assuming that there are no other oepration in the applicaiton which will update/delete etc, and the only update happens in this method
    //so read operation will not have inconsistent value in a multithreaded environment

    if(amount.compareTo(BigDecimal.ZERO) < 1){
      throw new InvalidTranferAmountServiceException(String.format("Transfer %s is invalid as amout should be positive!", amount));
    }

    Account fromAccount = accountsRepository.getAccount(fromAccountId);
    Account toAccount = accountsRepository.getAccount(toAccountId);

    BigDecimal newBalance = fromAccount.getBalance().subtract(amount);


    if(newBalance.compareTo(BigDecimal.ZERO) > 0) {
      //making the entire operation atomic together
      // This solution prevents deadlock issue, as without acquiring lock if there was another thread trying to do the
      // transfer in reverse it can cause a deadlock situation
      synchronized (this) {
        fromAccount.setBalance(newBalance);
        fromAccount = this.accountsRepository.updateAccount(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(amount));
        toAccount = this.accountsRepository.updateAccount(toAccount);
      }
    } else {
      throw new NotEnoughBalanceServiceException( "Transfer failed as the account id " + fromAccount.getAccountId() + " does not have sufficient balance!");
    }

    this.notificationService.notifyAboutTransfer(fromAccount, String.format("Transferred %s to account %s", amount, toAccountId));
    this.notificationService.notifyAboutTransfer(toAccount, String.format("Transferred %s from account %s", amount, fromAccountId));
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
}
