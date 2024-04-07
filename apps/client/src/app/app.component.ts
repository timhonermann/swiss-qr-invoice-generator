import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: true,
  imports: [RouterModule],
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  private readonly httpClient = inject(HttpClient);

  generatePdf(): void {
    this.httpClient
      .post(
        'http://localhost:8080/api/v1/generator/pdf',
        {},
        {
          observe: 'body',
          responseType: 'blob',
        }
      )
      .subscribe((response) => {
        const fileUrl = URL.createObjectURL(response);

        const a = document.createElement('a');

        a.href = fileUrl;
        a.download = 'pdfFile.pdf';
        a.click();
        a.remove();
      });
  }

  generate(): void {
    const invoice = {
      title: 'Rechnung April',
      invoiceDate: new Date('2024-04-30'),
      dueDate: new Date('2024-05-31'),
      vatNumber: 'CHE-321.439.745 MWST',
      periodFrom: new Date('2024-04-01'),
      periodTo: new Date('2024-04-30'),
      reference: '20240401',
      creditor: {
        iban: 'CH76 0483 5187 2391 0100 0',
        name: 'Hontech GmbH',
        streetName: 'Dornimatte',
        streetNumber: '9',
        postalCode: '6047',
        city: 'Kastanienbaum',
        country: 'CH',
        phone: '+41 79 887 81 96',
        email: 'info@honte.ch',
      },
      ultimateDebtor: {
        name: 'PEAX AG',
        streetName: 'Pilatusstrasse',
        streetNumber: '28',
        postalCode: '6003',
        city: 'Luzern',
        country: 'CH',
      },
      items: [
        {
          description: 'Arbeitsstunden',
          quantity: 104,
          vat: 8.1,
          unitPrice: 120.5,
        },
        {
          description: 'Anderes',
          quantity: 3.5,
          vat: 8.1,
          unitPrice: 150,
        },
      ],
    };

    this.httpClient
      .post('http://localhost:8080/api/v1/generator', invoice, {
        observe: 'body',
        responseType: 'blob',
      })
      .subscribe((response) => {
        const fileUrl = URL.createObjectURL(response);

        const a = document.createElement('a');

        a.href = fileUrl;
        a.download = 'qrInvoice.pdf';
        a.click();
        a.remove();
      });
  }
}
