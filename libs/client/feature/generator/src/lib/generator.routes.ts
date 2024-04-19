import { Routes } from '@angular/router';
import { GeneratorComponent } from './containers/generator/generator.component';

export const GENERATOR_ROUTES: Routes = [
  {
    path: 'generator',
    component: GeneratorComponent
  },
  {
    path: '',
    redirectTo: 'generator',
    pathMatch: 'full'
  }
]
