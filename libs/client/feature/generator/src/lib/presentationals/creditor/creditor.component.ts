import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';

@Component({
  selector: 'sqig-creditor',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
  ],
  templateUrl: './creditor.component.html',
  styleUrl: './creditor.component.scss',
})
export class CreditorComponent {
  formGroup = input.required<FormGroup>();
}
