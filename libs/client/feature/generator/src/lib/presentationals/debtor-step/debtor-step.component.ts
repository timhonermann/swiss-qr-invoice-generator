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
  selector: 'sqig-debtor-step',
  standalone: true,
  imports: [
    CommonModule,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
  ],
  templateUrl: './debtor-step.component.html',
  styleUrl: './debtor-step.component.scss',
})
export class DebtorStepComponent implements OnInit, WizardStep {
  private readonly formBuilder = inject(FormBuilder);

  private readonly data = inject<Invoice>(WIZARD_DATA);

  private readonly destroyRef = inject(DestroyRef);

  readonly debtorForm = this.formBuilder.group({
    name: new FormControl<string | null>(null, [Validators.required]),
    streetName: new FormControl<string | null>(null, [Validators.required]),
    streetNumber: new FormControl<string | null>(null, [Validators.required]),
    postalCode: new FormControl<string | null>(null, [Validators.required]),
    city: new FormControl<string | null>(null, [Validators.required]),
  });

  ngOnInit(): void {
    this.setFormData();
    this.observeValueChanges();
  }

  validate(): boolean {
    return this.debtorForm.valid;
  }

  private observeValueChanges(): void {
    this.debtorForm.valueChanges
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.mapFormToInvoice())
      )
      .subscribe();
  }

  private mapFormToInvoice(): void {
    const formValues = this.debtorForm.getRawValue();

    this.data.debtor.name = formValues.name ?? '';
    this.data.debtor.streetName = formValues.streetName ?? '';
    this.data.debtor.streetNumber = formValues.streetNumber ?? '';
    this.data.debtor.postalCode = formValues.postalCode ?? '';
    this.data.debtor.city = formValues.city ?? '';
  }

  private setFormData(): void {
    if (!this.data) {
      return;
    }

    this.debtorForm.controls.name.setValue(this.data.debtor.name);
    this.debtorForm.controls.streetName.setValue(this.data.debtor.streetName);
    this.debtorForm.controls.streetNumber.setValue(
      this.data.debtor.streetNumber
    );
    this.debtorForm.controls.postalCode.setValue(this.data.debtor.postalCode);
    this.debtorForm.controls.city.setValue(this.data.debtor.city);
  }
}
