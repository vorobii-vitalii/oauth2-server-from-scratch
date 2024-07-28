--liquibase formatted sql

--changeset Vitalii:1 context:auth-server
--comment: Create table that stores registered OAuth2 clients
create table client_registrations
(
    client_id      uuid not null,
    client_secret_enc varchar(255) not null,
    client_type varchar(50) not null,
    redirect_url varchar(300) not null,
    client_name varchar(100) not null unique,
    client_description varchar(500) not null,
    primary key (client_id)
);
comment on table client_registrations IS 'OAuth2 clients registrations';
comment on column client_registrations.client_id IS 'ID of OAuth2 client';
comment on column client_registrations.client_secret_enc IS 'Encrypted client secret';
comment on column client_registrations.client_type IS 'Client type: confidential or public';
comment on column client_registrations.redirect_url IS 'Redirect URL following successful user authentication';
comment on column client_registrations.client_name IS 'Name of OAuth2 client';
comment on column client_registrations.client_description IS 'Description of OAuth2 client';

--rollback DROP TABLE client_registrations;

--changeset Vitalii:2 context:auth-server
--comment: Create table that stores registered users
create table users
(
    user_id      uuid not null,
    username varchar(255) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    phone_number varchar(30) not null,
    password varchar(300) not null,
    primary key (user_id)
);
comment on table users IS 'Registered users';
comment on column users.user_id IS 'ID of user';
comment on column users.username IS 'Username';
comment on column users.first_name IS 'First name';
comment on column users.last_name IS 'Last name';
comment on column users.phone_number IS 'Phone number';
comment on column users.password IS 'Encrypted password';

--rollback DROP TABLE users;

--changeset Vitalii:3 context:auth-server
--comment: Create table that stores authorization requests
create table authorization_requests
(
    id      uuid not null,
    username varchar(255) not null,
    response_type varchar(100) not null,
    client_id uuid not null,
    scope varchar(120) not null,
    state varchar(255),
    primary key (id)
);
comment on table authorization_requests IS 'Authorization requests';
comment on column authorization_requests.id IS 'ID of auth request';
comment on column authorization_requests.username IS 'Expected approve user';
comment on column authorization_requests.response_type IS 'Response type (code, token etc)';
comment on column authorization_requests.client_id IS 'OAuth2 client';
comment on column authorization_requests.scope IS 'Scope of authorization requests';
comment on column authorization_requests.state IS 'State that was passed to authorization request';

--rollback DROP TABLE authorization_requests;

--changeset Vitalii:4 context:auth-server
--comment: Add redirect_url column to authorization_requests
alter table authorization_requests add column redirect_url varchar(400);
comment on column authorization_requests.redirect_url is 'Redirect URL after request approval or denial';

