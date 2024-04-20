import { TestBed } from '@angular/core/testing';
import { MatIconRegistry } from '@angular/material/icon';
import { icons } from '../models/icon.models';

import { IconService } from './icon.service';

describe('IconService', () => {
  let service: IconService;
  let matIconRegistry: MatIconRegistry;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [IconService],
    });
    service = TestBed.inject(IconService);
    matIconRegistry = TestBed.inject(MatIconRegistry);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('init', () => {
    it('should be created', () => {
      const iconCount = icons.length;

      const spy = jest.spyOn(matIconRegistry, 'addSvgIcon');

      service.init();

      expect(spy).toHaveBeenCalledTimes(iconCount);
    });
  });
});
