import { provideHttpClient } from '@angular/common/http';
import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import {
  MAT_DATE_LOCALE,
  provideNativeDateAdapter,
} from '@angular/material/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { IconService } from '@swiss-qr-invoice-generator/shared/ui/icons';
import { EMPTY, Observable } from 'rxjs';
import { appRoutes } from './app.routes';

function initializeAppFactory(
  iconService: IconService
): () => Observable<never> {
  return () => {
    iconService.init();

    return EMPTY;
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(appRoutes),
    provideHttpClient(),
    provideAnimationsAsync(),
    provideAnimationsAsync(),
    provideNativeDateAdapter(),
    { provide: MAT_DATE_LOCALE, useValue: 'de' },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeAppFactory,
      deps: [IconService],
      multi: true,
    },
  ],
};
