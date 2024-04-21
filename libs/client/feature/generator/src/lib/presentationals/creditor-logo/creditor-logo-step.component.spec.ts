import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreditorLogoStepComponent } from './creditor-logo-step.component';

describe('CreditorLogoComponent', () => {
  let component: CreditorLogoStepComponent;
  let fixture: ComponentFixture<CreditorLogoStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreditorLogoStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CreditorLogoStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
