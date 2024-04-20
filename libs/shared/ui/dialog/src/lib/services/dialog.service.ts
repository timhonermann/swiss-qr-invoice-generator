import { coerceArray } from '@angular/cdk/coercion';
import { ComponentType } from '@angular/cdk/portal';
import { inject, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { DialogConfig, DialogWidth } from '../models/dialog.models';

const PANEL_CLASS_MAP: { [key in DialogWidth]: string } = {
  extraSmall: 'dialog-width-xs',
  small: 'dialog-width-sm',
  medium: 'dialog-width-md',
  large: 'dialog-width-lg',
  extraLarge: 'dialog-width-xl',
};

@Injectable({
  providedIn: 'root',
})
export class DialogService {
  private readonly dialog = inject(MatDialog);

  /**
   * Opens a modal dialog containing the given component.
   * @param component Type of the component to load into the dialog.
   * @param config Extra configuration options.
   * @returns Reference to the newly-opened dialog.
   */
  open<T, R, D>(
    component: ComponentType<T>,
    config: DialogConfig<D> = {}
  ): MatDialogRef<T, R> {
    const widthClass = PANEL_CLASS_MAP[config.dialogWidth ?? 'extraSmall'];

    const panelClasses = config.panelClass
      ? coerceArray(config.panelClass)
      : [];
    const matConfig = {
      ...config,
      panelClass: [widthClass, ...(panelClasses ?? [])],
    };

    return this.dialog.open(component, matConfig);
  }

  /**
   * Finds an open dialog by its id.
   * @param id ID to use when looking up the dialog.
   */
  getDialogById<T = unknown>(id: string): MatDialogRef<T> | undefined {
    return this.dialog.getDialogById(id);
  }

  hasOpenDialogs(): boolean {
    return this.dialog.openDialogs?.length > 0;
  }
}
