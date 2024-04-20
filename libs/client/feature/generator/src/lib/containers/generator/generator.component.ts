import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { WizardService } from '@swiss-qr-invoice-generator/shared/ui/wizard';
import { take, tap } from 'rxjs';
import { Invoice } from '../../models/generator.models';
import { InvoiceItemsStepComponent } from '../../presentationals/invoice-items-step/invoice-items-step.component';

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

  openWizard(): void {
    this.wizardService
      .open<Invoice>({
        title: 'Rechnung erstellen',
        steps: [
          // InvoiceInformationStepComponent,
          // CreditorStepComponent,
          // DebtorStepComponent,
          InvoiceItemsStepComponent,
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
