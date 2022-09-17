package com.dws.challenge.exception;

public class NotEnoughBalanceServiceException extends RuntimeException{
    public NotEnoughBalanceServiceException(String message){
        super(message);
    }
}
