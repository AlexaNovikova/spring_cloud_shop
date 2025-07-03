CREATE TABLE IF NOT EXISTS auth.user
(
    id uuid PRIMARY KEY,
    last_name varchar(1024),
    first_name varchar(1024),
    second_name varchar(1024),
    email varchar(1024),
    created_at timestamp with time zone NOT NULL,
    blocked_at timestamp with time zone
);

CREATE TABLE IF NOT EXISTS auth.identifier
(
    id uuid PRIMARY KEY,
    user_id uuid REFERENCES auth.user (id) NOT NULL,
    login varchar(255) NOT NULL,
    blocked boolean,
    secret varchar(255) NOT NULL,
    CONSTRAINT identifier_login_unq UNIQUE (login)
);

CREATE TABLE IF NOT EXISTS auth.role
(
    id uuid PRIMARY KEY,
    name varchar(255) NOT NULL,
    CONSTRAINT role_unq UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS auth.user_role
(
    user_id uuid REFERENCES auth.user (id) NOT NULL,
    role_id uuid REFERENCES auth.role (id) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);