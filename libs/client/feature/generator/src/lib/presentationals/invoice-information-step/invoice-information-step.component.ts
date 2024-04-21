import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDateRangeInput,
  MatDateRangePicker,
} from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  WIZARD_DATA,
  WizardStep,
} from '@swiss-qr-invoice-generator/shared/ui/wizard';
import { debounceTime, distinctUntilChanged, tap } from 'rxjs';
import { Invoice } from '../../models/generator.models';

@Component({
  selector: 'sqig-invoice-information-step',
  standalone: true,
  imports: [
    CommonModule,
    MatDateRangeInput,
    MatDateRangePicker,
    MatDatepicker,
    MatDatepickerInput,
    ReactiveFormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
  ],
  templateUrl: './invoice-information-step.component.html',
  styleUrl: './invoice-information-step.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceInformationStepComponent implements WizardStep {
  private readonly formBuilder = inject(FormBuilder);

  private readonly data = inject<Invoice>(WIZARD_DATA);

  private readonly destroyRef = inject(DestroyRef);

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

  ngOnInit(): void {
    this.setFormData();
    this.observeValueChanges();
  }

  validate(): boolean {
    return this.invoiceInformationForm.valid;
  }

  private observeValueChanges(): void {
    this.invoiceInformationForm.valueChanges
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.mapFormToInvoice())
      )
      .subscribe();
  }

  private mapFormToInvoice(): void {
    const formValues = this.invoiceInformationForm.getRawValue();

    this.data.title = formValues.title ?? '';
    this.data.vatNumber = formValues.vatNumber ?? '';
    this.data.reference = formValues.referenceNumber ?? '';
    this.data.invoiceDate = formValues.invoiceDate ?? new Date();
    this.data.dueDate = formValues.dueDate ?? new Date();
    this.data.periodFrom = formValues.periodFrom ?? new Date();
    this.data.periodTo = formValues.periodTo ?? new Date();
  }

  private setFormData(): void {
    if (!this.data) {
      return;
    }

    this.invoiceInformationForm.controls.title.setValue(this.data.title);
    this.invoiceInformationForm.controls.invoiceDate.setValue(
      this.data.invoiceDate
    );
    this.invoiceInformationForm.controls.dueDate.setValue(this.data.dueDate);
    this.invoiceInformationForm.controls.periodFrom.setValue(
      this.data.periodFrom
    );
    this.invoiceInformationForm.controls.periodTo.setValue(this.data.periodTo);
    this.invoiceInformationForm.controls.vatNumber.setValue(
      this.data.vatNumber
    );
    this.invoiceInformationForm.controls.referenceNumber.setValue(
      this.data.reference
    );
  }
}
