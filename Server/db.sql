CREATE EXTENSION IF NOT EXISTS postgis;

-- 0 -> Mobile speed radar --
-- 1 -> Traffic stop --
-- 2 -> High traffic area --
DROP DOMAIN IF EXISTS EVENTTYPE CASCADE;
CREATE DOMAIN EVENTTYPE AS INTEGER
	CHECK (VALUE IN (0, 1, 2));

DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users
(
	id SERIAL PRIMARY KEY,
	facebookID text UNIQUE NOT NULL
);

DROP TABLE IF EXISTS events CASCADE;
CREATE TABLE events
(
	id SERIAL PRIMARY KEY,
	type EVENTTYPE NOT NULL,
	description TEXT NOT NULL,
	location TEXT,
	coords POINT,
	datetime TIMESTAMP NOT NULL DEFAULT(CURRENT_TIMESTAMP),
	CONSTRAINT has_location CHECK (location IS NOT NULL OR coords IS NOT NULL)
);

DROP TABLE IF EXISTS confirmations CASCADE;
CREATE TABLE confirmations
(
	id SERIAL PRIMARY KEY,
	type BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS comments CASCADE;
CREATE TABLE comments
(
	id SERIAL PRIMARY KEY,
	message TEXT NOT NULL,
	datetime TIMESTAMP NOT NULL DEFAULT(CURRENT_TIMESTAMP)
);

INSERT INTO events (type, description, location) VALUES (0, 'Toyota azul com radar.', 'Em frente à Makro, na Via Norte, sentido Porto - Maia.');