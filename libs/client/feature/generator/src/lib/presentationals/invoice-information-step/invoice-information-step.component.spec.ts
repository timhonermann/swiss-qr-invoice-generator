import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InvoiceInformationStepComponent } from './invoice-information-step.component';

describe('InvoiceInformationStepComponent', () => {
  let component: InvoiceInformationStepComponent;
  let fixture: ComponentFixture<InvoiceInformationStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvoiceInformationStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InvoiceInformationStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
