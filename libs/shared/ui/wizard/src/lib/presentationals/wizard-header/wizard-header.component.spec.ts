import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { WizardHeaderComponent } from './wizard-header.component';

describe('WizardHeaderComponent', () => {
  let component: WizardHeaderComponent;
  let fixture: ComponentFixture<WizardHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatIconTestingModule],
      declarations: [WizardHeaderComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WizardHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit close event', () => {
    // arrange
    const emitSpy = jest.spyOn(component.closeClicked, 'emit');

    // act
    component.close();

    // assert
    expect(emitSpy).toHaveBeenCalled();
  });

  describe('showCloseButton', () => {
    it('should show close button', () => {
      // arrange
      component.maxSteps = 2;
      component.currentStep = 1;

      // act
      const result = component.showCloseButton;

      // assert
      expect(result).toBe(true);
    });

    it('should hide close button', () => {
      // arrange
      component.maxSteps = 2;
      component.currentStep = 0;

      // act
      const result = component.showCloseButton;

      // assert
      expect(result).toBe(false);
    });

    it('should not show close icon button on single step wizard', () => {
      // arrange
      component.maxSteps = 1;
      component.currentStep = 0;

      // act
      const result = component.showCloseButton;

      // assert
      expect(result).toBe(false);
    });
  });
});
