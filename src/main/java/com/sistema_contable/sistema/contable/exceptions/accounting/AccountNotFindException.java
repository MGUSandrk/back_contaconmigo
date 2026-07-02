package com.sistema_contable.sistema.contable.exceptions.accounting;

import org.springframework.http.HttpStatus;

import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;

public class AccountNotFindException extends ModelExceptions{
    public AccountNotFindException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
