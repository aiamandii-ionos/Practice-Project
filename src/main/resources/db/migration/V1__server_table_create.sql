  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  CREATE TABLE server (
      id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v1(),
      user_id UUID NOT NULL,
      datacenter_id UUID,
      server_ionos_id UUID,
      ipblock_ionos_id UUID,
      volume_id UUID,
      ip VARCHAR(100),
      name VARCHAR(100) NOT NULL,
      cores INTEGER NOT NULL,
      ram INTEGER NOT NULL,
      storage INTEGER NOT NULL,
      private_key VARCHAR(4096)
  );