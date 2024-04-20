import { CUSTOM_ELEMENTS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ButtonComponent } from './button.component';

describe('ButtonComponent', () => {
  let component: ButtonComponent;
  let fixture: ComponentFixture<ButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ButtonComponent, MatTooltipModule],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should update styleClasses when color changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        color: {} as SimpleChange,
      };

      component.color = 'disruption';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.styleClasses).toMatchInlineSnapshot(`
        [
          "disruption",
          "filled",
          "square",
        ]
      `);
    });

    it('should update styleClasses when type changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        type: {} as SimpleChange,
      };

      component.type = 'outline';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.styleClasses).toMatchInlineSnapshot(`
        [
          "primary",
          "outline",
          "square",
        ]
      `);
    });

    it('should update styleClasses when shape changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        shape: {} as SimpleChange,
      };

      component.shape = 'rounded';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.styleClasses).toMatchInlineSnapshot(`
        [
          "primary",
          "filled",
          "rounded",
        ]
      `);
    });

    it('should update styleClasses when classList changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        classList: {} as SimpleChange,
      };

      component.classList = ['floating'];

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.styleClasses).toMatchInlineSnapshot(`
        [
          "primary",
          "filled",
          "square",
          "floating",
        ]
      `);
    });

    it('should update styleClasses when tooltipLabel changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        tooltipLabel: {} as SimpleChange,
      };

      component.tooltipLabel = 'activeTooltip';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.tooltipText).toMatchInlineSnapshot(`"activeTooltip"`);
    });

    it('should update styleClasses when tooltipLabelDisabled changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        tooltipLabel: {} as SimpleChange,
      };

      component.tooltipLabel = 'disabledTooltip';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.tooltipText).toMatchInlineSnapshot(`"disabledTooltip"`);
    });

    it('should update styleClasses when disabled changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        disabled: {} as SimpleChange,
      };

      component.disabled = true;
      component.tooltipLabelDisabled = 'tt';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.tooltipText).toMatchInlineSnapshot(`"tt"`);
    });

    it('should update spinnerSize when size changes', () => {
      // arrange
      fixture.detectChanges();

      const changes = {
        size: {} as SimpleChange,
      };

      component.size = 'small';

      // act
      component.ngOnChanges(changes);

      // assert
      expect(component.spinnerSize).toMatchInlineSnapshot(`14`);
    });
  });

  describe('onMouseLeave', () => {
    it('should blur the button when mouse leaves host', () => {
      // arrange
      const blurSpy = jest.fn();
      const outerBlurSpy = jest.fn();

      (component as any).hostElement = {
        nativeElement: {
          querySelector: () => ({ blur: blurSpy }),
          blur: outerBlurSpy,
        },
      };

      // act
      component.onMouseLeave();

      // assert
      expect(blurSpy).toHaveBeenCalledTimes(1);
      expect(outerBlurSpy).toHaveBeenCalledTimes(1);
    });

    it('should not blur the button when mouse leaves but button element is not found', () => {
      // arrange
      const blurSpy = jest.fn();
      const outerBlurSpy = jest.fn();

      (component as any).hostElement = {
        nativeElement: {
          querySelector: () => null,
          blur: outerBlurSpy,
        },
      };

      // act
      component.onMouseLeave();

      // assert
      expect(blurSpy).not.toHaveBeenCalled();
    });
  });

  describe('onClick', () => {
    it('should call blur when button is clicked', () => {
      // arrange
      const event = {} as Event;
      const blurSpy = jest.spyOn(component as any, 'blur');

      component.disabled = false;

      // act
      component.onClick(event);

      // assert
      expect(blurSpy).toHaveBeenCalledTimes(1);
    });
  });
});
