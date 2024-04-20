import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { StepIndicatorComponent } from '../step-indicator/step-indicator.component';

@Component({
  selector: 'sqig-wizard-header',
  templateUrl: './wizard-header.component.html',
  styleUrls: ['./wizard-header.component.scss'],
  imports: [StepIndicatorComponent, ButtonComponent],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardHeaderComponent {
  title = input.required<string>();

  maxSteps = input.required<number>();

  currentStep = input.required<number>();

  closeClicked = output<void>();

  showCloseButton = computed(() => this.currentStep() > 0);

  showStepIndicator = computed(() => this.maxSteps() > 1);

  close(): void {
    this.closeClicked.emit();
  }
}
