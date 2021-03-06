PGDMP     )    1                t           postgres    9.5.3    9.5.3 4    �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                       false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                       false            �           1262    12373    postgres    DATABASE     �   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'Portuguese_Portugal.1252' LC_CTYPE = 'Portuguese_Portugal.1252';
    DROP DATABASE postgres;
             postgres    false            �           1262    12373    postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                  postgres    false    3477            	            2615    2200    public    SCHEMA        CREATE SCHEMA public;
    DROP SCHEMA public;
             postgres    false            �           0    0    SCHEMA public    COMMENT     6   COMMENT ON SCHEMA public IS 'standard public schema';
                  postgres    false    9            �           0    0    public    ACL     �   REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;
                  postgres    false    9                        3079    12355    plpgsql 	   EXTENSION     ?   CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
    DROP EXTENSION plpgsql;
                  false            �           0    0    EXTENSION plpgsql    COMMENT     @   COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';
                       false    2                        3079    16384 	   adminpack 	   EXTENSION     A   CREATE EXTENSION IF NOT EXISTS adminpack WITH SCHEMA pg_catalog;
    DROP EXTENSION adminpack;
                  false            �           0    0    EXTENSION adminpack    COMMENT     M   COMMENT ON EXTENSION adminpack IS 'administrative functions for PostgreSQL';
                       false    1                        3079    16529    postgis 	   EXTENSION     ;   CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;
    DROP EXTENSION postgis;
                  false    9            �           0    0    EXTENSION postgis    COMMENT     g   COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';
                       false    3            �           1247    17895 	   eventtype    DOMAIN     g   CREATE DOMAIN eventtype AS integer
	CONSTRAINT eventtype_check CHECK ((VALUE = ANY (ARRAY[0, 1, 2])));
    DROP DOMAIN public.eventtype;
       public       postgres    false    9            1           1255    16438    update_confirmations()    FUNCTION     O  CREATE FUNCTION update_confirmations() RETURNS trigger
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
       public       postgres    false    9    2            �            1259    17951    comments    TABLE     �   CREATE TABLE comments (
    id integer NOT NULL,
    writer integer NOT NULL,
    event integer NOT NULL,
    message text NOT NULL,
    datetime timestamp without time zone DEFAULT now() NOT NULL
);
    DROP TABLE public.comments;
       public         postgres    false    9            �            1259    17949    comments_id_seq    SEQUENCE     q   CREATE SEQUENCE comments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.comments_id_seq;
       public       postgres    false    205    9            �           0    0    comments_id_seq    SEQUENCE OWNED BY     5   ALTER SEQUENCE comments_id_seq OWNED BY comments.id;
            public       postgres    false    204            �            1259    17933    confirmations    TABLE     �   CREATE TABLE confirmations (
    id integer NOT NULL,
    creator integer NOT NULL,
    event integer NOT NULL,
    type boolean NOT NULL
);
 !   DROP TABLE public.confirmations;
       public         postgres    false    9            �            1259    17931    confirmations_id_seq    SEQUENCE     v   CREATE SEQUENCE confirmations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 +   DROP SEQUENCE public.confirmations_id_seq;
       public       postgres    false    203    9            �           0    0    confirmations_id_seq    SEQUENCE OWNED BY     ?   ALTER SEQUENCE confirmations_id_seq OWNED BY confirmations.id;
            public       postgres    false    202            �            1259    17912    events    TABLE     F  CREATE TABLE events (
    id integer NOT NULL,
    creator integer NOT NULL,
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
       public         postgres    false    1757    3    3    9    9    3    9    3    9    3    9    3    9    3    9    3    9    9            �            1259    17910    events_id_seq    SEQUENCE     o   CREATE SEQUENCE events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 $   DROP SEQUENCE public.events_id_seq;
       public       postgres    false    201    9            �           0    0    events_id_seq    SEQUENCE OWNED BY     1   ALTER SEQUENCE events_id_seq OWNED BY events.id;
            public       postgres    false    200            �            1259    17899    users    TABLE     N   CREATE TABLE users (
    id integer NOT NULL,
    facebookid text NOT NULL
);
    DROP TABLE public.users;
       public         postgres    false    9            �            1259    17897    users_id_seq    SEQUENCE     n   CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 #   DROP SEQUENCE public.users_id_seq;
       public       postgres    false    9    199            �           0    0    users_id_seq    SEQUENCE OWNED BY     /   ALTER SEQUENCE users_id_seq OWNED BY users.id;
            public       postgres    false    198            �           2604    17992    id    DEFAULT     \   ALTER TABLE ONLY comments ALTER COLUMN id SET DEFAULT nextval('comments_id_seq'::regclass);
 :   ALTER TABLE public.comments ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    205    204    205            �           2604    17993    id    DEFAULT     f   ALTER TABLE ONLY confirmations ALTER COLUMN id SET DEFAULT nextval('confirmations_id_seq'::regclass);
 ?   ALTER TABLE public.confirmations ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    203    202    203            �           2604    17994    id    DEFAULT     X   ALTER TABLE ONLY events ALTER COLUMN id SET DEFAULT nextval('events_id_seq'::regclass);
 8   ALTER TABLE public.events ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    201    200    201            �           2604    17995    id    DEFAULT     V   ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);
 7   ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
       public       postgres    false    198    199    199            �          0    17951    comments 
   TABLE DATA               A   COPY comments (id, writer, event, message, datetime) FROM stdin;
    public       postgres    false    205   :       �           0    0    comments_id_seq    SEQUENCE SET     6   SELECT pg_catalog.setval('comments_id_seq', 1, true);
            public       postgres    false    204            �          0    17933    confirmations 
   TABLE DATA               :   COPY confirmations (id, creator, event, type) FROM stdin;
    public       postgres    false    203   V:       �           0    0    confirmations_id_seq    SEQUENCE SET     ;   SELECT pg_catalog.setval('confirmations_id_seq', 1, true);
            public       postgres    false    202            �          0    17912    events 
   TABLE DATA               �   COPY events (id, creator, type, description, location, coords, photo, datetime, positiveconfirmations, negativeconfirmations) FROM stdin;
    public       postgres    false    201   y:       �           0    0    events_id_seq    SEQUENCE SET     4   SELECT pg_catalog.setval('events_id_seq', 1, true);
            public       postgres    false    200            �          0    16819    spatial_ref_sys 
   TABLE DATA               "   COPY spatial_ref_sys  FROM stdin;
    public       postgres    false    184   *;       �          0    17899    users 
   TABLE DATA               (   COPY users (id, facebookid) FROM stdin;
    public       postgres    false    199   G;       �           0    0    users_id_seq    SEQUENCE SET     3   SELECT pg_catalog.setval('users_id_seq', 1, true);
            public       postgres    false    198            	           2606    17960    comments_pkey 
   CONSTRAINT     M   ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.comments DROP CONSTRAINT comments_pkey;
       public         postgres    false    205    205                       2606    17938    confirmations_pkey 
   CONSTRAINT     W   ALTER TABLE ONLY confirmations
    ADD CONSTRAINT confirmations_pkey PRIMARY KEY (id);
 J   ALTER TABLE ONLY public.confirmations DROP CONSTRAINT confirmations_pkey;
       public         postgres    false    203    203                       2606    17925    events_pkey 
   CONSTRAINT     I   ALTER TABLE ONLY events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);
 <   ALTER TABLE ONLY public.events DROP CONSTRAINT events_pkey;
       public         postgres    false    201    201                        2606    17909    users_facebookid_key 
   CONSTRAINT     T   ALTER TABLE ONLY users
    ADD CONSTRAINT users_facebookid_key UNIQUE (facebookid);
 D   ALTER TABLE ONLY public.users DROP CONSTRAINT users_facebookid_key;
       public         postgres    false    199    199                       2606    17907 
   users_pkey 
   CONSTRAINT     G   ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public         postgres    false    199    199                       1259    17971    coords_lat_lng_index    INDEX     A   CREATE INDEX coords_lat_lng_index ON events USING gist (coords);
 (   DROP INDEX public.coords_lat_lng_index;
       public         postgres    false    201    3    3    9    3    3    9    9    3    9    9    3    3    9    9    3    9    3    9    3    9    3    9    3    9    3    9                       2620    17972    update_confirmations    TRIGGER     �   CREATE TRIGGER update_confirmations AFTER INSERT OR UPDATE OF type ON confirmations FOR EACH ROW EXECUTE PROCEDURE update_confirmations();
 ;   DROP TRIGGER update_confirmations ON public.confirmations;
       public       postgres    false    203    203    1329                       2606    17966    comments_event_fkey    FK CONSTRAINT     ~   ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_event_fkey FOREIGN KEY (event) REFERENCES events(id) ON DELETE CASCADE;
 F   ALTER TABLE ONLY public.comments DROP CONSTRAINT comments_event_fkey;
       public       postgres    false    205    201    3333                       2606    17961    comments_writer_fkey    FK CONSTRAINT        ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_writer_fkey FOREIGN KEY (writer) REFERENCES users(id) ON DELETE CASCADE;
 G   ALTER TABLE ONLY public.comments DROP CONSTRAINT comments_writer_fkey;
       public       postgres    false    205    3330    199                       2606    17939    confirmations_creator_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY confirmations
    ADD CONSTRAINT confirmations_creator_fkey FOREIGN KEY (creator) REFERENCES users(id) ON DELETE CASCADE;
 R   ALTER TABLE ONLY public.confirmations DROP CONSTRAINT confirmations_creator_fkey;
       public       postgres    false    203    199    3330                       2606    17944    confirmations_event_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY confirmations
    ADD CONSTRAINT confirmations_event_fkey FOREIGN KEY (event) REFERENCES events(id) ON DELETE CASCADE;
 P   ALTER TABLE ONLY public.confirmations DROP CONSTRAINT confirmations_event_fkey;
       public       postgres    false    203    3333    201            
           2606    17926    events_creator_fkey    FK CONSTRAINT     }   ALTER TABLE ONLY events
    ADD CONSTRAINT events_creator_fkey FOREIGN KEY (creator) REFERENCES users(id) ON DELETE CASCADE;
 D   ALTER TABLE ONLY public.events DROP CONSTRAINT events_creator_fkey;
       public       postgres    false    3330    201    199            �   :   x�3�4�������Լ=N#C3]S]#SC3+S+c=s3sc�=... Mr      �      x�3�4��=... ��      �   �   x��;�0Dk�{��Z�k�S���Q�YA�"H,�P�i��0Ӽ�L1Zh���ae����0C�3G%�.q\�>o��5����Ї���S;��S����J�NN2��?$,�MU4֑6�A��v��[�*"B1� �9��8оveMVQ����rPR�/�m-G      �      x������ � �      �      x�3�44 C3ScK�=... 1��     