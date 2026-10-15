import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  it('shows the server as UP once the health probe answers', async () => {
    TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const fixture = TestBed.createComponent(App);
    TestBed.inject(HttpTestingController).expectOne('health').flush({ status: 'UP' });
    await fixture.whenStable();

    const status = (fixture.nativeElement as HTMLElement).querySelector('.backend-status');
    expect(status?.textContent).toContain('Server: UP');
  });
});
