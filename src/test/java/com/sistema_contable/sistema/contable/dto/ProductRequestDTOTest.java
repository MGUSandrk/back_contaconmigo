package com.sistema_contable.sistema.contable.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

import com.sistema_contable.sistema.contable.dto.sales.PaymentRequestDTO;

class ProductRequestDTOTest {

    @Test
    void paymentsUsesSalesPaymentRequestDTOAsGenericType() throws Exception {
        Type returnType = ProductRequestDTO.class.getMethod("getPayments").getGenericReturnType();

        assertTrue(returnType instanceof ParameterizedType);
        Type paymentType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        assertEquals(PaymentRequestDTO.class, paymentType);
    }
}
