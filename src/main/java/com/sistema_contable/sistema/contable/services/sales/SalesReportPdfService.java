package com.sistema_contable.sistema.contable.services.sales;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sistema_contable.sistema.contable.dto.sales.SaleResponseDTO;

@Service
public class SalesReportPdfService {

    private final TemplateEngine templateEngine;

    public SalesReportPdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(
            Integer month,
            Integer year,
            List<SaleResponseDTO> sales,
            Map<String, Double> totalsByPaymentType,
            Double totalIncome) {
        try {
            Context context = new Context();
            context.setVariable("month", month);
            context.setVariable("year", year);
            context.setVariable("sales", sales);
            context.setVariable("totalsByPaymentType", totalsByPaymentType);
            context.setVariable("totalIncome", totalIncome);

            String html = templateEngine.process("pdf/sales-report", context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html, null);
                builder.toStream(outputStream);
                builder.run();

                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de informe de ventas", e);
        }
    }
}
