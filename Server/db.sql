CREATE EXTENSION IF NOT EXISTS postgis;

-- 0 -> Mobile speed radar --
-- 1 -> Traffic stop --
-- 2 -> High traffic area --
-- 3 -> Car crash --
DROP DOMAIN IF EXISTS EVENTTYPE CASCADE;
CREATE DOMAIN EVENTTYPE AS INTEGER
	CHECK (VALUE IN (0, 1, 2, 3));

DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users
(
	facebookID TEXT PRIMARY KEY,
	address INET,
	port INTEGER,
	coords GEOGRAPHY(POINT,4326),
	radius NUMERIC(7, 3),
	CONSTRAINT positive_radius CHECK (radius IS NULL OR radius > 0),
	CONSTRAINT notify_fully_set_or_empty CHECK ((address IS NULL AND port IS NULL AND coords IS NULL AND radius IS NULL) OR (address IS NOT NULL AND port IS NOT NULL AND coords IS NOT NULL AND radius IS NOT NULL))
);

DROP TABLE IF EXISTS events CASCADE;
CREATE TABLE events
(
	id SERIAL PRIMARY KEY,
	creator TEXT REFERENCES users(facebookID) ON DELETE CASCADE NOT NULL,
	type EVENTTYPE NOT NULL,
	description TEXT NOT NULL,
	location TEXT,
	coords GEOGRAPHY(POINT,4326),
	photo BYTEA,
	datetime TIMESTAMP NOT NULL DEFAULT(CURRENT_TIMESTAMP),
	positiveconfirmations INT NOT NULL DEFAULT 0,
	negativeconfirmations INT NOT NULL DEFAULT 0,
	CONSTRAINT has_location CHECK (location IS NOT NULL OR coords IS NOT NULL),
	CONSTRAINT positive_numconfirmations CHECK (positiveconfirmations >= 0 AND negativeconfirmations >= 0)
);

DROP TABLE IF EXISTS confirmations CASCADE;
CREATE TABLE confirmations
(
	id SERIAL PRIMARY KEY,
	creator TEXT REFERENCES users(facebookID) ON DELETE CASCADE NOT NULL,
	event INTEGER REFERENCES events(id) ON DELETE CASCADE NOT NULL,
	type BOOLEAN NOT NULL,
	UNIQUE(creator, event)
);

DROP TABLE IF EXISTS comments CASCADE;
CREATE TABLE comments
(
	id SERIAL PRIMARY KEY,
	writer TEXT REFERENCES users(facebookID) ON DELETE CASCADE NOT NULL,
	event INTEGER REFERENCES events(id) ON DELETE CASCADE NOT NULL,
	message TEXT NOT NULL,
	datetime TIMESTAMP NOT NULL DEFAULT(CURRENT_TIMESTAMP)
);

CREATE INDEX coords_lat_lng_index ON events USING gist(coords);

CREATE OR REPLACE FUNCTION update_confirmations() RETURNS TRIGGER AS 
$function$
BEGIN
	IF (TG_OP = 'DELETE') THEN
		IF (OLD.type) THEN
			UPDATE events SET positiveconfirmations = positiveconfirmations - 1;
		ELSE
			UPDATE events SET negativeconfirmations = negativeconfirmations - 1;
		END IF;
		RETURN NULL;
	END IF;
	
	IF (TG_OP = 'UPDATE') THEN
		IF (OLD.type = NEW.type) THEN
			RETURN NEW;
		ELSE
			IF (OLD.type) THEN
				UPDATE events SET positiveconfirmations = positiveconfirmations - 1;
			ELSE
				UPDATE events SET negativeconfirmations = negativeconfirmations - 1;
			END IF;
		END IF;
	END IF;
	
	IF (NEW.type) THEN
		UPDATE events SET positiveconfirmations = positiveconfirmations + 1;
	ELSE
		UPDATE events SET negativeconfirmations = negativeconfirmations + 1;
	END IF;
	RETURN NEW;
END;
$function$
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_confirmations ON events;
CREATE TRIGGER update_confirmations
	AFTER INSERT OR UPDATE OF type ON confirmations
	FOR EACH ROW
	EXECUTE PROCEDURE update_confirmations();

INSERT INTO users (facebookID) VALUES ('100000416538494');
INSERT INTO events (creator, type, description, location, coords) VALUES ('100000416538494', 0, 'Toyota azul com radar.', 'Em frente � Makro, na Via Norte, sentido Porto - Maia.', ST_GeomFromText('POINT(-8.6273612 41.2018094)'));
INSERT INTO comments (writer, event, message) VALUES ('100000416538494', 1, 'Test comment.');
INSERT INTO confirmations (creator, event, type) VALUES ('100000416538494', 1, TRUE);