package ch.timhonermann.service.generator.services;

import ch.codeblock.qrinvoice.pdf.QrPdfMerger;
import ch.timhonermann.service.generator.dtos.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneratorService {
  private final QrService qrService;

  private final PdfService pdfService;

  @Autowired
  GeneratorService(QrService qrService, PdfService pdfService) {
    this.qrService = qrService;
    this.pdfService = pdfService;
  }

  public byte[] createInvoice(Invoice invoice) {
    var pdfInvoice = pdfService.generatePdfInvoice(invoice);
    var qrBill = qrService.createQrBill(invoice);

    try {
      return QrPdfMerger.create().mergePdfs(pdfInvoice, qrBill);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }

    return new byte[0];
  }
}
