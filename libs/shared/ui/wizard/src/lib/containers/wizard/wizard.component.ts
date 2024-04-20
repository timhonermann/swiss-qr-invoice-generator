import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  Injector,
  OnInit,
  signal,
  viewChild,
} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { filter, of } from 'rxjs';
import { WizardContentDirective } from '../../directives/wizard-content.directive';
import {
  WIZARD_DATA,
  WIZARD_STATE,
  WizardConfig,
  WizardState,
  WizardStep,
} from '../../models/wizard.models';
import { WizardFooterComponent } from '../../presentationals/wizard-footer/wizard-footer.component';
import { WizardHeaderComponent } from '../../presentationals/wizard-header/wizard-header.component';
import { WizardLayoutComponent } from '../../presentationals/wizard-layout/wizard-layout.component';

@Component({
  selector: 'sqig-wizard',
  templateUrl: './wizard.component.html',
  styleUrls: ['./wizard.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    WizardLayoutComponent,
    WizardHeaderComponent,
    WizardContentDirective,
    WizardFooterComponent,
  ],
})
export class WizardComponent implements OnInit {
  private readonly config = inject<WizardConfig<unknown>>(MAT_DIALOG_DATA);

  private readonly dialogRef = inject(MatDialogRef<WizardComponent>);

  private readonly cdr = inject(ChangeDetectorRef);

  wizardContent = viewChild.required(WizardContentDirective);

  currentStep = 0;

  isLoading = false;

  private currentComponent?: WizardStep;

  title = signal(this.config.title);

  maxSteps = signal(this.config.steps.length);

  completeButtonLabel = signal(this.config.completeButtonLabel);

  ngOnInit(): void {
    this.setCurrentStep(this.config.initialStepIndex);
  }

  onBack(): void {
    this.currentStep--;

    this.currentComponent?.back?.();

    this.loadComponent();
  }

  onNext(): void {
    this.currentStep++;

    this.currentComponent?.next?.();

    this.loadComponent();
  }

  onComplete(): void {
    const beforeClose$ = this.currentComponent?.beforeClose?.() ?? of(true);

    beforeClose$.pipe(filter(Boolean)).subscribe(() => {
      this.dialogRef.close(this.config.data);
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  isNextDisabled(): boolean {
    return !this.currentComponent?.validate();
  }

  private loadComponent(): void {
    const component = this.config.steps[this.currentStep];
    const injector = Injector.create({
      providers: [
        { provide: WIZARD_DATA, useValue: this.config.data },
        {
          provide: WIZARD_STATE,
          useValue: {
            setTitle: (title: string) => this.setTitle(title),
            goToStep: (index: number) => this.setCurrentStep(index),
            setLoading: (isLoading: boolean) => (this.isLoading = isLoading),
          } as WizardState,
        },
        { provide: MatDialogRef, useValue: this.dialogRef },
      ],
    });

    this.wizardContent().viewContainerRef?.clear();
    this.currentComponent =
      this.wizardContent().viewContainerRef?.createComponent(component, {
        injector,
      }).instance;
  }

  private setTitle(title: string): void {
    this.config.title = title;

    // Because the child component (wizard step) can call
    // this method, `this.cdr.detectChanges` is required.
    this.cdr.detectChanges();
  }

  private setCurrentStep(stepIndex?: number): void {
    this.currentStep = stepIndex ?? 0;
    this.loadComponent();
  }
}
