CREATE table foo (
    id SERIAL PRIMARY KEY,
    date_created timestamp,
    last_updated timestamp,
    a varchar,
    b int
);

CREATE TABLE person (
    id SERIAL PRIMARY KEY,
    date_created timestamp,
    last_updated timestamp,
    name varchar,
    age int
)
