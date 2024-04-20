import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { WizardService } from '@swiss-qr-invoice-generator/shared/ui/wizard';
import { take, tap } from 'rxjs';
import { Invoice } from '../../models/generator.models';
import { CreditorStepComponent } from '../../presentationals/creditor-step/creditor-step.component';
import { CreditorComponent } from '../../presentationals/creditor/creditor.component';
import { DebtorStepComponent } from '../../presentationals/debtor-step/debtor-step.component';
import { DebtorComponent } from '../../presentationals/debtor/debtor.component';
import { InvoiceInformationStepComponent } from '../../presentationals/invoice-information-step/invoice-information-step.component';
import { InvoiceInformationComponent } from '../../presentationals/invoice-information/invoice-information.component';

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
    InvoiceInformationComponent,
    CreditorComponent,
    DebtorComponent,
  ],
  templateUrl: './generator.component.html',
  styleUrl: './generator.component.scss',
})
export class GeneratorComponent {
  private readonly wizardService = inject(WizardService);

  openWizard(): void {
    this.wizardService
      .open<Invoice>({
        title: 'Rechnung erstellen',
        steps: [
          InvoiceInformationStepComponent,
          CreditorStepComponent,
          DebtorStepComponent,
        ],
        data: this.generateEmptyInvoice(),
      })
      .afterClosed()
      .pipe(
        take(1),
        tap(() => console.log('closed'))
      )
      .subscribe();
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
      debtor: {
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
