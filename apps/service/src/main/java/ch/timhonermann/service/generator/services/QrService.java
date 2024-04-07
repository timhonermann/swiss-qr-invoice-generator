package ch.timhonermann.service.generator.services;

import ch.codeblock.qrinvoice.FontFamily;
import ch.codeblock.qrinvoice.OutputFormat;
import ch.codeblock.qrinvoice.PageSize;
import ch.codeblock.qrinvoice.QrInvoicePaymentPartReceiptCreator;
import ch.codeblock.qrinvoice.model.QrInvoice;
import ch.codeblock.qrinvoice.model.ReferenceType;
import ch.codeblock.qrinvoice.model.builder.QrInvoiceBuilder;
import ch.codeblock.qrinvoice.model.validation.ValidationException;
import ch.codeblock.qrinvoice.util.CreditorReferenceUtils;

import java.util.Locale;

import ch.timhonermann.service.generator.dtos.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QrService {
  private final InvoiceCalculatorService invoiceCalculatorService;

  @Autowired
  public QrService(InvoiceCalculatorService invoiceCalculatorService) {
    this.invoiceCalculatorService = invoiceCalculatorService;
  }

  public byte[] createQrBill(Invoice invoice) {
    QrInvoice qrInvoice = buildInvoice(invoice);

    return buildQrBill(qrInvoice);
  }

  private QrInvoice buildInvoice(Invoice invoice) {
    QrInvoice qrInvoice = null;

    var qrReference = CreditorReferenceUtils.createCreditorReference(invoice.reference());
    var amount = invoiceCalculatorService.calculateTotalAmount(invoice.items());

    try {
      qrInvoice = QrInvoiceBuilder
        .create()
        .creditorIBAN(invoice.creditor().iban())
        .paymentAmountInformation(p -> p.chf(amount))
        .creditor(c -> c.structuredAddress()
          .name(invoice.creditor().name())
          .streetName(invoice.creditor().streetName())
          .houseNumber(invoice.creditor().streetNumber())
          .postalCode(invoice.creditor().postalCode())
          .city(invoice.creditor().city())
          .country(invoice.creditor().country())
        )
        .ultimateDebtor(d -> d.structuredAddress()
          .name(invoice.ultimateDebtor().name())
          .streetName(invoice.ultimateDebtor().streetName())
          .houseNumber(invoice.ultimateDebtor().streetNumber())
          .postalCode(invoice.ultimateDebtor().postalCode())
          .city(invoice.ultimateDebtor().city())
          .country(invoice.ultimateDebtor().country())
        )
        .paymentReference(r -> r.reference(qrReference).referenceType(ReferenceType.CREDITOR_REFERENCE))
        .build();
    } catch (ValidationException validationException) {
      var validationResult = validationException.getValidationResult();

      System.out.println(validationResult.getValidationErrorSummary());
      System.exit(1);
    }

    return qrInvoice;
  }

  private byte[] buildQrBill(QrInvoice qrInvoice) {
    return QrInvoicePaymentPartReceiptCreator
      .create()
      .qrInvoice(qrInvoice)
      .outputFormat(OutputFormat.PDF)
      .pageSize(PageSize.A4)
      .fontFamily(FontFamily.LIBERATION_SANS)
      .locale(Locale.GERMAN)
      .createPaymentPartReceipt()
      .getData();
  }
}
