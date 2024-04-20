import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDateRangeInput,
  MatDateRangePicker,
} from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'sqig-invoice-information',
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
  templateUrl: './invoice-information.component.html',
  styleUrl: './invoice-information.component.scss',
})
export class InvoiceInformationComponent {
  formGroup = input.required<FormGroup>();
}
