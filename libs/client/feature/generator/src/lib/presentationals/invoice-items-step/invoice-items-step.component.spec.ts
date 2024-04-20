import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InvoiceItemsStepComponent } from './invoice-items-step.component';

describe('InvoiceItemsStepComponent', () => {
  let component: InvoiceItemsStepComponent;
  let fixture: ComponentFixture<InvoiceItemsStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvoiceItemsStepComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InvoiceItemsStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
