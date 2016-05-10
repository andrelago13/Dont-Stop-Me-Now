DROP TABLE IF EXISTS events;
CREATE TABLE events
(
  id SERIAL PRIMARY KEY,
  description text NOT NULL
);


INSERT INTO events (description) VALUES ('Toyota azul em frente à macro na Via Norte, sentido Porto - Maia.');