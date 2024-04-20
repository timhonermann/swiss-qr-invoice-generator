import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import {
  WIZARD_DATA,
  WizardStep,
} from '@swiss-qr-invoice-generator/shared/ui/wizard';
import { debounceTime, distinctUntilChanged, tap } from 'rxjs';
import { Invoice } from '../../models/generator.models';

@Component({
  selector: 'sqig-creditor-step',
  standalone: true,
  imports: [
    CommonModule,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
  ],
  templateUrl: './creditor-step.component.html',
  styleUrl: './creditor-step.component.scss',
})
export class CreditorStepComponent implements OnInit, WizardStep {
  private readonly formBuilder = inject(FormBuilder);

  private readonly data = inject<Invoice>(WIZARD_DATA);

  private readonly destroyRef = inject(DestroyRef);

  readonly creditorForm = this.formBuilder.group({
    iban: new FormControl<string | null>(null, [Validators.required]),
    name: new FormControl<string | null>(null, [Validators.required]),
    streetName: new FormControl<string | null>(null, [Validators.required]),
    streetNumber: new FormControl<string | null>(null, [Validators.required]),
    postalCode: new FormControl<string | null>(null, [Validators.required]),
    city: new FormControl<string | null>(null, [Validators.required]),
    phone: new FormControl<string | null>(null, [Validators.required]),
    email: new FormControl<string | null>(null, [
      Validators.required,
      Validators.email,
    ]),
  });

  ngOnInit(): void {
    this.setFormData();
    this.observeValueChanges();
  }

  validate(): boolean {
    return this.creditorForm.valid;
  }

  private observeValueChanges(): void {
    this.creditorForm.valueChanges
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.mapFormToInvoice())
      )
      .subscribe();
  }

  private mapFormToInvoice(): void {
    const formValues = this.creditorForm.getRawValue();

    this.data.creditor.iban = formValues.iban ?? '';
    this.data.creditor.name = formValues.name ?? '';
    this.data.creditor.streetName = formValues.streetName ?? '';
    this.data.creditor.streetNumber = formValues.streetNumber ?? '';
    this.data.creditor.postalCode = formValues.postalCode ?? '';
    this.data.creditor.city = formValues.city ?? '';
    this.data.creditor.phone = formValues.phone ?? '';
    this.data.creditor.email = formValues.email ?? '';
  }

  private setFormData(): void {
    if (!this.data) {
      return;
    }

    this.creditorForm.controls.iban.setValue(this.data.creditor.iban);
    this.creditorForm.controls.name.setValue(this.data.creditor.name);
    this.creditorForm.controls.streetName.setValue(
      this.data.creditor.streetName
    );
    this.creditorForm.controls.streetNumber.setValue(
      this.data.creditor.streetNumber
    );
    this.creditorForm.controls.postalCode.setValue(
      this.data.creditor.postalCode
    );
    this.creditorForm.controls.city.setValue(this.data.creditor.city);
    this.creditorForm.controls.phone.setValue(this.data.creditor.phone);
    this.creditorForm.controls.email.setValue(this.data.creditor.email);
  }
}
