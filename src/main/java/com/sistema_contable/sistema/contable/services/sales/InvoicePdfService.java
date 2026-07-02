package com.sistema_contable.sistema.contable.services.sales;
import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sistema_contable.sistema.contable.model.sales.Invoice;

@Service
public class InvoicePdfService {

    private final TemplateEngine templateEngine;

    public InvoicePdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(Invoice factura) {
        try {
            Context context = new Context();
            context.setVariable("factura", factura);

            String html = templateEngine.process("pdf/factura", context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html, null);
                builder.toStream(outputStream);
                builder.run();

                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de factura interna", e);
        }
    }
}