import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { WizardService } from '@swiss-qr-invoice-generator/shared/ui/wizard';
import { exhaustMap, filter, take } from 'rxjs';
import { Invoice } from '../../models/generator.models';
import { InvoiceItemsStepComponent } from '../../presentationals/invoice-items-step/invoice-items-step.component';
import { InvoiceInformationStepComponent } from '../../presentationals/invoice-information-step/invoice-information-step.component';
import { CreditorStepComponent } from '../../presentationals/creditor-step/creditor-step.component';
import { DebtorStepComponent } from '../../presentationals/debtor-step/debtor-step.component';
import { GeneratorService } from '../../services/generator.service';

@Component({
  selector: 'sqig-generator',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    ButtonComponent,
  ],
  templateUrl: './generator.component.html',
  styleUrl: './generator.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GeneratorComponent {
  private readonly wizardService = inject(WizardService);

  private readonly generatorService = inject(GeneratorService);

  openWizard(): void {
    this.wizardService
      .open<Invoice>({
        title: 'Rechnung erstellen',
        steps: [
          InvoiceInformationStepComponent,
          CreditorStepComponent,
          DebtorStepComponent,
          InvoiceItemsStepComponent,
        ],
        data: this.generateEmptyInvoice(),
      })
      .afterClosed()
      .pipe(
        take(1),
        filter(Boolean),
        exhaustMap((invoice) => this.generatorService.generateInvoice(invoice))
      )
      .subscribe((response) => {
        const fileUrl = URL.createObjectURL(response);

        const a = document.createElement('a');

        a.href = fileUrl;
        a.download = 'qrInvoice.pdf';
        a.click();
        a.remove();
      });
  }

  private generateEmptyInvoice(): Invoice {
    return {
      title: '',
      vatNumber: '',
      reference: '',
      invoiceDate: new Date(),
      dueDate: new Date(),
      periodFrom: new Date(),
      periodTo: new Date(),
      creditor: {
        iban: '',
        name: '',
        streetName: '',
        streetNumber: '',
        postalCode: '',
        city: '',
        country: 'CH',
        phone: '',
        email: '',
      },
      ultimateDebtor: {
        name: '',
        streetName: '',
        streetNumber: '',
        postalCode: '',
        city: '',
        country: 'CH',
      },
      items: [],
    };
  }
}
