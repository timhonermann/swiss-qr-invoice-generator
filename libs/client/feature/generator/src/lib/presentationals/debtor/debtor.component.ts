import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';

@Component({
  selector: 'sqig-debtor',
  standalone: true,
  imports: [
    CommonModule,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
  ],
  templateUrl: './debtor.component.html',
  styleUrl: './debtor.component.scss',
})
export class DebtorComponent {
  formGroup = input.required<FormGroup>();
}
