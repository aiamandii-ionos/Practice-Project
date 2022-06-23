    CREATE TABLE request (
      id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v1(),
      resource_id UUID NOT NULL,
      type VARCHAR(100) NOT NULL,
      status VARCHAR(100) NOT NULL,
      properties VARCHAR(500),
      message VARCHAR(500),
      user_id UUID NOT NULL,
      created_at TIMESTAMP,
      FOREIGN KEY(resource_id) REFERENCES server(id)
  );