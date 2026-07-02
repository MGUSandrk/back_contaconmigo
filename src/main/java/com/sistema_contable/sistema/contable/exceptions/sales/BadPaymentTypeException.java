package com.sistema_contable.sistema.contable.exceptions.sales;

import org.springframework.http.HttpStatus;

import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;

public class BadPaymentTypeException extends ModelExceptions {

    public BadPaymentTypeException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
