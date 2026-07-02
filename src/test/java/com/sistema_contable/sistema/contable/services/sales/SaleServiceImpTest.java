package com.sistema_contable.sistema.contable.services.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sistema_contable.sistema.contable.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceImpTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SaleServiceImp service;

    @Test
    void countSalesOfCurrentMonthCountsSalesBetweenMonthStartAndNextMonthStart() throws Exception {
        when(saleRepository.countSalesByDateCreatedBetween(
                expectedMonthStart(),
                expectedNextMonthStart()
        )).thenReturn(7L);

        Long result = service.countSalesOfCurrentMonth();

        assertEquals(7L, result);
        ArgumentCaptor<Date> startDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endDateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(saleRepository).countSalesByDateCreatedBetween(startDateCaptor.capture(), endDateCaptor.capture());
        assertEquals(expectedMonthStart(), startDateCaptor.getValue());
        assertEquals(expectedNextMonthStart(), endDateCaptor.getValue());
    }

    private Date expectedMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date expectedNextMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expectedMonthStart());
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }
}
