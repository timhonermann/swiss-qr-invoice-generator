import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WizardFooterComponent } from './wizard-footer.component';

describe('WizardFooterComponent', () => {
  let component: WizardFooterComponent;
  let fixture: ComponentFixture<WizardFooterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WizardFooterComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WizardFooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show next button', () => {
    // assert
    component.currentStep = 1;
    component.maxSteps = 4;

    // act
    const result = component.showNextButton;

    // assert
    expect(result).toBeTruthy();
  });

  it('should hide next button on last step', () => {
    // assert
    component.currentStep = 1;
    component.maxSteps = 2;

    // act
    const result = component.showNextButton;

    // assert
    expect(result).toBeFalsy();
  });

  it('should show back button', () => {
    // assert
    component.currentStep = 1;
    component.maxSteps = 4;

    // act
    const result = component.showBackButton;

    // assert
    expect(result).toBeTruthy();
  });

  it('should hide back button on first step', () => {
    // assert
    component.currentStep = 0;
    component.maxSteps = 2;

    // act
    const result = component.showBackButton;

    // assert
    expect(result).toBeFalsy();
  });

  it('should show complete button on last step', () => {
    // assert
    component.currentStep = 3;
    component.maxSteps = 4;

    // act
    const result = component.showCompleteButton;

    // assert
    expect(result).toBeTruthy();
  });

  it('should hide complete button', () => {
    // assert
    component.currentStep = 0;
    component.maxSteps = 2;

    // act
    const result = component.showCompleteButton;

    // assert
    expect(result).toBeFalsy();
  });

  it('should show cancel button when it is the first step', () => {
    // assert
    component.currentStep = 0;
    component.maxSteps = 1;

    // act
    const result = component.showCancelButton;

    // assert
    expect(result).toBeTruthy();
  });

  it('should not hide cancel button when there is more than one step', () => {
    // assert
    component.currentStep = 0;
    component.maxSteps = 2;

    // act
    const result = component.showCancelButton;

    // assert
    expect(result).toBe(true);
  });

  it('should emit back event', () => {
    // assert
    const emitSpy = jest.spyOn(component.backClicked, 'emit');

    // act
    component.back();

    // assert
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should emit next event', () => {
    // assert
    const emitSpy = jest.spyOn(component.nextClicked, 'emit');

    // act
    component.next();

    // assert
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should emit complete event', () => {
    // assert
    const emitSpy = jest.spyOn(component.completeClicked, 'emit');

    // act
    component.complete();

    // assert
    expect(emitSpy).toHaveBeenCalled();
  });

  it('should emit cancel event', () => {
    // assert
    const emitSpy = jest.spyOn(component.cancelClicked, 'emit');

    // act
    component.cancel();

    // assert
    expect(emitSpy).toHaveBeenCalled();
  });
});
