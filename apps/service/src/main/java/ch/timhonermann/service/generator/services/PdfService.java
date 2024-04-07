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
  private static final int A4_WIDTH_MILLIMETERS = 210;
  private static final int MARGIN_LEFT_MILLIMETERS = 30;
  private static final int MARGIN_RIGHT_MILLIMETERS = 10;
  private static final int MARGIN_TOP_MILLIMETERS = 10;
  private static final int TITLE_MARGIN_TOP_MILLIMETERS = 105;
  private static final float TEXT_SIZE_DEFAULT = 11f;
  private static final float TEXT_SIZE_SMALL = 8f;
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
    var xPos = getXPositionRightSideText(headerLines, page, TEXT_SIZE_DEFAULT);
    var yPos = page.getMediaBox().getHeight() - marginTop;

    for (String line : headerLines) {
      drawText(contentStream, line, xPos, yPos, TEXT_SIZE_DEFAULT);
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

    var senderMarginTop = millimetersToPoints(50);
    var receiverMarginTop = millimetersToPoints(60);
    var xPos = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var yPosReceiver = firstPage.getMediaBox().getHeight() - receiverMarginTop;
    var yPosSender = firstPage.getMediaBox().getHeight() - senderMarginTop;

    drawText(contentStream, senderLine, xPos, yPosSender, TEXT_SIZE_SMALL);

    for (String line : receiverLines) {
      drawText(contentStream, line, xPos, yPosReceiver, TEXT_SIZE_DEFAULT);
      yPosReceiver -= DEFAULT_LINE_HEIGHT;
    }

    contentStream.close();
  }

  private void addConditions(PDDocument document, Invoice invoice) throws IOException {
    var page = document.getPage(0);
    var pageHeight = page.getMediaBox().getHeight();
    var yPosInvoiceNumber = pageHeight - millimetersToPoints(75);
    var yPosInvoiceDate = yPosInvoiceNumber - DEFAULT_LINE_HEIGHT;
    var yPosDueDate = yPosInvoiceNumber - (DEFAULT_LINE_HEIGHT * 2);
    var yPosBillingPeriod = yPosInvoiceNumber - (DEFAULT_LINE_HEIGHT * 3);
    var yPosVatNumber = yPosInvoiceNumber - (DEFAULT_LINE_HEIGHT * 4);
    var pageWidth = page.getMediaBox().getWidth();
    var textWidthVatNumber = getTextWidth(invoice.vatNumber(), TEXT_SIZE_SMALL); // Longest value
    var labelPlaceholderWidth = millimetersToPoints(35);
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
    var invoiceNumberWidth = getTextWidth(invoiceNumber, TEXT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - invoiceNumberWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, TEXT_SIZE_SMALL);
    drawText(contentStream, invoiceNumber, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addInvoiceDate(PDDocument document, LocalDate invoiceDate, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "Rechnungsdatum:";
    var date = formatDate(invoiceDate);
    var pageWidth = page.getMediaBox().getWidth();
    var invoiceDateWidth = getTextWidth(date, TEXT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - invoiceDateWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, TEXT_SIZE_SMALL);
    drawText(contentStream, date, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addDueDate(PDDocument document, LocalDate dueDate, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "Fälligkeitsdatum:";
    var date = formatDate(dueDate);
    var pageWidth = page.getMediaBox().getWidth();
    var dueDateWidth = getTextWidth(date, TEXT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - dueDateWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, TEXT_SIZE_SMALL);
    drawText(contentStream, date, xPos, yPos, TEXT_SIZE_SMALL);

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
    var dueDateWidth = getTextWidth(billingPeriod, TEXT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - dueDateWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, TEXT_SIZE_SMALL);
    drawText(contentStream, billingPeriod, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addVatNumber(PDDocument document, String vatNumber, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var label = "MWST-Nr.:";
    var pageWidth = page.getMediaBox().getWidth();
    var vatNumberWidth = getTextWidth(vatNumber, TEXT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPos = pageWidth - vatNumberWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, TEXT_SIZE_SMALL);
    drawText(contentStream, vatNumber, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addTitle(PDDocument document, int pageNumber, String title) throws IOException {
    var page = document.getPage(pageNumber);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var marginTop = millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS);

    var xPos = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var yPos = page.getMediaBox().getHeight() - marginTop;

    drawTitle(contentStream, title, xPos, yPos);

    var width = page.getMediaBox().getWidth() - millimetersToPoints(MARGIN_RIGHT_MILLIMETERS) - millimetersToPoints(MARGIN_LEFT_MILLIMETERS);

    System.out.println(width);

    contentStream.close();
  }

  private void addInvoiceItems(PDDocument document, List<Item> items) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var pageHeight = page.getMediaBox().getHeight();
    var pageWidth = page.getMediaBox().getWidth();

    var marginTopMillimeters = TITLE_MARGIN_TOP_MILLIMETERS + 15;
    var marginTop = millimetersToPoints(marginTopMillimeters);

    //    var headerContent = List.of("Pos.", "Bezeichnung", "Anzahl", "MwSt", "Einzelpreis", "Gesamtpreis");

    var posWidthMillimeters = 15;
    var descriptionWidthMillimeters = 75;
    var quantityWidthMillimeters = 15;
    var vatWidthMillimeters = 15;
    var unitPriceWidthMillimeters = 25;
    var amountWidthMillimeters = 25;

    var yPos = pageHeight - marginTop;
    var xPosPosition = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var xPosDescription = xPosPosition + millimetersToPoints(posWidthMillimeters);
    var xPosQuantity = xPosDescription + millimetersToPoints(descriptionWidthMillimeters);
    var xPosVat = xPosQuantity + millimetersToPoints(quantityWidthMillimeters);
    var xPosUnitPrice = xPosVat + millimetersToPoints(vatWidthMillimeters);
    var xPosAmount = xPosUnitPrice + millimetersToPoints(unitPriceWidthMillimeters);

    drawTextBold(contentStream, "Pos.", xPosPosition, yPos, TEXT_SIZE_SMALL);
    drawTextBold(contentStream, "Bezeichnung", xPosDescription, yPos, TEXT_SIZE_SMALL);
    drawTextBold(contentStream, "Anzahl", xPosQuantity, yPos, TEXT_SIZE_SMALL);
    drawTextBold(contentStream, "MwSt", xPosVat, yPos, TEXT_SIZE_SMALL);
    drawTextBold(contentStream, "Einzelpreis", xPosUnitPrice, yPos, TEXT_SIZE_SMALL);
    drawTextBold(contentStream, "Gesamtpreis", xPosAmount, yPos, TEXT_SIZE_SMALL);

    IntStream.range(0, items.size())
        .forEach(i -> {
          var item = items.get(i);
          var pos = i + 1;
          var entryHeight = 20;
          var yPosCurrent = yPos - (pos * entryHeight);
          try {
            addPosition(document, page, String.format("%d", pos), xPosPosition, yPosCurrent);
            addDescription(document, page, item.description(), xPosDescription, yPosCurrent);
            addQuantity(document, page, item.quantity(), xPosQuantity, yPosCurrent);
            addVat(document, page, item.vat(), xPosVat, yPosCurrent);
            addUnitPrice(document, page, item.unitPrice(), xPosUnitPrice, yPosCurrent);
            addAmount(document, page, invoiceCalculatorService.calculateItemTotalAmount(item), xPosAmount, yPosCurrent);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });


    contentStream.close();

//    var entryHeight = 20;
//    var tableWidthMillimeters = A4_WIDTH_MILLIMETERS - MARGIN_LEFT_MILLIMETERS - MARGIN_RIGHT_MILLIMETERS;
//    var tableWidth = millimetersToPoints(tableWidthMillimeters);

//    var decimalFormat = new DecimalFormat();
//    decimalFormat.setMaximumFractionDigits(2);
  }

  private void addPosition(PDDocument document, PDPage page, String position, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    drawText(contentStream, position, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addDescription(PDDocument document, PDPage page, String description, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    drawText(contentStream, description, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addQuantity(PDDocument document, PDPage page, Double quantity, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(1);

    drawText(contentStream, decimalFormat.format(quantity), xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addVat(PDDocument document, PDPage page, Double vat, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(2);

    var vatWithPercentage = String.join("", decimalFormat.format(vat), "%");

    drawText(contentStream, vatWithPercentage, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addUnitPrice(PDDocument document, PDPage page, Double unitPrice, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMinimumFractionDigits(2);

    var unitPriceWithCurrency = String.join(" ", "CHF", decimalFormat.format(unitPrice));

    drawText(contentStream, unitPriceWithCurrency, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private void addAmount(PDDocument document, PDPage page, Double amount, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMinimumFractionDigits(2);

    var amountWithCurrency = String.join(" ", "CHF", decimalFormat.format(amount));

    drawText(contentStream, amountWithCurrency, xPos, yPos, TEXT_SIZE_SMALL);

    contentStream.close();
  }

  private float getXPositionRightSideText(List<String> lines, PDPage page, float fontSize) throws IOException {
    var longestLine = lines.stream().max(Comparator.comparingInt(String::length)).orElse("");
    var longestLineWidth = getTextWidth(longestLine, fontSize);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);

    return page.getMediaBox().getWidth() - longestLineWidth - marginRight;
  }

  private float getXPositionRightAlignedText(String text, PDPage page, float fontSize, int marginRightMillimeters) throws IOException {
    var textWidth = getTextWidth(text, fontSize);
    var marginRightPoints = millimetersToPoints(marginRightMillimeters);
    var pageWidth = page.getMediaBox().getWidth();

    return pageWidth - textWidth - marginRightPoints;
  }

  private float getTextWidth(String text, float fontSize) throws IOException {
    return PDType1Font.HELVETICA.getStringWidth(text) / 1000 * fontSize;
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


//  private void addItemTable(List<Item> items, PDDocument document) throws IOException {
//    var page = document.getPage(0);
//    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);
//    var decimalFormat = new DecimalFormat();
//    decimalFormat.setMaximumFractionDigits(2);
//
//    float width = page.getMediaBox().getWidth() - 2 * MARGIN;
//    float yStart = 400f;
//    float tableWidth = width;
//    float yPosition = yStart;
//    float rowHeight = 20f;
//    float cellMargin = 5f;
//
//    var headerContent = List.of("Pos.", "Bezeichnung", "Anzahl", "MwSt", "Einzelpreis", "Gesamtpreis");
//
//    int numOfCols = headerContent.size();
//    float colWidth = tableWidth / numOfCols;
//
//    float tableHeight = rowHeight * (items.size() + 1);
//
//    // Draw header
//    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
//    for (int i = 0; i < numOfCols; i++) {
//      contentStream.beginText();
//      contentStream.newLineAtOffset(MARGIN + i * colWidth + cellMargin, yStart - 15);
//      contentStream.showText(headerContent.get(i));
//      contentStream.endText();
//    }
//
//    // Draw table content
//    contentStream.setFont(PDType1Font.HELVETICA, 12);
//
//    var xPos = MARGIN + 5f;
//    var yPos = 360f;
//
//    for (int i = 0; i < items.size(); i++) {
//      var item = items.get(i);
//      var pos = i + 1;
//
//      contentStream.beginText();
//
//      contentStream.newLineAtOffset(xPos, yPos);
//      contentStream.showText(String.format("%d", pos));
//
//      contentStream.newLineAtOffset(colWidth, 0);
//      contentStream.showText(item.description());
//
//      contentStream.newLineAtOffset(colWidth, 0);
//      contentStream.showText(String.format("%d", item.quantity()));
//
//      contentStream.newLineAtOffset(colWidth, 0);
//      contentStream.showText(decimalFormat.format(item.vat()));
//
//      contentStream.newLineAtOffset(colWidth, 0);
//      contentStream.showText(decimalFormat.format(item.unitPrice()));
//
//      contentStream.newLineAtOffset(colWidth, 0);
//      contentStream.showText(decimalFormat.format(invoiceCalculatorService.calculateItemTotalAmount(item)));
//
//      contentStream.endText();
//
//      yPosition += rowHeight;
//      yPos -= rowHeight;
//    }
//
//    // Draw table borders
//    contentStream.moveTo(MARGIN, yStart - yPosition);
//    contentStream.lineTo(MARGIN + tableWidth, yStart - yPosition);
//    for (int i = 0; i <= numOfCols; i++) {
//      contentStream.moveTo(MARGIN + i * colWidth, yStart);
//      contentStream.lineTo(MARGIN + i * colWidth, yStart - tableHeight);
//    }
//    contentStream.stroke();
//    contentStream.close();
//  }
}
