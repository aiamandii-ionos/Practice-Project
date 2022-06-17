  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE TABLE server (
      id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v1(),
      user_id UUID NOT NULL,
      datacenter_id UUID NOT NULL,
      server_ionos_id UUID NOT NULL,
      ipblock_ionos_id UUID NOT NULL,
      volume_id UUID NOT NULL,
      ip VARCHAR(100) NOT NULL,
      name VARCHAR(100) NOT NULL,
      cores INTEGER NOT NULL,
      ram INTEGER NOT NULL,
      storage INTEGER NOT NULL,
      private_key VARCHAR(4096) NOT NULL
  );