package ch.timhonermann.service.generator.dtos;

public record Creditor(
  String iban,
  String name,
  String streetName,
  String streetNumber,
  String postalCode,
  String city,
  String country,
  String phone,
  String email,
  String logoBase64
) {
}
