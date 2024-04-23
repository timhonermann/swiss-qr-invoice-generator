import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImageUploadComponent } from '@swiss-qr-invoice-generator/shared/ui/image-upload';
import { Invoice } from '../../models/generator.models';
import {
  WIZARD_DATA,
  WizardStep,
} from '@swiss-qr-invoice-generator/shared/ui/wizard';

@Component({
  selector: 'sqig-creditor-logo',
  standalone: true,
  imports: [CommonModule, ImageUploadComponent],
  templateUrl: './creditor-logo-step.component.html',
  styleUrl: './creditor-logo-step.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreditorLogoStepComponent implements WizardStep {
  private readonly data = inject<Invoice>(WIZARD_DATA);

  readonly logoUrl = signal<string | null>(this.data.creditor.logoBase64);

  onLogoChanged(logo: string | null): void {
    this.logoUrl.set(logo);
    this.data.creditor.logoBase64 = logo;
  }

  validate(): boolean {
    return true;
  }
}
