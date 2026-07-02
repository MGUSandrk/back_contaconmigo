package com.sistema_contable.sistema.contable.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;
import com.sistema_contable.sistema.contable.services.interfaces.InvoiceService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

@RestController
@RequestMapping("/sale/invoice")
@CrossOrigin(origins = "${FRONT_URL}")
public class InvoiceResource {

    //dependencies
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private AuthorizationService authService;

    //endpoints
    @GetMapping(value = "/{id}", produces = "application/pdf")
    public ResponseEntity<?> getInvoicePdf(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean download) {
        try {
            authService.authorize(token);
            byte[] pdf = invoiceService.generatePdf(id);

            String disposition = download ? "attachment" : "inline";
            String filename = "invoice-" + id + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println("error: " + modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
