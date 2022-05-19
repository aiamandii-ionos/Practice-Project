CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE server (
    id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v1(),
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    cores INTEGER NOT NULL,
    ram INTEGER NOT NULL,
    storage INTEGER NOT NULL
);

INSERT INTO server(user_id,name, cores, ram, storage) VALUES ('4365fd20-f38b-4e37-8e5c-0c4bd788e894','server1', 4, 34, 33);