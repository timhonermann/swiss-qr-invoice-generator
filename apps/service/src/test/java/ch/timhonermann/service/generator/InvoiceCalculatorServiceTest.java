package ch.timhonermann.service.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InvoiceCalculatorServiceTest {
  @InjectMocks
  InvoiceCalculatorService testee;

  @Test
  void shouldReturnTotalAmountIncludingTax() {
    // Arrange
    var items = List.of(
      new Item("Arbeitsstunden", 100, 100d, 8.1),
      new Item("Anderes", 3, 150d, 8.1)
    );
    var expectedTotalAmount = 11296.45;

    // Act
    var result = testee.calculateTotalAmount(items);

    // Assert
    assertEquals(expectedTotalAmount, result);
  }

  @Test
  void shouldReturnDownTotalAmountIncludingTax() {
    // Arrange
    var items = List.of(
      new Item("Arbeitsstunden", 104, 120d, 8.1),
      new Item("Anderes", 3, 150d, 8.1)
    );
    var expectedTotalAmount = 13977.30;

    // Act
    var result = testee.calculateTotalAmount(items);

    // Assert
    assertEquals(expectedTotalAmount, result);
  }

  @Test
  void shouldCalculateTotalAmountWithoutTax() {
    // Arrange
    var items = List.of(
      new Item("Arbeitsstunden", 104, 120d, 8.1),
      new Item("Anderes", 3, 150d, 8.1)
    );
    var expectedTotalAmount = 12930.00;

    // Act
    var result = testee.calculateTotalAmountWithoutTax(items);

    // Assert
    assertEquals(expectedTotalAmount, result);
  }

  @Test
  void shouldCalculateTotalVat() {
    // Arrange
    var items = List.of(
      new Item("Arbeitsstunden", 104, 120d, 8.1),
      new Item("Anderes", 3, 150d, 8.1)
    );
    var expectedTotalVat = 1047.33;

    // Act
    var result = testee.calculateTotalVat(items);

    // Assert
    assertEquals(expectedTotalVat, result);
  }

  @Test
  void shouldCalculateRoundingDifference() {
    // Arrange
    var items = List.of(
      new Item("Arbeitsstunden", 104, 120d, 8.1),
      new Item("Anderes", 3, 150d, 8.1)
    );
    var expectedRoundingDifference = .03;

    // Act
    var result = testee.calculateRoundingDifference(items);

    // Assert
    assertEquals(expectedRoundingDifference, result);
  }

  @Test
  void shouldCalculateTotalAmountOfSingleItem() {
    // Arrange
    var item = new Item("Arbeitsstunden", 104, 120d, 8.1);
    var expectedTotalAmount = 12480.00;

    // Act
    var result = testee.calculateItemTotalAmount(item);

    // Assert
    assertEquals(expectedTotalAmount, result);
  }
}
