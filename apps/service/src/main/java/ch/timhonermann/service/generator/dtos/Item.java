package ch.timhonermann.service.generator.dtos;

public record Item(
  String description,
  Double quantity,
  Double unitPrice,
  Double vat
) {
}
