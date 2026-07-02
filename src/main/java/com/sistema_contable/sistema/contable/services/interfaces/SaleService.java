package com.sistema_contable.sistema.contable.services.interfaces;

import java.util.List;

import com.sistema_contable.sistema.contable.dto.sales.InvoiceResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleRequestDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleResponseDTO;
import com.sistema_contable.sistema.contable.model.User;

public interface SaleService {
    InvoiceResponseDTO createSale(SaleRequestDTO saleRequestDTO, User seller) throws Exception;
    List<SaleResponseDTO> getAllSales() throws Exception;
    List<SaleResponseDTO> getSalesByClientId(Long clientId) throws Exception;
    List<InvoiceResponseDTO> getInvoicesByClientCuit(String clientCuit) throws Exception;
    Long countSalesOfCurrentMonth() throws Exception;
}
