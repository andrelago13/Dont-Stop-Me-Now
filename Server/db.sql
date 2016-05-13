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
	creator INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
	type EVENTTYPE NOT NULL,
	description TEXT NOT NULL,
	location TEXT,
	coords POINT,
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
	creator INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
	event INTEGER REFERENCES events(id) ON DELETE CASCADE NOT NULL,
	type BOOLEAN NOT NULL
);

DROP TABLE IF EXISTS comments CASCADE;
CREATE TABLE comments
(
	id SERIAL PRIMARY KEY,
	writer INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
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
INSERT INTO events (creator, type, description, location) VALUES (1, 0, 'Toyota azul com radar.', 'Em frente à Makro, na Via Norte, sentido Porto - Maia.');
INSERT INTO comments (writer, event, message) VALUES (1, 1, 'Test comment.');
INSERT INTO confirmations (creator, event, type) VALUES (1, 1, TRUE);