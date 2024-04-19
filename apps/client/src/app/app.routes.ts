import { Route } from '@angular/router';
import { ClientRoutes } from '@swiss-qr-invoice-generator/client/shared/globals';
import { ShellComponent } from './containers/shell/shell.component';

export const appRoutes: Route[] = [
  {
    path: '',
    component: ShellComponent,
    children: [
      {
        path: ClientRoutes.GENERATOR,
        loadChildren: () =>
          import('@swiss-qr-invoice-generator/client/feature/generator').then(
            (lib) => lib.GENERATOR_ROUTES
          ),
      },
      {
        path: '**',
        redirectTo: ClientRoutes.GENERATOR,
        pathMatch: 'full',
      },
    ],
  },
];
