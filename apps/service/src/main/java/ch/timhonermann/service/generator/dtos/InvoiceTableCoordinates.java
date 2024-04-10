package ch.timhonermann.service.generator.dtos;

public record InvoiceTableCoordinates(
  float yPos,
  float xPosPosition,
  float xPosDescription,
  float xPosQuantity,
  float xPosVat,
  float xPosUnitPrice,
  float xPosAmount
) {
}
