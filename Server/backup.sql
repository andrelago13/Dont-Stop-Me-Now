PGDMP         /                t           postgres    9.5.3    9.5.3 /    �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                       false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                       false            �           1262    12373    postgres    DATABASE     �   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'Portuguese_Portugal.1252' LC_CTYPE = 'Portuguese_Portugal.1252';
    DROP DATABASE postgres;
             postgres    false            �           1262    12373    postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                  postgres    false    3474                        2615    2200    public    SCHEMA        CREATE SCHEMA public;
    DROP SCHEMA public;
             postgres    false            �           0    0    SCHEMA public    COMMENT     6   COMMENT ON SCHEMA public IS 'standard public schema';
                  postgres    false    8            �           0    0    public    ACL     �   REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;
                  postgres    false    8                        3079    12355    plpgsql 	   EXTENSION     ?   CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
    DROP EXTENSION plpgsql;
                  false            �           0    0    EXTENSION plpgsql    COMMENT     @   COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';
                       false    2                        3079    16384 	   adminpack 	   EXTENSION     A   CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;
    DROP EXTENSION adminpack;
                  false            �           0    0    EXTENSION adminpack    COMMENT     M   COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';
                       false    1                        3079    16393    postgis 	   EXTENSION     ;   CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;
    DROP EXTENSION postgis;
                  false    8            �           0    0    EXTENSION postgis    COMMENT     g   COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';
                       false    3            �           1247    17759 	   eventtype    DOMAIN     j   CREATE DOMAIN eventtype AS integer
	CONSTRAINT eventtype_check CHECK ((VALUE = ANY (ARRAY[0, 1, 2, 3])));
    DROP DOMAIN public.eventtype;
       public       postgres    false    8                        1255    17836    update_confirmations()    FUNCTION     O  CREATE FUNCTION update_confirmations() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;
 -   DROP FUNCTION public.update_confirmations();
       public       postgres    false    8    2            �            1259    17815    comments    TABLE     �   CREATE TABLE comments (
    id integer NOT NULL,
    writer text NOT NULL,
    event integer NOT NULL,
    message text NOT NULL,
    datetime timestamp without time zone DEFAULT now() NOT NULL
);
    DROP TABLE public.comments;
       public         postgres    false    8            �            1259    17813    comments_id_seq    SEQUENCE     q   CREATE SEQUENCE comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.comments_id_seq;
       public       postgres    false    204    8            �           0    0    comments_id_seq    SEQUENCE OWNED BY     5   ALTER SEQUENCE comments_id_seq OWNED BY comments.id;
            public       postgres    false    203            �            1259    17794    confirmations    TABLE     �   CREATE TABLE confirmations (
    id integer NOT NULL,
    creator text NOT NULL,
    event integer NOT NULL,
    type boolean NOT NULL
);
 !   DROP TABLE public.confirmations;
       public         postgres    false    8            �            1259    17792    confirmations_id_seq    SEQUENCE     v   CREATE SEQUENCE confirmations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 +   DROP SEQUENCE public.confirmations_id_seq;
       public       postgres    false    202    8            �           0    0    confirmations_id_seq    SEQUENCE OWNED BY     ?   ALTER SEQUENCE confirmations_id_seq OWNED BY confirmations.id;
            public       postgres    false    201            �            1259    17773    events    TABLE     C  CREATE TABLE events (
    id integer NOT NULL,
    creator text NOT NULL,
    type eventtype NOT NULL,
    description text NOT NULL,
    location text,
    coords geography(Point,4326),
    photo bytea,
    datetime timestamp without time zone DEFAULT now() NOT NULL,
    positiveconfirmations integer DEFAULT 0 NOT NULL,
    negativeconfirmations integer DEFAULT 0 NOT NULL,
    CONSTRAINT has_location CHECK (((location IS NOT NULL) OR (coords IS NOT NULL))),
    CONSTRAINT positive_numconfirmations CHECK (((positiveconfirmations >= 0) AND (negativeconfirmations >= 0)))
);
    DROP TABLE public.events;
       public         postgres    false    8    3    3    8    3    8    3    8    3    8    3    8    3    8    3    8    8    1756            �            1259    17771    events_id_seq    SEQUENCE     o   CREATE SEQUENCE events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 $   DROP SEQUENCE public.events_id_seq;
       public       postgres    false    200    8            �           0    0    events_id_seq    SEQUENCE OWNED BY     1   ALTER SEQUENCE events_id_seq OWNED BY events.id;
            public       postgres    false    199            �            1259    17761    users    TABLE     �  CREATE TABLE users (
    facebookid text NOT NULL,
    address text,
    port integer,
    coords geography(Point,4326),
    radius numeric(7,3),
    CONSTRAINT notify_fully_set_or_empty CHECK ((((address IS NULL) AND (port IS NULL) AND (coords IS NULL) AND (radius IS NULL)) OR ((address IS NOT NULL) AND (port IS NOT NULL) AND (coords IS NOT NULL) AND (radius IS NOT NULL)))),
    CONSTRAINT positive_radius CHECK (((radius IS NULL) OR (radius > (0)::numeric)))
);
    DROP TABLE public.users;
       public         postgres    false    8    3    3    8    3    8    3    8    3    8    3    8    3    8    3    8    8            �           2604    17818    id    DEFAULT     \   ALTER TABLE ONLY comments ALTER COLUMN id SET DEFAULT nextval('comments_id_seq'::regclass);
 :   ALTER TABLE public.comments ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    204    203    204            �           2604    17797    id    DEFAULT     f   ALTER TABLE ONLY confirmations ALTER COLUMN id SET DEFAULT nextval('confirmations_id_seq'::regclass);
 ?   ALTER TABLE public.confirmations ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    201    202    202            �           2604    17776    id    DEFAULT     X   ALTER TABLE ONLY events ALTER COLUMN id SET DEFAULT nextval('events_id_seq'::regclass);
 8   ALTER TABLE public.events ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    200    199    200            �          0    17815    comments 
   TABLE DATA               A   COPY comments (id, writer, event, message, datetime) FROM stdin;
    public       postgres    false    204   67       �           0    0    comments_id_seq    SEQUENCE SET     6   SELECT pg_catalog.setval('comments_id_seq', 1, true);
            public       postgres    false    203            �          0    17794    confirmations 
   TABLE DATA               :   COPY confirmations (id, creator, event, type) FROM stdin;
    public       postgres    false    202   �7       �           0    0    confirmations_id_seq    SEQUENCE SET     ;   SELECT pg_catalog.setval('confirmations_id_seq', 1, true);
            public       postgres    false    201            �          0    17773    events 
   TABLE DATA               �   COPY events (id, creator, type, description, location, coords, photo, datetime, positiveconfirmations, negativeconfirmations) FROM stdin;
    public       postgres    false    200   �7       �           0    0    events_id_seq    SEQUENCE SET     4   SELECT pg_catalog.setval('events_id_seq', 1, true);
            public       postgres    false    199            �          0    16683    spatial_ref_sys 
   TABLE DATA               "   COPY spatial_ref_sys  FROM stdin;
    public       postgres    false    184   u8       �          0    17761    users 
   TABLE DATA               C   COPY users (facebookid, address, port, coords, radius) FROM stdin;
    public       postgres    false    198   �8                  2606    17824    comments_pkey 
   CONSTRAINT     M   ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.comments DROP CONSTRAINT comments_pkey;
       public         postgres    false    204    204                       2606    17802    confirmations_pkey 
   CONSTRAINT     W   ALTER TABLE ONLY confirmations
    ADD CONSTRAINT confirmations_pkey PRIMARY KEY (id);
 J   ALTER TABLE ONLY public.confirmations DROP CONSTRAINT confirmations_pkey;
       public         postgres    false    202    202                       2606    17786    events_pkey 
   CONSTRAINT     I   ALTER TABLE ONLY events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);
 <   ALTER TABLE ONLY public.events DROP CONSTRAINT events_pkey;
       public         postgres    false    200    200                        2606    17770 
   users_pkey 
   CONSTRAINT     O   ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (facebookid);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public         postgres    false    198    198                       1259    17835    coords_lat_lng_index    INDEX     A   CREATE INDEX coords_lat_lng_index ON events USING gist (coords);
 (   DROP INDEX public.coords_lat_lng_index;
       public         postgres    false    200    3    8    3    3    8    8    3    8    3    3    8    3    8    3    8    3    8    3    8    3    8    3    8    8    3    8                       2620    17837    update_confirmations    TRIGGER     �   CREATE TRIGGER update_confirmations AFTER INSERT OR UPDATE OF type ON confirmations FOR EACH ROW EXECUTE PROCEDURE update_confirmations();
 ;   DROP TRIGGER update_confirmations ON public.confirmations;
       public       postgres    false    1312    202    202                       2606    17830    comments_event_fkey    FK CONSTRAINT     ~   ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_event_fkey FOREIGN KEY (event) REFERENCES events(id) ON DELETE CASCADE;
 F   ALTER TABLE ONLY public.comments DROP CONSTRAINT comments_event_fkey;
       public       postgres    false    204    200    3331                       2606    17825    comments_writer_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_writer_fkey FOREIGN KEY (writer) REFERENCES users(facebookid) ON DELETE CASCADE;
 G   ALTER TABLE ONLY public.comments DROP CONSTRAINT comments_writer_fkey;
       public       postgres    false    204    3328    198            	           2606    17803    confirmations_creator_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY confirmations
    ADD CONSTRAINT confirmations_creator_fkey FOREIGN KEY (creator) REFERENCES users(facebookid) ON DELETE CASCADE;
 R   ALTER TABLE ONLY public.confirmations DROP CONSTRAINT confirmations_creator_fkey;
       public       postgres    false    202    3328    198            
           2606    17808    confirmations_event_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY confirmations
    ADD CONSTRAINT confirmations_event_fkey FOREIGN KEY (event) REFERENCES events(id) ON DELETE CASCADE;
 P   ALTER TABLE ONLY public.confirmations DROP CONSTRAINT confirmations_event_fkey;
       public       postgres    false    202    200    3331                       2606    17787    events_creator_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY events
    ADD CONSTRAINT events_creator_fkey FOREIGN KEY (creator) REFERENCES users(facebookid) ON DELETE CASCADE;
 D   ALTER TABLE ONLY public.events DROP CONSTRAINT events_creator_fkey;
       public       postgres    false    198    3328    200            �   G   x�3�44 C3ScKNCΐ�������Լ=N#C3]S]#sCs++K=c3Ks�=... ��H      �       x�3�44 C3ScKNC��=... C��      �   �   x�%�=�@F��S����?K)B'�0V4��(l�b���.^L��}y_���g�)�
��/��~œ�0�ޅ�	�a���/ܹK�����ևe��ڱ��_��x=�.b�54��?F�h66�L*��"��4�6[�VD�4qS�9/EJ��)2�90������0      �      x������ � �      �      x�34 C3ScK�?8����� r|     