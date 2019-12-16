package net.jfabricationgames.genesis_project_server.game;

import java.util.Map;

public class MoveList {
	
	/**
	 * Maps the number of the moves (n for the n-th move in the game) to the JSON representation of the moves.
	 */
	private Map<Integer, String> moves;
	/**
	 * Maps the id of the moves to the number of the moves;
	 */
	private Map<Integer, Integer> idToNum;
	
	public MoveList() {
		
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
}