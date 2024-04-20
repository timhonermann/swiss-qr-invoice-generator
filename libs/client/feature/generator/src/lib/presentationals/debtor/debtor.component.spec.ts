import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebtorComponent } from './debtor.component';

describe('DebtorComponent', () => {
  let component: DebtorComponent;
  let fixture: ComponentFixture<DebtorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DebtorComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DebtorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
