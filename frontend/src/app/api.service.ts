import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

export interface Availability {
  itemId: string;
  sku: string;
  onHandQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
}

export interface Reservation {
  id: string;
  itemId: string;
  quantity: number;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED';
  expiresAt: string;
  completedAt: string | null;
}

export interface ReserveResult { reservation: Reservation; replayed: boolean; }

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = '/api';
  constructor(private readonly http: HttpClient) {}

  createItem(request: { sku: string; initialQuantity: number }) {
    return this.http.post<Availability>(`${this.baseUrl}/inventory/items`, request);
  }

  availability(itemId: string) {
    return this.http.get<Availability>(`${this.baseUrl}/inventory/items/${itemId}/availability`);
  }

  reserve(itemId: string, quantity: number, idempotencyKey: string) {
    return this.http.post<ReserveResult>(`${this.baseUrl}/reservations`, { itemId, quantity }, {
      headers: new HttpHeaders({ 'Idempotency-Key': idempotencyKey })
    });
  }

  transition(id: string, action: 'confirm' | 'cancel') {
    return this.http.post<Reservation>(`${this.baseUrl}/reservations/${id}/${action}`, {});
  }

  expire() { return this.http.post<{ expired: number }>(`${this.baseUrl}/reservations/expire`, {}); }
}
