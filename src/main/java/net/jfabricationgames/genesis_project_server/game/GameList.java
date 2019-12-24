package net.jfabricationgames.genesis_project_server.game;

import java.time.LocalDate;
import java.util.Map;

public class GameList {
	
	/**
	 * Maps the game id to the game content as JSON.
	 */
	private Map<Integer, String> games;
	/**
	 * Maps the game id to the date the game was started.
	 */
	private Map<Integer, LocalDate> started;
	/**
	 * Maps the game id to the date the game was played the last time.
	 */
	private Map<Integer, LocalDate> lastPlayed;
	
	public GameList() {
		
	}
	
	@Override
	public String toString() {
		return "GameList [games=" + games + ", started=" + started + ", lastPlayed=" + lastPlayed + "]";
	}
	
	public Map<Integer, String> getGames() {
		return games;
	}
	public void setGames(Map<Integer, String> games) {
		this.games = games;
	}
	
	public Map<Integer, LocalDate> getLastPlayed() {
		return lastPlayed;
	}
	public void setLastPlayed(Map<Integer, LocalDate> lastPlayed) {
		this.lastPlayed = lastPlayed;
	}
	
	public Map<Integer, LocalDate> getStarted() {
		return started;
	}
	public void setStarted(Map<Integer, LocalDate> started) {
		this.started = started;
	}
}