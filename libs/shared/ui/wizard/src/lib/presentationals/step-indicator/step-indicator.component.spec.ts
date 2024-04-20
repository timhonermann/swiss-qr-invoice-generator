import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StepIndicatorComponent } from './step-indicator.component';

describe('StepIndicatorComponent', () => {
  let component: StepIndicatorComponent;
  let fixture: ComponentFixture<StepIndicatorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StepIndicatorComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StepIndicatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have active step', () => {
    // arrange
    component.currentStep = 2;
    const step = 1;

    // act
    const result = component.isActive(step);

    // assert
    expect(result).toBeTruthy();
  });

  it('should have inactive step', () => {
    // arrange
    component.currentStep = 2;
    const step = 3;

    // act
    const result = component.isActive(step);

    // assert
    expect(result).toBeFalsy();
  });

  it('should have array for given length', () => {
    // arrange
    const length = 3;

    // act
    const result = component.toArray(length);

    // assert
    expect(result.length).toBe(length);
  });
});
