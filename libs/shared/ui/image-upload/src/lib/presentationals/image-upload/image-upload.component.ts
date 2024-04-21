import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { filter, Observable, take } from 'rxjs';
import { ButtonComponent } from '@swiss-qr-invoice-generator/shared/ui/button';
import { MatIcon } from '@angular/material/icon';

@Component({
  selector: 'sqig-image-upload',
  standalone: true,
  imports: [CommonModule, ButtonComponent, MatIcon],
  templateUrl: './image-upload.component.html',
  styleUrl: './image-upload.component.scss',
})
export class ImageUploadComponent {
  private readonly maxFileSize = 5_000_000;

  imageUrl = input.required<string | null>();

  imageChanged = output<string | null>();

  readonly acceptFileTypes = [
    'image/gif',
    'image/jpg',
    'image/jpeg',
    'image/png',
    'image/svg+xml',
  ];

  handleFileInput(event: Event): void {
    const file = this.getFileFromEvent(event);
    const isValidFileType = this.isValidFileType(file);
    const isValidFileSize = file && file.size <= this.maxFileSize;
    const isValidFile = isValidFileSize && isValidFileType;

    if (!isValidFile) {
      // const error = !isValidFileType ? 'type' : 'size';
      //
      // this.onImageFileError(error);

      return;
    }

    this.convertBlobToBase64(file)
      .pipe(
        take(1),
        filter((base64Url): base64Url is string => Boolean(base64Url))
      )
      .subscribe((image) => {
        this.imageChanged.emit(image);
      });
  }

  removeImage(): void {
    this.imageChanged.emit(null);
  }

  // private onImageFileError(error: 'size' | 'type'): void {
  //   const message =
  //     error === 'size'
  //       ? `Das Bild zu ist gross. Max grösse ist ${
  //           this.maxFileSize / 1000 / 1000
  //         }MB`
  //       : 'Es können nur Bilder in den Formaten PNG, JPG, JPEG sowie nicht animierte GIF hochgeladen werden.';
  //
  //   this.snackbarService.open({
  //     message,
  //     type: 'error',
  //   });
  // }

  private getFileFromEvent(event: Event): File {
    const inputEl = event.target as HTMLInputElement & { files: File[] };
    const file = inputEl.files[0];

    inputEl.value = '';

    return file;
  }

  // private openCropDialog(
  //   base64Url: string
  // ): Observable<CropAvatarDialogComponentResult> {
  //   return this.dialogService
  //     .open<
  //       CropAvatarDialogComponent,
  //       CropAvatarDialogComponentResult,
  //       CropAvatarDialogComponentData
  //     >(CropAvatarDialogComponent, {
  //       data: { imageUrl: base64Url, isSquare: true },
  //       dialogWidth: 'large',
  //       disableClose: false,
  //     })
  //     .afterClosed()
  //     .pipe(
  //       takeUntilDestroyed(this.destoryRef),
  //       filter((result): result is CropAvatarDialogComponentResult =>
  //         Boolean(result?.croppedImage)
  //       )
  //     );
  // }

  private isValidFileType(file: File): boolean {
    return this.acceptFileTypes.includes(file?.type);
  }

  private convertBlobToBase64(data: Blob): Observable<string | null> {
    return new Observable<string | null>((observer) => {
      if (data) {
        const blob = new Blob([data], {
          type: this.getContentType(data),
        });
        const reader = new FileReader();

        reader.readAsDataURL(blob); // convert blob to base64
        reader.onloadend = () => {
          observer.next(`${reader.result}`); // emit the base64 string result
          observer.complete();
        };
      } else {
        observer.next(null);
        observer.complete();
      }
    });
  }

  private getContentType(data: Blob): string {
    if (!data) {
      return 'image';
    } else {
      return data.type;
    }
  }
}
