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
    state varchar
);

CREATE TRIGGER set_address_timestamps
    BEFORE INSERT OR UPDATE ON address
    FOR EACH ROW
EXECUTE FUNCTION set_timestamp_fields();
