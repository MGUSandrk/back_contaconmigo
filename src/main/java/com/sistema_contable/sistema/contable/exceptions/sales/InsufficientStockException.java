package com.sistema_contable.sistema.contable.exceptions.sales;

import org.springframework.http.HttpStatus;

import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;

public class InsufficientStockException extends ModelExceptions {

    public InsufficientStockException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
