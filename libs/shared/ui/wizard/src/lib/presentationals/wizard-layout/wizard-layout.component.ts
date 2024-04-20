import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'sqig-wizard-layout',
  templateUrl: './wizard-layout.component.html',
  styleUrls: ['./wizard-layout.component.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardLayoutComponent {}
