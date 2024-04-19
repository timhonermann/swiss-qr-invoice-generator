import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButton } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'sqig-generator',
  standalone: true,
  imports: [
    CommonModule,
    MatButton,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
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
  });
}
