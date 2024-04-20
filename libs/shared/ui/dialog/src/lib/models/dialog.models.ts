import { MatDialogConfig } from '@angular/material/dialog';

export type DialogType = 'primary' | 'danger';

export type DialogWidth =
  | 'extraSmall'
  | 'small'
  | 'medium'
  | 'large'
  | 'extraLarge';

export interface DialogConfig<D = unknown> extends MatDialogConfig<D> {
  dialogWidth?: DialogWidth;
}
