import { inject, Injectable } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { DialogService } from '@swiss-qr-invoice-generator/shared/ui/dialog';
import { WizardComponent } from '../containers/wizard/wizard.component';
import { WizardConfig } from '../models/wizard.models';

@Injectable({
  providedIn: 'root',
})
export class WizardService {
  private readonly dialogService = inject(DialogService);

  open<T = unknown>(config: WizardConfig<T>): MatDialogRef<WizardComponent, T> {
    return this.dialogService.open(WizardComponent, {
      panelClass: 'wizard-full-screen',
      data: config,
    });
  }
}
