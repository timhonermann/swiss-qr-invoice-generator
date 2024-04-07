package ch.timhonermann.service.generator.services;

import java.util.List;

import ch.timhonermann.service.generator.dtos.Item;
import org.springframework.stereotype.Service;

@Service
public class InvoiceCalculatorService {
  public double calculateTotalAmount(List<Item> items) {
    var totalAmount = calculateTotalAmountRaw(items);

    return roundToFloor5(totalAmount);
  }

  public double calculateTotalAmountWithoutTax(List<Item> items) {
    return items.stream().mapToDouble(i -> i.quantity() * i.unitPrice()).sum();
  }

  public double calculateTotalVat(List<Item> items) {
    var totalAmount = calculateTotalAmountRaw(items);
    var totalAmountWithoutTax = calculateTotalAmountWithoutTax(items);

    return totalAmount - totalAmountWithoutTax;
  }

  public double calculateRoundingDifference(List<Item> items) {
    var totalAmountRaw = calculateTotalAmountRaw(items);
    var totalAmount = calculateTotalAmount(items);
    var difference = totalAmountRaw - totalAmount;

    return roundToTwoDecimal(difference);
  }

  public double calculateItemTotalAmount(Item item) {
    return roundToTwoDecimal(item.unitPrice() * (double)item.quantity());
  }

  public double roundToTwoDecimal(double value) {
    return Math.round(value * 100) / 100d;
  }

  private double calculateTotalAmountRaw(List<Item> items) {
    return items.stream().mapToDouble(i -> i.quantity() * i.unitPrice() * getVatMultiplier(i.vat())).sum();
  }

  private double getVatMultiplier(double vat) {
    var vatDecimal = vat / 100.0;

    return 1.0 + vatDecimal;
  }

  private double roundToFloor5(double value) {
    return Math.floor(value * 20) / 20d;
  }
}
