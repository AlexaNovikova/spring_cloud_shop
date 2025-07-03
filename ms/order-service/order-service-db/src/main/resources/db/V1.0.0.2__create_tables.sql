CREATE TABLE IF NOT EXISTS "order"."order"
(
    id serial PRIMARY KEY,
    user_id uuid not null,
    email varchar not null,
    status varchar NOT NULL,
    total_price numeric(8, 2) not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz,
    completed_at timestamptz
);

CREATE TABLE IF NOT EXISTS "order".order_item
(
    order_id int REFERENCES "order"."order" (id) NOT NULL,
    product_id int not null,
    name varchar not null,
    price numeric(8, 2) not null default 0,
    quantity int NOT NULL default 0,
    PRIMARY KEY(order_id, product_id)
);