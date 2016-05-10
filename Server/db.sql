DROP TABLE IF EXISTS users;
CREATE TABLE users
(
	id SERIAL PRIMARY KEY,
	facebookID text UNIQUE NOT NULL
);

DROP TABLE IF EXISTS events;
CREATE TABLE events
(
	id SERIAL PRIMARY KEY,
	description TEXT NOT NULL,
	location TEXT,
	coords POINT,
	datetime TIMESTAMP NOT NULL DEFAULT(CURRENT_TIMESTAMP),
	CONSTRAINT has_location CHECK (location IS NOT NULL OR coords IS NOT NULL)
);

DROP TABLE IF EXISTS confirmations;
CREATE TABLE confirmations
(
	id SERIAL PRIMARY KEY,
	type BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS comments;
CREATE TABLE comments
(
	id SERIAL PRIMARY KEY,
	message TEXT NOT NULL,
	datetime TIMESTAMP NOT NULL DEFAULT(CURRENT_TIMESTAMP)
);

INSERT INTO events (description, location) VALUES ('Toyota azul com radar.', 'Em frente à Makro, na Via Norte, sentido Porto - Maia.');