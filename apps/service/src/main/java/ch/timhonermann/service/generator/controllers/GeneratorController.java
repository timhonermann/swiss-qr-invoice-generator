package ch.timhonermann.service.generator.controllers;

import ch.timhonermann.service.generator.dtos.Invoice;
import ch.timhonermann.service.generator.services.GeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/generator")
public class GeneratorController {

  private final GeneratorService generatorService;

  @Autowired
  public GeneratorController(GeneratorService generatorService) {
    this.generatorService = generatorService;
  }

  @PostMapping
  public ResponseEntity<byte[]> generate(@RequestBody Invoice invoice) {
    var qrInvoice = generatorService.createInvoice(invoice);

    var uri = ServletUriComponentsBuilder
      .fromCurrentRequestUri()
      .path("/{id}")
      .build()
      .toUri();

    return ResponseEntity.created(uri).body(qrInvoice);
  }
}
