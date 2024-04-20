import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
  selector: '[sqigWizardContent]',
  standalone: true,
})
export class WizardContentDirective {
  constructor(public viewContainerRef: ViewContainerRef) {}
}
