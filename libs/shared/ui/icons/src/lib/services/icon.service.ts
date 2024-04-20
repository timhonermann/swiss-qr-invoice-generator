import { inject, Injectable } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { icons, IconType } from '../models/icon.models';

@Injectable({
  providedIn: 'root',
})
export class IconService {
  private readonly matIconRegistry = inject(MatIconRegistry);

  private readonly domSanitizer = inject(DomSanitizer);

  init(): void {
    icons.forEach((i) => this.registerSvgIcon(i, `assets/icons/${i}.svg`));
  }

  private registerSvgIcon(key: IconType, assetUrl: string): void {
    this.matIconRegistry.addSvgIcon(
      key,
      this.domSanitizer.bypassSecurityTrustResourceUrl(assetUrl),
    );
  }
}
