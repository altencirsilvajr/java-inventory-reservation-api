import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { ApiService, Availability, Reservation } from './api.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly availability = signal<Availability | null>(null);
  readonly reservations = signal<Reservation[]>([]);
  readonly timeline = signal<string[]>([]);
  readonly busy = signal(false);

  readonly itemForm = new FormGroup({
    sku: new FormControl('INTERVIEW-SKU', { nonNullable: true, validators: [Validators.required] }),
    initialQuantity: new FormControl(5, { nonNullable: true, validators: [Validators.min(0)] })
  });
  readonly reserveForm = new FormGroup({
    quantity: new FormControl(2, { nonNullable: true, validators: [Validators.min(1)] }),
    idempotencyKey: new FormControl(crypto.randomUUID(), { nonNullable: true, validators: [Validators.required] })
  });

  constructor(private readonly api: ApiService) {}

  async createItem(): Promise<void> {
    if (this.itemForm.invalid) return;
    await this.run('Create inventory item', async () => {
      const item = await firstValueFrom(this.api.createItem(this.itemForm.getRawValue()));
      this.availability.set(item);
      this.reservations.set([]);
    });
  }

  async reserve(): Promise<void> {
    const item = this.availability();
    if (!item || this.reserveForm.invalid) return;
    await this.run('Reserve stock', async () => {
      const result = await firstValueFrom(this.api.reserve(item.itemId, this.reserveForm.controls.quantity.value,
        this.reserveForm.controls.idempotencyKey.value));
      this.upsert(result.reservation);
      await this.refresh();
    });
  }

  async transition(reservation: Reservation, action: 'confirm' | 'cancel'): Promise<void> {
    await this.run(`${action} reservation`, async () => {
      this.upsert(await firstValueFrom(this.api.transition(reservation.id, action)));
      await this.refresh();
    });
  }

  async race(): Promise<void> {
    const item = this.availability();
    if (!item) return;
    const quantity = Math.max(1, item.availableQuantity);
    await this.run(`Race two claims of ${quantity}`, async () => {
      const attempts = await Promise.allSettled([
        firstValueFrom(this.api.reserve(item.itemId, quantity, crypto.randomUUID())),
        firstValueFrom(this.api.reserve(item.itemId, quantity, crypto.randomUUID()))
      ]);
      attempts.forEach(result => {
        if (result.status === 'fulfilled') this.upsert(result.value.reservation);
        this.note(result.status === 'fulfilled' ? 'race: 200 accepted' : 'race: 409 insufficient stock');
      });
      await this.refresh();
    });
  }

  async expire(): Promise<void> {
    await this.run('Expire pending reservations', async () => {
      const result = await firstValueFrom(this.api.expire());
      this.note(`expired: ${result.expired}`);
      await this.refresh();
    });
  }

  async refresh(): Promise<void> {
    const item = this.availability();
    if (item) this.availability.set(await firstValueFrom(this.api.availability(item.itemId)));
  }

  private async run(label: string, action: () => Promise<void>): Promise<void> {
    this.busy.set(true);
    this.note(`${label} → request`);
    try { await action(); this.note(`${label} → success`); }
    catch (failure: any) { this.note(`${label} → ${failure?.status ?? 'error'} ${failure?.error?.detail ?? failure?.message}`); }
    finally { this.busy.set(false); }
  }

  private upsert(reservation: Reservation): void {
    this.reservations.update(current => [reservation, ...current.filter(item => item.id !== reservation.id)]);
  }

  private note(message: string): void {
    this.timeline.update(entries => [`${new Date().toLocaleTimeString()}  ${message}`, ...entries].slice(0, 20));
  }
}
