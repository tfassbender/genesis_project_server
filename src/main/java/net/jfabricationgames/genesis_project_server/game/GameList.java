package net.jfabricationgames.genesis_project_server.game;

import java.util.Map;

public class GameList {
	
	/**
	 * Maps the game id to the game content as JSON.
	 */
	private Map<Integer, String> games;
	/**
	 * Maps the game id to the date the game was started.
	 */
	private Map<Integer, String> started;
	/**
	 * Maps the game id to the date the game was played the last time.
	 */
	private Map<Integer, String> lastPlayed;
	
	public GameList() {
		
	}
	
	public Map<Integer, String> getGames() {
		return games;
	}
	public void setGames(Map<Integer, String> games) {
		this.games = games;
	}
	
	public Map<Integer, String> getLastPlayed() {
		return lastPlayed;
	}
	public void setLastPlayed(Map<Integer, String> lastPlayed) {
		this.lastPlayed = lastPlayed;
	}
	
	public Map<Integer, String> getStarted() {
		return started;
	}
	public void setStarted(Map<Integer, String> started) {
		this.started = started;
	}
}