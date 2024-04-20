import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { CreditorComponent } from '../../presentationals/creditor/creditor.component';
import { DebtorComponent } from '../../presentationals/debtor/debtor.component';
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
  private readonly formBuilder = inject(FormBuilder);

  readonly invoiceInformationForm = this.formBuilder.group({
    title: new FormControl<string | null>(null, [Validators.required]),
    invoiceDate: new FormControl<Date | null>(null, [Validators.required]),
    dueDate: new FormControl<Date | null>(null, [Validators.required]),
    periodFrom: new FormControl<Date | null>(null, [Validators.required]),
    periodTo: new FormControl<Date | null>(null, [Validators.required]),
    vatNumber: new FormControl<string | null>(null, [Validators.required]),
    referenceNumber: new FormControl<string | null>(null, [
      Validators.required,
    ]),
  });

  readonly creditorForm = this.formBuilder.group({
    iban: new FormControl<string | null>(null, [Validators.required]),
    name: new FormControl<string | null>(null, [Validators.required]),
    streetName: new FormControl<string | null>(null, [Validators.required]),
    streetNumber: new FormControl<string | null>(null, [Validators.required]),
    postalCode: new FormControl<number | null>(null, [Validators.required]),
    city: new FormControl<string | null>(null, [Validators.required]),
    phone: new FormControl<string | null>(null, [Validators.required]),
    email: new FormControl<string | null>(null, [
      Validators.required,
      Validators.email,
    ]),
  });

  readonly debtorForm = this.formBuilder.group({
    name: new FormControl<string | null>(null, [Validators.required]),
    streetName: new FormControl<string | null>(null, [Validators.required]),
    streetNumber: new FormControl<string | null>(null, [Validators.required]),
    postalCode: new FormControl<number | null>(null, [Validators.required]),
    city: new FormControl<string | null>(null, [Validators.required]),
  });
}
