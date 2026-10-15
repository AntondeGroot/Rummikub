import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { HealthService } from './health.service';

describe('HealthService', () => {
  it('reports DOWN when the server cannot be reached', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const status = firstValueFrom(TestBed.inject(HealthService).status());

    TestBed.inject(HttpTestingController).expectOne('health').error(new ProgressEvent('error'));

    expect(await status).toBe('DOWN');
  });

  it('reports DOWN when the server answers with any status other than UP', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const status = firstValueFrom(TestBed.inject(HealthService).status());

    TestBed.inject(HttpTestingController).expectOne('health').flush({ status: 'STARTING' });

    expect(await status).toBe('DOWN');
  });
});
