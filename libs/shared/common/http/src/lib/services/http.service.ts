import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HttpService {
  private readonly http = inject(HttpClient);

  get<T>(url: string, options?: NonNullable<unknown>): Observable<T> {
    return this.http.get<T>(url, options);
  }

  post<T>(
    url: string,
    body: unknown,
    options?: NonNullable<unknown>
  ): Observable<T> {
    return this.http.post<T>(url, body, options);
  }

  put<T>(
    url: string,
    body: unknown,
    options?: NonNullable<unknown>
  ): Observable<T> {
    return this.http.put<T>(url, body, options);
  }

  patch<T>(
    url: string,
    body: unknown,
    options?: NonNullable<unknown>
  ): Observable<T> {
    const headers = new HttpHeaders().set(
      'Content-Type',
      'application/json-patch+json'
    );

    return this.http.patch<T>(url, body, { ...options, headers });
  }

  delete<T>(url: string, options?: NonNullable<unknown>): Observable<T> {
    return this.http.delete<T>(url, options);
  }
}
