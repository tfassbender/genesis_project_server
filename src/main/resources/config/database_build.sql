CREATE DATABASE IF NOT EXISTS genesis_project;

CREATE TABLE IF NOT EXISTS genesis_project.users (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(50) UNIQUE KEY,
	password VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS genesis_project.games (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	active BOOLEAN,
	started DATE,
	last_played DATE,
	data TEXT
);

CREATE TABLE IF NOT EXISTS genesis_project.players (
	user_id INT REFERENCES genesis_project.users (id),
	game_id INT REFERENCES genesis_project.games (id),
	PRIMARY KEY (user_id, game_id)
);

CREATE TABLE IF NOT EXISTS genesis_project.moves (
	user_id INT REFERENCES genesis_project.users (id),
	game_id INT REFERENCES genesis_project.games (id),
	move TEXT,
	num INT,
	PRIMARY KEY (user_id, game_id)
);