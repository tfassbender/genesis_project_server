CREATE DATABASE IF NOT EXISTS <<DATABASE_NAME>>;

CREATE TABLE IF NOT EXISTS <<DATABASE_NAME>>.users (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(50) UNIQUE KEY,
	password VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS <<DATABASE_NAME>>.games (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	active BOOLEAN,
	started DATE,
	last_played DATE,
	data TEXT
);

CREATE TABLE IF NOT EXISTS <<DATABASE_NAME>>.players (
	user_id INT REFERENCES <<DATABASE_NAME>>.users (id),
	game_id INT REFERENCES <<DATABASE_NAME>>.games (id) ON DELETE CASCADE,
	PRIMARY KEY (user_id, game_id)
);

CREATE TABLE IF NOT EXISTS <<DATABASE_NAME>>.moves (
	user_id INT REFERENCES <<DATABASE_NAME>>.users (id),
	game_id INT REFERENCES <<DATABASE_NAME>>.games (id) ON DELETE CASCADE,
	move TEXT,
	num INT,
	PRIMARY KEY (user_id, game_id)
);