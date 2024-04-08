package ch.timhonermann.service.generator.services;

import ch.timhonermann.service.generator.dtos.Creditor;
import ch.timhonermann.service.generator.dtos.Invoice;
import ch.timhonermann.service.generator.dtos.Item;
import ch.timhonermann.service.generator.dtos.UltimateDebtor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class PdfService {
  private static final int MARGIN_LEFT_MILLIMETERS = 25;
  private static final int MARGIN_RIGHT_MILLIMETERS = 10;
  private static final int MARGIN_TOP_MILLIMETERS = 10;
  private static final int RECEIVER_MARGIN_TOP_MILLIMETERS = 50;
  private static final int TITLE_MARGIN_TOP_MILLIMETERS = 100;

  private static final int ENTRY_HEIGHT = 17;
  private static final int POS_WIDTH_MILLIMETERS = 15;
  private static final int DESCRIPTION_WIDTH_MILLIMETERS = 70;
  private static final int QUANTITY_WIDTH_MILLIMETERS = 15;
  private static final int VAT_WIDTH_MILLIMETERS = 15;
  private static final int UNIT_PRICE_WIDTH_MILLIMETERS = 25;
  private static final int AMOUNT_WIDTH_MILLIMETERS = 35;

  private static final float FONT_SIZE_DEFAULT = 10f;
  private static final float FONT_SIZE_SMALL = 8f;
  private static final float TITLE_TEXT_SIZE = 14f;
  private static final float DEFAULT_LINE_HEIGHT = 14f;

  private static final String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

  private final InvoiceCalculatorService invoiceCalculatorService;

  @Autowired
  PdfService(InvoiceCalculatorService invoiceCalculatorService) {
    this.invoiceCalculatorService = invoiceCalculatorService;
  }

  public byte[] generatePageWithHeader(Invoice invoice) {
    var byteArrayOutputStream = new ByteArrayOutputStream();

    try (var document = initializePdf()) {
      addHeader(document, 0, invoice.creditor());
      document.save(byteArrayOutputStream);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      System.exit(1);
    }

    return byteArrayOutputStream.toByteArray();
  }

  public byte[] generatePdfInvoice(Invoice invoice) {
    var byteArrayOutputStream = new ByteArrayOutputStream();
    var emptyLayoutPage = generatePageWithHeader(invoice);

    try (var document = PDDocument.load(emptyLayoutPage)) {
      addReceiver(document, invoice.creditor(), invoice.ultimateDebtor());
      addTitle(document, 0, invoice.title());
      addConditions(document, invoice);
      addInvoiceItems(document, invoice.items());

      document.save(byteArrayOutputStream);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      System.exit(1);
    }

    return byteArrayOutputStream.toByteArray();
  }

  private PDDocument initializePdf() {
    var invoice = new PDDocument();
    var page = new PDPage(PDRectangle.A4);

    invoice.addPage(page);

    return invoice;
  }

  private void addHeader(PDDocument document, int pageNumber, Creditor creditor) throws IOException {
    var page = document.getPage(pageNumber);
    var contentStream = new PDPageContentStream(document, page);

    var name = creditor.name();
    var street = String.format("%s %s", creditor.streetName(), creditor.streetNumber());
    var city = String.format("%s %s", creditor.postalCode(), creditor.city());
    var phone = creditor.phone();
    var email = creditor.email();

    var headerLines = List.of(name, street, city, phone, email);
    var marginTop = millimetersToPoints(MARGIN_TOP_MILLIMETERS);
    var xPos = getXPositionRightSideText(headerLines, page, FONT_SIZE_DEFAULT);
    var yPos = page.getMediaBox().getHeight() - marginTop;

    for (String line : headerLines) {
      drawText(contentStream, line, xPos, yPos, FONT_SIZE_DEFAULT);
      yPos -= DEFAULT_LINE_HEIGHT;
    }

    contentStream.close();
  }

  private void addReceiver(PDDocument document, Creditor creditor, UltimateDebtor ultimateDebtor) throws IOException {
    var firstPage = document.getPage(0);
    var contentStream = new PDPageContentStream(document, firstPage, PDPageContentStream.AppendMode.APPEND, false);

    var senderName = creditor.name();
    var senderStreet = String.format("%s %s", creditor.streetName(), creditor.streetNumber());
    var senderCity = String.format("%s %s", creditor.postalCode(), creditor.city());

    var sender = List.of(senderName, senderStreet, senderCity);
    var senderLine = String.join(", ", sender);

    var receiverName = ultimateDebtor.name();
    var receiverStreet = String.format("%s %s", ultimateDebtor.streetName(), ultimateDebtor.streetNumber());
    var receiverCity = String.format("%s %s", ultimateDebtor.postalCode(), ultimateDebtor.city());
    var receiverLines = List.of(receiverName, receiverStreet, receiverCity);

    var senderMarginTop = millimetersToPoints(RECEIVER_MARGIN_TOP_MILLIMETERS);
    var receiverMarginTop = millimetersToPoints(RECEIVER_MARGIN_TOP_MILLIMETERS + 10);
    var xPos = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var yPosReceiver = firstPage.getMediaBox().getHeight() - receiverMarginTop;
    var yPosSender = firstPage.getMediaBox().getHeight() - senderMarginTop;

    drawText(contentStream, senderLine, xPos, yPosSender, FONT_SIZE_SMALL);

    for (String line : receiverLines) {
      drawText(contentStream, line, xPos, yPosReceiver, FONT_SIZE_DEFAULT);
      yPosReceiver -= DEFAULT_LINE_HEIGHT;
    }

    contentStream.close();
  }

  private void addConditions(PDDocument document, Invoice invoice) throws IOException {
    var page = document.getPage(0);
    var pageHeight = page.getMediaBox().getHeight();
    var yPosInvoiceNumber = pageHeight - millimetersToPoints(RECEIVER_MARGIN_TOP_MILLIMETERS);
    var yPosInvoiceDate = yPosInvoiceNumber - DEFAULT_LINE_HEIGHT;
    var yPosDueDate = yPosInvoiceNumber - (DEFAULT_LINE_HEIGHT * 2);
    var yPosBillingPeriod = yPosInvoiceNumber - (DEFAULT_LINE_HEIGHT * 3);
    var yPosVatNumber = yPosInvoiceNumber - (DEFAULT_LINE_HEIGHT * 4);
    var pageWidth = page.getMediaBox().getWidth();
    var textWidthVatNumber = getTextWidth(invoice.vatNumber(), FONT_SIZE_DEFAULT); // Longest value
    var labelPlaceholderWidth = millimetersToPoints(30);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPosLabel = pageWidth - textWidthVatNumber - labelPlaceholderWidth - marginRight;

    addInvoiceNumber(document, invoice.reference(), xPosLabel, yPosInvoiceNumber);
    addInvoiceDate(document, invoice.invoiceDate(), xPosLabel, yPosInvoiceDate);
    addDueDate(document, invoice.dueDate(), xPosLabel, yPosDueDate);
    addBillingPeriod(document, invoice.periodFrom(), invoice.periodTo(), xPosLabel, yPosBillingPeriod);
    addVatNumber(document, invoice.vatNumber(), xPosLabel, yPosVatNumber);
  }

  private void addInvoiceNumber(PDDocument document, String invoiceNumber, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "Rechnungs-Nr.:";
    var pageWidth = page.getMediaBox().getWidth();
    var invoiceNumberWidth = getTextWidth(invoiceNumber, FONT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - invoiceNumberWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, FONT_SIZE_SMALL);
    drawText(contentStream, invoiceNumber, xPos, yPos, FONT_SIZE_SMALL);

    contentStream.close();
  }

  private void addInvoiceDate(PDDocument document, LocalDate invoiceDate, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "Rechnungsdatum:";
    var date = formatDate(invoiceDate);
    var pageWidth = page.getMediaBox().getWidth();
    var invoiceDateWidth = getTextWidth(date, FONT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - invoiceDateWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, FONT_SIZE_SMALL);
    drawText(contentStream, date, xPos, yPos, FONT_SIZE_SMALL);

    contentStream.close();
  }

  private void addDueDate(PDDocument document, LocalDate dueDate, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "Fälligkeitsdatum:";
    var date = formatDate(dueDate);
    var pageWidth = page.getMediaBox().getWidth();
    var dueDateWidth = getTextWidth(date, FONT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - dueDateWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, FONT_SIZE_SMALL);
    drawText(contentStream, date, xPos, yPos, FONT_SIZE_SMALL);

    contentStream.close();
  }

  private void addBillingPeriod(PDDocument document, LocalDate periodFrom, LocalDate periodTo, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "Abrechnungsperiode:";
    var dateFrom = formatDate(periodFrom);
    var dateTo = formatDate(periodTo);
    var billingPeriod = String.join(" - ", dateFrom, dateTo);
    var pageWidth = page.getMediaBox().getWidth();
    var dueDateWidth = getTextWidth(billingPeriod, FONT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - dueDateWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, FONT_SIZE_SMALL);
    drawText(contentStream, billingPeriod, xPos, yPos, FONT_SIZE_SMALL);

    contentStream.close();
  }

  private void addVatNumber(PDDocument document, String vatNumber, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "MWST-Nr.:";
    var pageWidth = page.getMediaBox().getWidth();
    var vatNumberWidth = getTextWidth(vatNumber, FONT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - vatNumberWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, FONT_SIZE_SMALL);
    drawText(contentStream, vatNumber, xPos, yPos, FONT_SIZE_SMALL);

    contentStream.close();
  }

  private void addTitle(PDDocument document, int pageNumber, String title) throws IOException {
    var page = document.getPage(pageNumber);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var marginTop = millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS);

    var xPos = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var yPos = page.getMediaBox().getHeight() - marginTop;

    drawTitle(contentStream, title, xPos, yPos);

    contentStream.close();
  }

  private void addInvoiceItems(PDDocument document, List<Item> items) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var pageHeight = page.getMediaBox().getHeight();

    var marginTopMillimeters = TITLE_MARGIN_TOP_MILLIMETERS + 10;
    var marginTop = millimetersToPoints(marginTopMillimeters);

    var yPos = pageHeight - marginTop;
    var xPosPosition = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var xPosDescription = xPosPosition + millimetersToPoints(POS_WIDTH_MILLIMETERS);
    var xPosQuantity = xPosDescription + millimetersToPoints(DESCRIPTION_WIDTH_MILLIMETERS);
    var xPosVat = xPosQuantity + millimetersToPoints(QUANTITY_WIDTH_MILLIMETERS);
    var xPosUnitPrice = xPosVat + millimetersToPoints(VAT_WIDTH_MILLIMETERS);
    var xPosAmount = xPosUnitPrice + millimetersToPoints(UNIT_PRICE_WIDTH_MILLIMETERS);

    var quantityDescription = "Anzahl";
    var vatDescription = "MwSt";
    var unitPriceDescription = "Einzelpreis";
    var amountDescription = "Gesamtpreis";

    var xPosQuantityRightAligned = xPosQuantity + millimetersToPoints(QUANTITY_WIDTH_MILLIMETERS) - getBoldTextWidth(quantityDescription,
      FONT_SIZE_DEFAULT);
    var xPosVatRightAligned = xPosVat + millimetersToPoints(VAT_WIDTH_MILLIMETERS) - getBoldTextWidth(vatDescription,
      FONT_SIZE_DEFAULT);
    var xPosUnitPriceRightAligned = xPosUnitPrice + millimetersToPoints(UNIT_PRICE_WIDTH_MILLIMETERS) - getBoldTextWidth(unitPriceDescription,
      FONT_SIZE_DEFAULT);
    var xPosAmountRightAligned = xPosAmount + millimetersToPoints(AMOUNT_WIDTH_MILLIMETERS) - getBoldTextWidth(amountDescription,
      FONT_SIZE_DEFAULT);

    drawTextBold(contentStream, "Pos.", xPosPosition, yPos, FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, "Bezeichnung", xPosDescription, yPos, FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, quantityDescription, xPosQuantityRightAligned, yPos, FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, vatDescription, xPosVatRightAligned, yPos, FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, unitPriceDescription, xPosUnitPriceRightAligned, yPos,
      FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, amountDescription, xPosAmountRightAligned, yPos, FONT_SIZE_DEFAULT);

    IntStream.range(0, items.size())
        .forEach(i -> {
          var item = items.get(i);
          var pos = i + 1;
          var yPosCurrent = yPos - (pos * ENTRY_HEIGHT);
          try {
            addPosition(document, page, String.format("%d", pos), xPosPosition, yPosCurrent);
            addDescription(document, page, item.description(), xPosDescription, yPosCurrent);
            addQuantity(document, page, item.quantity(), xPosQuantity, yPosCurrent);
            addVat(document, page, item.vat(), xPosVat, yPosCurrent);
            addUnitPrice(document, page, item.unitPrice(), xPosUnitPrice, yPosCurrent);
            addAmount(document, page, invoiceCalculatorService.calculateItemTotalAmount(item), xPosAmount, yPosCurrent, false);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    addSubTotal(document, items, xPosDescription, xPosAmount);
    addTotalVat(document, items, xPosDescription, xPosAmount);
    addRoundingDifference(document, items, xPosDescription, xPosAmount);
    addTotalAmount(document, items, xPosDescription, xPosAmount);

    contentStream.close();
  }

  private void addSubTotal(PDDocument document, List<Item> items, float xPosDescription, float xPosAmount)
    throws IOException {
    var page = document.getPage(0);

    var yPos = page.getMediaBox().getHeight() - millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS + 10);
    var subTotal = invoiceCalculatorService.calculateTotalAmountWithoutTax(items);
    var gap = 1.5f * ENTRY_HEIGHT;
    var yPosSubTotal = yPos - (items.size() * ENTRY_HEIGHT) - gap;

    addDescription(document, page, "Zwischensumme", xPosDescription, yPosSubTotal);
    addAmount(document, page, subTotal, xPosAmount, yPosSubTotal, false);
  }

  private void addTotalVat(PDDocument document, List<Item> items, float xPosDescription, float xPosAmount)
    throws IOException {
    var page = document.getPage(0);

    var yPos = page.getMediaBox().getHeight() - millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS + 10);
    var totalVat = invoiceCalculatorService.calculateTotalVat(items);
    var gap = 3f * ENTRY_HEIGHT;
    var yPosSubTotal = yPos - (items.size() * ENTRY_HEIGHT) - gap;

    addDescription(document, page, "Mehrwertsteuer 8.1%", xPosDescription, yPosSubTotal);
    addAmount(document, page, totalVat, xPosAmount, yPosSubTotal, false);
  }

  private void addRoundingDifference(PDDocument document, List<Item> items, float xPosDescription, float xPosAmount)
    throws IOException {
    var page = document.getPage(0);

    var yPos = page.getMediaBox().getHeight() - millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS + 10);
    var roundingDifference = invoiceCalculatorService.calculateRoundingDifference(items);
    var gap = 4f * ENTRY_HEIGHT;
    var yPosSubTotal = yPos - (items.size() * ENTRY_HEIGHT) - gap;

    addDescription(document, page, "Rundung", xPosDescription, yPosSubTotal);
    addAmount(document, page, roundingDifference, xPosAmount, yPosSubTotal, false);
  }

  private void addTotalAmount(PDDocument document, List<Item> items, float xPosDescription, float xPosAmount)
    throws IOException {
    var page = document.getPage(0);

    var yPos = page.getMediaBox().getHeight() - millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS + 10);
    var totalAmount = invoiceCalculatorService.calculateTotalAmount(items);
    var gap = 5.5f * ENTRY_HEIGHT;
    var yPosTotalAmount = yPos - (items.size() * ENTRY_HEIGHT) - gap;

    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    drawTextBold(contentStream, "Gesamtsumme", xPosDescription, yPosTotalAmount, FONT_SIZE_DEFAULT);

    contentStream.close();
    addAmount(document, page, totalAmount, xPosAmount, yPosTotalAmount, true);
  }

  private void addPosition(PDDocument document, PDPage page, String position, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    drawText(contentStream, position, xPos, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addDescription(PDDocument document, PDPage page, String description, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    drawText(contentStream, description, xPos, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addQuantity(PDDocument document, PDPage page, Double quantity, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMinimumFractionDigits(2);
    decimalFormat.setMaximumFractionDigits(2);

    var value = decimalFormat.format(quantity);
    var cellWidth = millimetersToPoints(QUANTITY_WIDTH_MILLIMETERS);
    var xPosValue = getXPosRightAlignedEntry(value, FONT_SIZE_DEFAULT, xPos, cellWidth, false);

    drawText(contentStream, decimalFormat.format(quantity), xPosValue, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addVat(PDDocument document, PDPage page, Double vat, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(2);

    var vatWithPercentage = String.join("", decimalFormat.format(vat), "%");
    var cellWidth = millimetersToPoints(VAT_WIDTH_MILLIMETERS);
    var xPosValue = getXPosRightAlignedEntry(vatWithPercentage, FONT_SIZE_DEFAULT, xPos, cellWidth, false);

    drawText(contentStream, vatWithPercentage, xPosValue, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addUnitPrice(PDDocument document, PDPage page, Double unitPrice, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMinimumFractionDigits(2);
    decimalFormat.setMaximumFractionDigits(2);

    var unitPriceWithCurrency = String.join(" ", "CHF", decimalFormat.format(unitPrice));
    var cellWidth = millimetersToPoints(UNIT_PRICE_WIDTH_MILLIMETERS);
    var xPosValue = getXPosRightAlignedEntry(unitPriceWithCurrency, FONT_SIZE_DEFAULT, xPos, cellWidth, false);

    drawText(contentStream, unitPriceWithCurrency, xPosValue, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addAmount(PDDocument document, PDPage page, Double amount, float xPos, float yPos, boolean isBold) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMinimumFractionDigits(2);
    decimalFormat.setMaximumFractionDigits(2);

    var amountWithCurrency = String.join(" ", "CHF", decimalFormat.format(amount));
    var cellWidth = millimetersToPoints(AMOUNT_WIDTH_MILLIMETERS);
    var xPosValue = getXPosRightAlignedEntry(amountWithCurrency, FONT_SIZE_DEFAULT, xPos, cellWidth, isBold);

    if (isBold) {
      drawTextBold(contentStream, amountWithCurrency, xPosValue, yPos, FONT_SIZE_DEFAULT);
    } else {
      drawText(contentStream, amountWithCurrency, xPosValue, yPos, FONT_SIZE_DEFAULT);
    }

    contentStream.close();
  }

  private float getXPositionRightSideText(List<String> lines, PDPage page, float fontSize) throws IOException {
    var longestLine = lines.stream().max(Comparator.comparingInt(String::length)).orElse("");
    var longestLineWidth = getTextWidth(longestLine, fontSize);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);

    return page.getMediaBox().getWidth() - longestLineWidth - marginRight;
  }

  private float getXPosRightAlignedEntry(String text, float fontSize, float xPosLabel, float cellWidth, boolean isBold) throws IOException {
    var textWidth = isBold ? getBoldTextWidth(text, fontSize) : getTextWidth(text, fontSize);
    var rightEndPoint = xPosLabel + cellWidth;

    return rightEndPoint - textWidth;
  }

  private float getTextWidth(String text, float fontSize) throws IOException {
    return PDType1Font.HELVETICA.getStringWidth(text) / 1000 * fontSize;
  }

  private float getBoldTextWidth(String text, float fontSize) throws IOException {
    return PDType1Font.HELVETICA_BOLD.getStringWidth(text) / 1000 * fontSize;
  }

  private String formatDate(LocalDate date) {
    var datePattern = "dd.MM.yyyy";
    var formatter = DateTimeFormatter.ofPattern(datePattern);

    return date.format(formatter);
  }

  private void drawTitle(PDPageContentStream contentStream, String title, float xPos, float yPos) throws IOException {
    contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_TEXT_SIZE);
    contentStream.beginText();
    contentStream.newLineAtOffset(xPos, yPos);
    contentStream.showText(title);
    contentStream.endText();
  }

  private void drawTextBold(PDPageContentStream contentStream, String text, float xPos, float yPos, float fontSize) throws IOException {
    contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
    contentStream.beginText();
    contentStream.newLineAtOffset(xPos, yPos);
    contentStream.showText(text);
    contentStream.endText();
  }

  private void drawText(PDPageContentStream contentStream, String text, float xPos, float yPos, float fontSize) throws IOException {
    var pattern = Pattern.compile(EMAIL_REGEX);
    var matcher = pattern.matcher(text);
    var isEmail = matcher.matches();

    if (isEmail) {
      contentStream.setNonStrokingColor(Color.BLUE);
    }

    contentStream.setFont(PDType1Font.HELVETICA, fontSize);
    contentStream.beginText();
    contentStream.newLineAtOffset(xPos, yPos);
    contentStream.showText(text);
    contentStream.endText();

    contentStream.setNonStrokingColor(Color.BLACK);
  }

  private float millimetersToPoints(int millimeters) {
    return millimeters * 72f / 25.4f;
  }
}
