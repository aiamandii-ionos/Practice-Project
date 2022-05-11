CREATE TABLE server (
    id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v1(),
    name VARCHAR(100),
    cores INTEGER,
    ram INTEGER,
    storage INTEGER
);

INSERT INTO server(name, cores, ram, storage) VALUES ('server1', 4, 34, 33);