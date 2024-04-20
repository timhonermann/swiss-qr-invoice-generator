import { TestBed } from '@angular/core/testing';
import { provideMock } from '@client-web/shared/testing';
import { DialogService } from '@client-web/shared/ui/dialog';
import { WizardComponent } from '../containers/wizard/wizard.component';
import { WizardConfig } from '../models/wizard.models';
import { WizardService } from './wizard.service';

describe('WizardService', () => {
  let service: WizardService;
  let dialogService: DialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideMock(DialogService)],
    });
    dialogService = TestBed.inject(DialogService);

    service = TestBed.inject(WizardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('open', () => {
    it('should open wizard component', () => {
      // arrange
      const config = {
        title: 'Some Title',
        steps: [],
        data: {
          some: 'data',
        },
      } as WizardConfig<any>;
      const openSpy = jest.spyOn(dialogService, 'open');

      // act
      service.open(config);

      // assert
      expect(openSpy).toHaveBeenCalledWith(WizardComponent, {
        panelClass: 'wizard-full-screen',
        data: config,
      });
    });
  });
});
