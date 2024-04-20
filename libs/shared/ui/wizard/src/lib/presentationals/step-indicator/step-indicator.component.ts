import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'sqig-step-indicator',
  templateUrl: './step-indicator.component.html',
  styleUrls: ['./step-indicator.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StepIndicatorComponent {
  maxSteps = input.required<number>();

  currentStep = input.required<number>();

  toArray(n: number): number[] {
    return Array(n);
  }

  isActive(step: number): boolean {
    return step <= this.currentStep();
  }
}
