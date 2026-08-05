create table inventory_items (
    id uuid primary key,
    sku varchar(80) not null unique,
    on_hand_quantity integer not null check (on_hand_quantity >= 0),
    reserved_quantity integer not null check (reserved_quantity >= 0 and reserved_quantity <= on_hand_quantity),
    version bigint not null default 0
);

create table reservations (
    id uuid primary key,
    item_id uuid not null references inventory_items(id),
    quantity integer not null check (quantity > 0),
    idempotency_key varchar(120) not null unique,
    expires_at timestamptz not null,
    completed_at timestamptz,
    status varchar(16) not null check (status in ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    version bigint not null default 0
);

create index ix_reservations_pending_expiration on reservations (expires_at) where status = 'PENDING';
