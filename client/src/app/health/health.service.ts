import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';

export type BackendStatus = 'UP' | 'DOWN';

/** Asks the server whether it is up. The relative URL resolves against <base href>, so it hits
 *  /rummikub/health in Tomcat and /health (proxied to the server) under ng serve. */
@Injectable({ providedIn: 'root' })
export class HealthService {
  private readonly http = inject(HttpClient);

  status(): Observable<BackendStatus> {
    return this.http.get<{ status: string }>('health').pipe(
      map((response) => (response.status === 'UP' ? 'UP' : 'DOWN')),
      catchError(() => of<BackendStatus>('DOWN')),
    );
  }
}
