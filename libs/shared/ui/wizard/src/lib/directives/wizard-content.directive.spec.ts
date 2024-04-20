import { ViewContainerRef } from '@angular/core';
import { WizardContentDirective } from './wizard-content.directive';

describe('WizardContentDirective', () => {
  it('should create an instance', () => {
    const directive = new WizardContentDirective({} as ViewContainerRef);

    expect(directive).toBeTruthy();
  });
});
