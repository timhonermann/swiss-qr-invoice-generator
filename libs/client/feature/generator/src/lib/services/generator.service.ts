import { inject, Injectable } from '@angular/core';
import { HttpService } from '@swiss-qr-invoice-generator/shared/common/http';
import { Invoice } from '../models/generator.models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GeneratorService {
  private readonly httpService = inject(HttpService);

  generateInvoice(invoice: Invoice): Observable<Blob> {
    return this.httpService.post(
      'http://localhost:8080/api/v1/generator',
      invoice,
      {
        observe: 'body',
        responseType: 'blob',
      }
    );
  }
}
