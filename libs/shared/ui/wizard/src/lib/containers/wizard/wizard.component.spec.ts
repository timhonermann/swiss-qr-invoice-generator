import { Component, CUSTOM_ELEMENTS_SCHEMA, Inject } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatDialogRefMock } from '@client-web/shared/testing';
import { WizardContentDirective } from '../../directives/wizard-content.directive';
import {
  WizardConfig,
  WizardState,
  WizardStep,
  WIZARD_STATE,
} from '../../models/wizard.models';
import { WizardComponent } from './wizard.component';

@Component({
  template: '',
})
class MockStepOneComponent implements WizardStep {
  constructor(@Inject(WIZARD_STATE) public state: WizardState) {}

  validate(): boolean {
    return true;
  }
}

@Component({
  template: '',
})
class MockStepTwoComponent implements WizardStep {
  constructor(@Inject(WIZARD_STATE) public state: WizardState) {}

  validate(): boolean {
    return true;
  }
}

describe('WizardComponent', () => {
  let component: WizardComponent;
  let fixture: ComponentFixture<WizardComponent>;
  let matDialogRef: MatDialogRef<any>;

  const steps = [
    MockStepOneComponent,
    MockStepTwoComponent,
    MockStepOneComponent,
  ];
  const data = 'data';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WizardComponent, WizardContentDirective],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            title: 'title',
            steps,
            data,
            completeButtonLabel: 'completeButtonLabel',
          } as WizardConfig<unknown>,
        },
        { provide: MatDialogRef, useClass: MatDialogRefMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    matDialogRef = TestBed.inject(MatDialogRef);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WizardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should go to previous step', () => {
    // arrange
    component.currentStep = 1;

    // act
    component.onBack();

    // assert
    expect(component.currentStep).toBe(0);
  });

  it('should go to next step', () => {
    // assert
    component.currentStep = 0;

    // act
    component.onNext();

    // assert
    expect(component.currentStep).toBe(1);
  });

  it('should complete wizard', () => {
    // act
    component.onComplete();

    // assert
    expect(matDialogRef.close).toHaveBeenCalledWith(data);
  });

  it('should cancel wizard', () => {
    // act
    component.cancel();

    // assert
    expect(matDialogRef.close).toHaveBeenCalledWith();
  });

  it('should update the title via injection token', () => {
    // arrange
    const title = 'new title';
    const childComponent = (component as any).currentComponent;

    // act
    childComponent.state.setTitle(title);

    // assert
    expect((component as any).config.title).toBe(title);
  });

  it('should set current step via injection token', () => {
    // arrange
    const childComponent = (component as any).currentComponent;

    component.currentStep = 0;

    // act
    childComponent.state.goToStep(1);

    // assert
    expect(component.currentStep).toBe(1);
  });

  it('should set loading state via injection token', () => {
    // arrange
    const childComponent = (component as any).currentComponent;
    const loadingState = true;

    // act
    childComponent.state.setLoading(loadingState);

    // assert
    expect(component.isLoading).toBe(loadingState);
  });

  describe('completeButtonLabel', () => {
    it('should get complete button label from config', () => {
      // act
      const result = component.completeButtonLabel;

      // assert
      expect(result).toBe('completeButtonLabel');
    });
  });
});
