import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreditorStepComponent } from './creditor-step.component';

describe('CreditorStepComponent', () => {
  let component: CreditorStepComponent;
  let fixture: ComponentFixture<CreditorStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreditorStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(CreditorStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
