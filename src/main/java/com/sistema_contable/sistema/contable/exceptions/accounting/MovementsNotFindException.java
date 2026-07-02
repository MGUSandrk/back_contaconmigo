package com.sistema_contable.sistema.contable.exceptions.accounting;

import org.springframework.http.HttpStatus;

import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;

public class MovementsNotFindException extends ModelExceptions {

    public MovementsNotFindException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return  HttpStatus.NOT_FOUND;
    }
}
