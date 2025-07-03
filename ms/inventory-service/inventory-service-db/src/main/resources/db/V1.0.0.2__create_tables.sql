CREATE TABLE IF NOT EXISTS inventory.product
(
    id serial PRIMARY KEY,
    name varchar(1024) NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory.inventory
(
    product_id int REFERENCES inventory.product (id) NOT NULL,
    quantity int NOT NULL default 0,
    threshold int not null default 0,
    CONSTRAINT inventory_product_unq UNIQUE (product_id)
);

CREATE TABLE IF NOT EXISTS inventory.reserved_product
(
    order_id int NOT NULL,
    product_id int REFERENCES inventory.product (id) NOT NULL,
    quantity int NOT NULL default 0,
    PRIMARY KEY(order_id, product_id)
);
