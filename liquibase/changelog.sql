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


