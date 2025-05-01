create extension if not exists "postgis";

CREATE OR REPLACE FUNCTION set_timestamp_fields()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.date_created := NOW();
        NEW.last_updated := NOW();
    ELSIF TG_OP = 'UPDATE' THEN
        NEW.last_updated := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    date_created timestamp not null,
    last_updated timestamp not null ,
    name varchar not null ,
    age int
);

CREATE TRIGGER set_person_timestamps
    BEFORE INSERT OR UPDATE ON person
    FOR EACH ROW
EXECUTE FUNCTION set_timestamp_fields();

CREATE TABLE address (
    id BIGSERIAL PRIMARY KEY,
    date_created timestamp not null,
    last_updated timestamp not null,
    person_id BIGINT not null
        constraint external_association_user_id_fk
        references person
        on delete cascade,
    street varchar,
    city varchar,
    state varchar,
    coordinates geometry
);

CREATE TRIGGER set_address_timestamps
    BEFORE INSERT OR UPDATE ON address
    FOR EACH ROW
EXECUTE FUNCTION set_timestamp_fields();

CREATE TABLE registered_user (
    id BIGSERIAL PRIMARY KEY,
    date_created timestamp not null,
    last_updated timestamp not null,
    email varchar unique not null,
    email_verified boolean not null,
    roles varchar[] not null,
    password_hash varchar not null,
    person_id BIGINT not null
        constraint registered_user_person_id_fk
        references person
        on delete cascade
);

CREATE TRIGGER set_registered_user_timestamps
    BEFORE INSERT OR UPDATE ON registered_user
    FOR EACH ROW
EXECUTE FUNCTION set_timestamp_fields();
