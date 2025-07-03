CREATE TABLE IF NOT EXISTS product.categories (
    id                              bigserial primary key,
    title                           varchar(255),
    created_at                      timestamp default current_timestamp,
    updated_at                      timestamp default current_timestamp
);

CREATE TABLE IF NOT EXISTS product.products
(
    id                              bigserial primary key,
    title                           varchar(255),
    price                           numeric (8, 2),
    category_id                     bigint references product.categories (id),
    created_at                      timestamp default current_timestamp,
    updated_at                      timestamp default current_timestamp
);