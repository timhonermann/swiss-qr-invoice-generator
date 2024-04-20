import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { IconType } from '@swiss-qr-invoice-generator/shared/ui/icons';

@Component({
  selector: 'sqig-wizard-footer',
  templateUrl: './wizard-footer.component.html',
  styleUrls: ['./wizard-footer.component.scss'],
  imports: [ButtonComponent],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardFooterComponent {
  currentStep = input.required<number>();

  maxSteps = input.required<number>();

  isNextDisabled = input.required<boolean>();

  isLoading = input.required<boolean>();

  completeButtonLabel = input<string>();

  icon = input<IconType>();

  backClicked = output<void>();

  nextClicked = output<void>();

  cancelClicked = output<void>();

  completeClicked = output<void>();

  showBackButton = computed(() => this.currentStep() > 0);

  showNextButton = computed(() => this.currentStep() < this.maxSteps() - 1);

  showCancelButton = computed(() => this.currentStep() === 0);

  showCompleteButton = computed(
    () => this.currentStep() === this.maxSteps() - 1
  );

  back(): void {
    this.backClicked.emit();
  }

  next(): void {
    this.nextClicked.emit();
  }

  cancel(): void {
    this.cancelClicked.emit();
  }

  complete(): void {
    this.completeClicked.emit();
  }
}
