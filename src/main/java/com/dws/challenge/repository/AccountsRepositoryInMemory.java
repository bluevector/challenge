package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountDeosNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public Account updateAccount(Account account) {
        if(accounts.get(account.getAccountId()) != null) {
            return accounts.replace(account.getAccountId(), account);
        } else {
            throw new AccountDeosNotExistException(
                    "Account id " + account.getAccountId() + " does not exist!");
        }
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

}
