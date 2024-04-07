package ch.timhonermann.service.generator.dtos;

import java.time.LocalDate;
import java.util.List;

public record Invoice(
  String title,
  LocalDate invoiceDate,
  LocalDate dueDate,
  LocalDate periodFrom,
  LocalDate periodTo,
  String vatNumber,
  Creditor creditor,
  UltimateDebtor ultimateDebtor,
  String reference,
  List<Item> items
) { }
