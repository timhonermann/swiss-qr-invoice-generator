import { coerceArray } from '@angular/cdk/coercion';
import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  inject,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule, TooltipPosition } from '@angular/material/tooltip';
import { IconType } from '@swiss-qr-invoice-generator/shared/ui/icons';
import {
  ButtonBehaviour,
  ButtonClass,
  ButtonColor,
  ButtonDisplay,
  ButtonShape,
  ButtonSize,
  ButtonType,
} from '../../models/button.models';

@Component({
  selector: 'sqig-button',
  standalone: true,
  imports: [
    CommonModule,
    MatTooltipModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
})
export class ButtonComponent implements OnInit, OnChanges {
  private readonly hostElement = inject(ElementRef<HTMLElement>);

  @Input() color: ButtonColor = 'primary';

  @Input() type: ButtonType = 'filled';

  @Input() shape: ButtonShape = 'square';

  @Input() size: ButtonSize = 'large';

  @Input() classList: ButtonClass | ButtonClass[] = [];

  @Input() display: ButtonDisplay = 'inline-block';

  @Input() disabled = false;

  @Input() isSpinning = false;

  @Input() tooltipLabel = '';

  @Input() tooltipLabelDisabled = '';

  @Input() tooltipPosition: TooltipPosition = 'above';

  @Input() behavior: ButtonBehaviour = 'button';

  @Input() label?: string;

  @Input() icon?: IconType;

  @Output() clicked = new EventEmitter<Event>();

  styleClasses: string[] = [];

  tooltipText = '';

  spinnerSize = 20;

  @HostBinding('class') get hostClasses(): string[] {
    return [this.display, this.size, ...this.styleClasses, 'button'];
  }

  @HostBinding('attr.disabled') get hostDisabled(): boolean | null {
    return this.disabled || null;
  }

  @HostListener('click', ['$event']) onClick(_: Event): void {
    this.blur();
  }

  @HostListener('mousedown', ['$event']) onFocus(_: Event): void {
    this.focus();
  }

  ngOnInit(): void {
    this.setStyleClasses();
    this.setTooltip();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['color'] ||
      changes['type'] ||
      changes['shape'] ||
      changes['classList']
    ) {
      this.setStyleClasses();
    }

    if (
      changes['tooltipLabel'] ||
      changes['tooltipLabelDisabled'] ||
      changes['disabled']
    ) {
      this.setTooltip();
    }

    if (changes['size']) {
      this.setSpinnerSize();
    }
  }

  onButtonClick(event: Event): void {
    this.clicked.emit(event);
  }

  onMouseLeave(): void {
    this.blur();
  }

  private setStyleClasses(): void {
    this.styleClasses = [
      this.color,
      this.type,
      this.shape,
      ...coerceArray(this.classList),
    ];
  }

  private setTooltip(): void {
    this.tooltipText = this.disabled
      ? this.tooltipLabelDisabled
      : this.tooltipLabel;
  }

  private setSpinnerSize(): void {
    const sizeMap: Record<ButtonSize, number> = {
      ['x-small']: 12,
      ['small']: 14,
      ['medium']: 16,
      ['large']: 20,
      ['x-large']: 20,
    };

    this.spinnerSize = sizeMap[this.size];
  }

  private focus(): void {
    this.hostElement.nativeElement?.focus();
  }

  private blur(): void {
    this.hostElement.nativeElement.querySelector('button')?.blur();
    this.hostElement.nativeElement?.blur();
  }
}
