-- Таблица ratings
CREATE TABLE IF NOT EXISTS ratings (
    id SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE
);

-- Таблица genres
CREATE TABLE IF NOT EXISTS genres (
    id SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE
);

-- Таблица films
CREATE TABLE IF NOT EXISTS films (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR(200),
    releaseDate DATE,
    duration INTEGER,
    id_rating INTEGER REFERENCES ratings(id)
);

-- Таблица users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    login VARCHAR NOT NULL,
    name VARCHAR,
    email VARCHAR,
    birthday DATE
);

-- Таблица friends
CREATE TABLE IF NOT EXISTS friends (
    id SERIAL PRIMARY KEY,
    _from INTEGER REFERENCES users(id),
    _to INTEGER REFERENCES users(id),
    status BOOLEAN
);

-- Таблица likes
CREATE TABLE IF NOT EXISTS likes (
    id SERIAL PRIMARY KEY,
    id_film INTEGER REFERENCES films(id),
    id_user INTEGER REFERENCES users(id)
);

-- Таблица films_genres
CREATE TABLE IF NOT EXISTS films_genres (
    id_film INTEGER REFERENCES films(id),
    id_genre INTEGER REFERENCES genres(id),
    PRIMARY KEY (id_film, id_genre)
);

MERGE INTO genres(name) KEY (name) VALUES ('Комедия');
MERGE INTO genres(name) KEY (name) VALUES ('Драма');
MERGE INTO genres(name) KEY (name) VALUES ('Мультфильм');
MERGE INTO genres(name) KEY (name) VALUES ('Триллер');
MERGE INTO genres(name) KEY (name) VALUES ('Документальный');
MERGE INTO genres(name) KEY (name) VALUES ('Боевик');

MERGE INTO ratings(name) KEY (name) VALUES ('G');
MERGE INTO ratings(name) KEY (name) VALUES ('PG');
MERGE INTO ratings(name) KEY (name) VALUES ('PG-13');
MERGE INTO ratings(name) KEY (name) VALUES ('R');
MERGE INTO ratings(name) KEY (name) VALUES ('NC-17');