package net.jfabricationgames.genesis_project_server.game;

import java.util.Map;

public class MoveList {
	
	/**
	 * Maps the id of the moves to the JSON representation of the moves.
	 */
	private Map<Integer, String> moves;
	
	/**
	 * Maps the id of the moves to the number of the moves (n for the n-th move in the game);
	 */
	private Map<Integer, Integer> idToNum;
	
	/**
	 * Maps the id of the moves to the name of the user who made the move
	 */
	public Map<Integer, String> idToUsername;
	
	public MoveList() {
		
	}
	
	@Override
	public String toString() {
		return "MoveList [moves=" + moves + ", idToNum=" + idToNum + ", idToUsername=" + idToUsername + "]";
	}
	
	public Map<Integer, String> getMoves() {
		return moves;
	}
	public void setMoves(Map<Integer, String> moves) {
		this.moves = moves;
	}
	
	public Map<Integer, Integer> getIdToNum() {
		return idToNum;
	}
	public void setIdToNum(Map<Integer, Integer> idToNum) {
		this.idToNum = idToNum;
	}
	
	public Map<Integer, String> getIdToUsername() {
		return idToUsername;
	}
	public void setIdToUsername(Map<Integer, String> idToUsername) {
		this.idToUsername = idToUsername;
	}
}