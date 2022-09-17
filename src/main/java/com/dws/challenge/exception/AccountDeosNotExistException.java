package com.dws.challenge.exception;

public class AccountDeosNotExistException extends RuntimeException{
    public AccountDeosNotExistException(String message){
        super(message);
    }
}
