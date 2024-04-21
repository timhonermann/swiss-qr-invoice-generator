import { GeneratorService } from './generator.service';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { provideMock } from '@swiss-qr-invoice-generator/shared/testing';
import { HttpService } from '@swiss-qr-invoice-generator/shared/common/http';
import { Invoice } from '../models/generator.models';
import { of } from 'rxjs';

describe('GeneratorService', () => {
  let service: GeneratorService;
  let httpService: HttpService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideMock(HttpService)],
    });

    service = TestBed.inject(GeneratorService);
    httpService = TestBed.inject(HttpService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  describe('generateInvoice', () => {
    it('should call HttpService post with needed parameters', waitForAsync(() => {
      // arrange
      const invoice: Invoice = {
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
      const blob = new Blob(['Test']);
      const postSpy = jest.spyOn(httpService, 'post').mockReturnValue(of(blob));

      // act
      const result$ = service.generateInvoice(invoice);

      // assert
      result$.subscribe((result) => {
        expect(result).toEqual(blob);
        expect(postSpy).toHaveBeenCalledWith(expect.any, {
          observe: 'body',
          responseType: 'blob',
        });
      });
    }));
  });
});
