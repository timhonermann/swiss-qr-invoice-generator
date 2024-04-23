package ch.timhonermann.service.generator.services;

import ch.timhonermann.service.generator.dtos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

  private static final int DEFAULT_IMAGE_HEIGHT = 94;

  private static final String EMAIL_REGEX = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

  private final InvoiceCalculatorService invoiceCalculatorService;

  private final ImageService imageService;

  @Autowired
  PdfService(InvoiceCalculatorService invoiceCalculatorService, ImageService imageService) {
    this.invoiceCalculatorService = invoiceCalculatorService;
    this.imageService = imageService;
  }

  public byte[] generatePdfInvoice(Invoice invoice) {
    var byteArrayOutputStream = new ByteArrayOutputStream();

    try (var document = initializePdf()) {
      addHeader(document, invoice.creditor());
      addReceiver(document, invoice.creditor(), invoice.ultimateDebtor());
      addTitle(document, invoice.title());
      addConditions(document, invoice);
      addInvoiceTable(document, invoice.items());

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

  private void addHeader(PDDocument document, Creditor creditor) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    var street = String.format("%s %s", creditor.streetName(), creditor.streetNumber());
    var city = String.format("%s %s", creditor.postalCode(), creditor.city());
    var phone = creditor.phone();
    var email = creditor.email();

    var headerLines = List.of(street, city, phone, email);
    var marginTop = millimetersToPoints(MARGIN_TOP_MILLIMETERS);
    var marginLeft = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var xPos = getXPositionRightSideText(headerLines, page, FONT_SIZE_DEFAULT);
    var yPos = page.getMediaBox().getHeight() - marginTop;

    for (String line : headerLines) {
      drawText(contentStream, line, xPos, yPos, FONT_SIZE_DEFAULT);
      yPos -= DEFAULT_LINE_HEIGHT;
    }

    var image = imageService.resizeImage(creditor.logoBase64(), DEFAULT_IMAGE_HEIGHT);
    var pdImage = LosslessFactory.createFromImage(document, image);

    contentStream.drawImage(pdImage, marginLeft, yPos);

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
    var pageWidth = page.getMediaBox().getWidth();

    // Get text width of longest value, which is the VAT number
    var textWidthVatNumber = getTextWidth(invoice.vatNumber(), FONT_SIZE_DEFAULT);
    var labelPlaceholderWidth = millimetersToPoints(30);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);

    var xPosLabel = pageWidth - textWidthVatNumber - labelPlaceholderWidth - marginRight;
    var yPosFirstLine = pageHeight - millimetersToPoints(RECEIVER_MARGIN_TOP_MILLIMETERS);
    var yPosSecondLine = yPosFirstLine - DEFAULT_LINE_HEIGHT;
    var yPosThirdLine = yPosFirstLine - (DEFAULT_LINE_HEIGHT * 2);
    var yPosFourthLine = yPosFirstLine - (DEFAULT_LINE_HEIGHT * 3);
    var yPosFifthLine = yPosFirstLine - (DEFAULT_LINE_HEIGHT * 4);

    addInvoiceNumber(document, invoice.reference(), xPosLabel, yPosFirstLine);
    addInvoiceDate(document, invoice.invoiceDate(), xPosLabel, yPosSecondLine);
    addDueDate(document, invoice.dueDate(), xPosLabel, yPosThirdLine);
    addBillingPeriod(document, invoice.periodFrom(), invoice.periodTo(), xPosLabel, yPosFourthLine);
    addVatNumber(document, invoice.vatNumber(), xPosLabel, yPosFifthLine);
  }

  private void addInvoiceNumber(PDDocument document, String invoiceNumber, float xPosLabel, float yPos) throws IOException {
    var label = "Rechnungs-Nr.:";

    addValueWithLabelRightAligned(document, label, invoiceNumber, xPosLabel, yPos);
  }

  private void addInvoiceDate(PDDocument document, LocalDate invoiceDate, float xPosLabel, float yPos) throws IOException {
    var label = "Rechnungsdatum:";
    var date = formatDate(invoiceDate);

    addValueWithLabelRightAligned(document, label, date, xPosLabel, yPos);
  }

  private void addDueDate(PDDocument document, LocalDate dueDate, float xPosLabel, float yPos) throws IOException {
    var label = "Fälligkeitsdatum:";
    var date = formatDate(dueDate);

    addValueWithLabelRightAligned(document, label, date, xPosLabel, yPos);
  }

  private void addBillingPeriod(PDDocument document, LocalDate periodFrom, LocalDate periodTo, float xPosLabel, float yPos) throws IOException {
    var label = "Abrechnungsperiode:";
    var dateFrom = formatDate(periodFrom);
    var dateTo = formatDate(periodTo);
    var billingPeriod = String.join(" - ", dateFrom, dateTo);

    addValueWithLabelRightAligned(document, label, billingPeriod, xPosLabel, yPos);
  }

  private void addVatNumber(PDDocument document, String vatNumber, float xPosLabel, float yPos) throws IOException {
    var label = "MWST-Nr.:";

    addValueWithLabelRightAligned(document, label, vatNumber, xPosLabel, yPos);
  }

  private void addValueWithLabelRightAligned(PDDocument document, String label, String value, float xPosLabel, float yPos) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    var pageWidth = page.getMediaBox().getWidth();
    var vatNumberWidth = getTextWidth(value, FONT_SIZE_SMALL);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);
    var xPosValue = pageWidth - vatNumberWidth - marginRight;

    drawText(contentStream, label, xPosLabel, yPos, FONT_SIZE_SMALL);
    drawText(contentStream, value, xPosValue, yPos, FONT_SIZE_SMALL);

    contentStream.close();
  }

  private void addTitle(PDDocument document, String title) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var marginTop = millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS);

    var xPos = millimetersToPoints(MARGIN_LEFT_MILLIMETERS);
    var yPos = page.getMediaBox().getHeight() - marginTop;

    drawTitle(contentStream, title, xPos, yPos);

    contentStream.close();
  }

  private InvoiceTableCoordinates getInvoiceTableCoordinates(PDDocument document) {
    var page = document.getPage(0);
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

    return new InvoiceTableCoordinates(
      yPos,
      xPosPosition,
      xPosDescription,
      xPosQuantity,
      xPosVat,
      xPosUnitPrice,
      xPosAmount
    );
  }

  private float getXPosRightAlignedCellValue(String value, int cellWidthMillimeters, float xPos, boolean isBold) throws IOException {
    var textWidth = isBold ? getBoldTextWidth(value, FONT_SIZE_DEFAULT) : getTextWidth(value, FONT_SIZE_DEFAULT);

    return xPos + millimetersToPoints(cellWidthMillimeters) - textWidth;
  }

  private void addInvoiceTable(PDDocument document, List<Item> items) throws IOException {
    addInvoiceTableHeader(document);
    addInvoiceTableItems(document, items);
  }

  private void addInvoiceTableItems(PDDocument document, List<Item> items) throws IOException {
    var page = document.getPage(0);
    var coordinates = getInvoiceTableCoordinates(document);

    IntStream.range(0, items.size())
      .forEach(i -> {
        var item = items.get(i);
        var pos = i + 1;
        var yPosCurrent = coordinates.yPos() - (pos * ENTRY_HEIGHT);
        try {
          addPosition(document, page, String.format("%d", pos), coordinates.xPosPosition(), yPosCurrent);
          addDescription(document, page, item.description(), coordinates.xPosDescription(), yPosCurrent);
          addQuantity(document, page, item.quantity(), coordinates.xPosQuantity(), yPosCurrent);
          addVat(document, page, item.vat(), coordinates.xPosVat(), yPosCurrent);
          addUnitPrice(document, page, item.unitPrice(), coordinates.xPosUnitPrice(), yPosCurrent);
          addAmount(document, page, invoiceCalculatorService.calculateItemTotalAmount(item), invoiceCalculatorService.calculateTotalAmount(items), coordinates.xPosAmount(), yPosCurrent, false);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

    addSubTotal(document, items, coordinates.xPosDescription(), coordinates.xPosAmount());
    addTotalVat(document, items, coordinates.xPosDescription(), coordinates.xPosAmount());
    addRoundingDifference(document, items, coordinates.xPosDescription(), coordinates.xPosAmount());
    addTotalAmount(document, items, coordinates.xPosDescription(), coordinates.xPosAmount());
  }

  private void addInvoiceTableHeader(PDDocument document) throws IOException {
    var page = document.getPage(0);
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var coordinates = getInvoiceTableCoordinates(document);

    var positionLabel = "Pos.";
    var descriptionLabel = "Bezeichnung";
    var quantityLabel = "Anzahl";
    var vatLabel = "MwSt";
    var unitPriceLabel = "Einzelpreis";
    var amountLabel = "Gesamtpreis";

    var xPosQuantityRightAligned = getXPosRightAlignedCellValue(
      quantityLabel,
      QUANTITY_WIDTH_MILLIMETERS,
      coordinates.xPosQuantity(),
      true
    );
    var xPosVatRightAligned = getXPosRightAlignedCellValue(
      vatLabel,
      VAT_WIDTH_MILLIMETERS,
      coordinates.xPosVat(),
      true
    );
    var xPosUnitPriceRightAligned = getXPosRightAlignedCellValue(
      unitPriceLabel,
      UNIT_PRICE_WIDTH_MILLIMETERS,
      coordinates.xPosUnitPrice(),
      true
    );
    var xPosAmountRightAligned = getXPosRightAlignedCellValue(
      amountLabel,
      AMOUNT_WIDTH_MILLIMETERS,
      coordinates.xPosAmount(),
      true
    );

    drawTextBold(contentStream, positionLabel, coordinates.xPosPosition(), coordinates.yPos(), FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, descriptionLabel, coordinates.xPosDescription(), coordinates.yPos(), FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, quantityLabel, xPosQuantityRightAligned, coordinates.yPos(), FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, vatLabel, xPosVatRightAligned, coordinates.yPos(), FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, unitPriceLabel, xPosUnitPriceRightAligned, coordinates.yPos(), FONT_SIZE_DEFAULT);
    drawTextBold(contentStream, amountLabel, xPosAmountRightAligned, coordinates.yPos(), FONT_SIZE_DEFAULT);

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
    addAmount(document, page, subTotal, invoiceCalculatorService.calculateTotalAmount(items), xPosAmount, yPosSubTotal, false);
  }

  private void addTotalVat(PDDocument document, List<Item> items, float xPosDescription, float xPosAmount)
    throws IOException {
    var page = document.getPage(0);

    var yPos = page.getMediaBox().getHeight() - millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS + 10);
    var totalVat = invoiceCalculatorService.calculateTotalVat(items);
    var gap = 3f * ENTRY_HEIGHT;
    var yPosSubTotal = yPos - (items.size() * ENTRY_HEIGHT) - gap;

    addDescription(document, page, "Mehrwertsteuer 8.1%", xPosDescription, yPosSubTotal);
    addAmount(document, page, totalVat, invoiceCalculatorService.calculateTotalAmount(items), xPosAmount, yPosSubTotal, false);
  }

  private void addRoundingDifference(PDDocument document, List<Item> items, float xPosDescription, float xPosAmount)
    throws IOException {
    var page = document.getPage(0);

    var yPos = page.getMediaBox().getHeight() - millimetersToPoints(TITLE_MARGIN_TOP_MILLIMETERS + 10);
    var roundingDifference = invoiceCalculatorService.calculateRoundingDifference(items);
    var gap = 4f * ENTRY_HEIGHT;
    var yPosSubTotal = yPos - (items.size() * ENTRY_HEIGHT) - gap;

    addDescription(document, page, "Rundung", xPosDescription, yPosSubTotal);
    addAmount(document, page, roundingDifference, invoiceCalculatorService.calculateTotalAmount(items), xPosAmount, yPosSubTotal, false);
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
    addAmount(document, page, totalAmount, invoiceCalculatorService.calculateTotalAmount(items), xPosAmount, yPosTotalAmount, true);
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

    var value = formatAmount(quantity);
    var xPosValue = getXPosRightAlignedCellValue(value, QUANTITY_WIDTH_MILLIMETERS, xPos, false);

    drawText(contentStream, value, xPosValue, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addVat(PDDocument document, PDPage page, Double vat, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(1);

    var vatWithPercentage = String.join("", decimalFormat.format(vat), "%");
    var xPosValue = getXPosRightAlignedCellValue(vatWithPercentage, VAT_WIDTH_MILLIMETERS, xPos, false);

    drawText(contentStream, vatWithPercentage, xPosValue, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addUnitPrice(PDDocument document, PDPage page, Double unitPrice, float xPos, float yPos) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);

    var unitPriceWithCurrency = String.join(" ", "CHF", formatAmount(unitPrice));
    var xPosValue = getXPosRightAlignedCellValue(unitPriceWithCurrency, UNIT_PRICE_WIDTH_MILLIMETERS, xPos, false);

    drawText(contentStream, unitPriceWithCurrency, xPosValue, yPos, FONT_SIZE_DEFAULT);

    contentStream.close();
  }

  private void addAmount(PDDocument document, PDPage page, Double amount, Double totalAmount, float xPos, float yPos, boolean isBold) throws IOException {
    var contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
    var currency = "CHF";
    // Get text width of total amount to set currency
    var totalAmountWithCurrency = String.join(" ", "CHF", formatAmount(totalAmount));
    var formattedAmount = formatAmount(amount);

    var xPosCurrency = getXPosRightAlignedCellValue(totalAmountWithCurrency, AMOUNT_WIDTH_MILLIMETERS, xPos, true);

    var xPosValue = getXPosRightAlignedCellValue(formattedAmount, AMOUNT_WIDTH_MILLIMETERS, xPos, isBold);

    if (isBold) {
      drawTextBold(contentStream, currency, xPosCurrency, yPos, FONT_SIZE_DEFAULT);
      drawTextBold(contentStream, formattedAmount, xPosValue, yPos, FONT_SIZE_DEFAULT);
    } else {
      drawText(contentStream, currency, xPosCurrency, yPos, FONT_SIZE_DEFAULT);
      drawText(contentStream, formattedAmount, xPosValue, yPos, FONT_SIZE_DEFAULT);
    }

    contentStream.close();
  }

  private float getXPositionRightSideText(List<String> lines, PDPage page, float fontSize) throws IOException {
    var longestLine = lines.stream().max(Comparator.comparingInt(String::length)).orElse("");
    var longestLineWidth = getTextWidth(longestLine, fontSize);
    var marginRight = millimetersToPoints(MARGIN_RIGHT_MILLIMETERS);

    return page.getMediaBox().getWidth() - longestLineWidth - marginRight;
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

  private String formatAmount(Double amount) {
    var formatter = new DecimalFormat("#,##0.00");
    var symbols = new DecimalFormatSymbols();
    symbols.setDecimalSeparator('.');
    symbols.setGroupingSeparator('\'');
    formatter.setDecimalFormatSymbols(symbols);

    return formatter.format(amount);
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
