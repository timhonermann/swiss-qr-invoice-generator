import { InjectionToken, Type } from '@angular/core';
import { Observable } from 'rxjs';

export const WIZARD_DATA = new InjectionToken('WIZARD_DATA');

export const WIZARD_STATE = new InjectionToken<WizardState>('WIZARD_STATE');

/**
 * Interface for Wizard steps
 *
 * @interface WizardStep
 */
export interface WizardStep {
  /**
   * Validates the current step and disables the next or complete button
   * based on the return value of this function
   * @returns boolean
   */
  validate(): boolean;

  /**
   * Optional method
   *
   * If present gets invoked when next button is clicked in wizard.
   * @returns void
   */
  next?(): void;

  /**
   * Optional method
   *
   * If present gets invoked when back button is clicked in wizard.
   * @returns void
   */
  back?(): void;

  /**
   * Optional method
   *
   * If present gets invoked when complete button is clicked in wizard.
   * Based on the Observable result the wizard either stays open or gets closed:
   * - true: wizard closes
   * - false: wizard stays open
   * @returns Object<boolean>
   */
  beforeClose?(): Observable<boolean>;
}

export interface WizardConfig<T> {
  title: string;
  steps: Type<WizardStep>[];
  data?: T;
  initialStepIndex?: number;
  completeButtonLabel?: string;
}

export interface WizardState {
  setTitle(title: string): void;

  goToStep(index: number): void;

  setLoading(isLoading: boolean): void;
}
