import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends idempotency key when reserving stock', () => {
    service.reserve('item-id', 2, 'stable-key').subscribe();
    const request = http.expectOne('/api/reservations');
    expect(request.request.method).toBe('POST');
    expect(request.request.headers.get('Idempotency-Key')).toBe('stable-key');
    expect(request.request.body).toEqual({ itemId: 'item-id', quantity: 2 });
    request.flush({ reservation: {}, replayed: false });
  });
});
