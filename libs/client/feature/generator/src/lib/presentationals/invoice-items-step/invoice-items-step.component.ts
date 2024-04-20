import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import {
  WIZARD_DATA,
  WizardStep,
} from '@swiss-qr-invoice-generator/shared/ui/wizard';
import { map } from 'rxjs';
import { Invoice, Item } from '../../models/generator.models';

@Component({
  selector: 'sqig-invoice-items-step',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    ButtonComponent,
  ],
  templateUrl: './invoice-items-step.component.html',
  styleUrl: './invoice-items-step.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InvoiceItemsStepComponent implements WizardStep {
  private readonly data = inject<Invoice>(WIZARD_DATA);

  private readonly formBuilder = inject(FormBuilder);

  readonly addItemForm = this.formBuilder.group({
    description: new FormControl<string | null>(null, [Validators.required]),
    quantity: new FormControl<number | null>(null, [Validators.required]),
    vat: new FormControl<number | null>(null, [Validators.required]),
    unitPrice: new FormControl<number | null>(null, [Validators.required]),
  });

  isFormInvalid = toSignal(
    this.addItemForm.valueChanges.pipe(map(() => !this.addItemForm.valid))
  );

  items = signal(this.data.items);

  addItem(): void {
    const item: Item = {
      description: this.addItemForm.controls.description.value ?? '',
      quantity: this.addItemForm.controls.quantity.value ?? 0,
      vat: this.addItemForm.controls.vat.value ?? 0,
      unitPrice: this.addItemForm.controls.unitPrice.value ?? 0,
    };

    this.data.items.push(item);

    this.addItemForm.reset();
  }

  validate(): boolean {
    return this.data.items.length > 0;
  }
}
