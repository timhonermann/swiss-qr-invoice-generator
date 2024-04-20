import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebtorStepComponent } from './debtor-step.component';

describe('DebtorStepComponent', () => {
  let component: DebtorStepComponent;
  let fixture: ComponentFixture<DebtorStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DebtorStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DebtorStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
