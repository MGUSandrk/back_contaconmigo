package com.sistema_contable.sistema.contable.exceptions.sales;

import org.springframework.http.HttpStatus;

import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;

public class ClientNotFindException extends ModelExceptions {

    public ClientNotFindException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
