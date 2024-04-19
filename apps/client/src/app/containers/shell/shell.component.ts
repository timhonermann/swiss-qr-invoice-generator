import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../presentationals/header/header.component';
import { ShellLayoutComponent } from '../../presentationals/shell-layout/shell-layout.component';

@Component({
  selector: 'sqig-shell',
  standalone: true,
  imports: [CommonModule, ShellLayoutComponent, HeaderComponent, RouterOutlet],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent {}
