package net.jfabricationgames.genesis_project_server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.jfabricationgames.genesis_project_server.exception.GameDataException;

class GameDataManagerTest {
	
	@Test
	public void testBuildGameListQuery() throws GameDataException {
		final String tableGames = "genesis_project.games";
		final String tableUsers = "genesis_project.users";
		final String tablePlayers = "genesis_project.players";
		
		GameDataManager manager = new GameDataManager();
		String query_complete_noUser = manager.buildGameListQuery(true, "-", tableGames, tablePlayers, tableUsers);
		String query_complete_user42 = manager.buildGameListQuery(true, "user42", tableGames, tablePlayers, tableUsers);
		String query_incomplete_noUser = manager.buildGameListQuery(false, "-", tableGames, tablePlayers, tableUsers);
		String query_incomplete_user42 = manager.buildGameListQuery(false, "user42", tableGames, tablePlayers, tableUsers);
		
		assertEquals("SELECT g.id, g.started, g.last_played, g.data FROM " + tableGames + " g", query_complete_noUser);
		assertEquals("SELECT g.id, g.started, g.last_played, g.data FROM " + tableGames + " g JOIN " + tablePlayers + " p ON g.id = p.game_id JOIN "
				+ tableUsers + " u ON u.id = p.user_id WHERE u.username = ?", query_complete_user42);
		assertEquals("SELECT g.id, g.started, g.last_played FROM " + tableGames + " g", query_incomplete_noUser);
		assertEquals("SELECT g.id, g.started, g.last_played FROM " + tableGames + " g JOIN " + tablePlayers + " p ON g.id = p.game_id JOIN "
				+ tableUsers + " u ON u.id = p.user_id WHERE u.username = ?", query_incomplete_user42);
	}
	
	@Test
	public void testBuildMoveListQuery() throws GameDataException {
		final String tableMoves = "genesis_project.moves";
		final String tableUsers = "genesis_project.users";
		
		GameDataManager manager = new GameDataManager();
		String query_allGames_allUsers_allMoves = manager.buildMoveListQuery(true, true, true, tableMoves, tableUsers);
		String query_selectedGames_allUsers_allMoves = manager.buildMoveListQuery(false, true, true, tableMoves, tableUsers);
		String query_allGames_selectedUsers_allMoves = manager.buildMoveListQuery(true, false, true, tableMoves, tableUsers);
		String query_allGames_allUsers_selectedMoves = manager.buildMoveListQuery(true, true, false, tableMoves, tableUsers);
		String query_selectedGames_selectedUsers_allMoves = manager.buildMoveListQuery(false, false, true, tableMoves, tableUsers);
		String query_selectedGames_allUsers_selectedMoves = manager.buildMoveListQuery(false, true, false, tableMoves, tableUsers);
		String query_allGames_selectedUsers_selectedMoves = manager.buildMoveListQuery(true, false, false, tableMoves, tableUsers);
		String query_selectedGames_selectedUsers_selectedMoves = manager.buildMoveListQuery(false, false, false, tableMoves, tableUsers);
		
		assertEquals("SELECT m.num, m.move FROM " + tableMoves + " m WHERE 1 ORDER BY m.num DESC", query_allGames_allUsers_allMoves);
		
		assertEquals("SELECT m.num, m.move FROM " + tableMoves + " m WHERE m.game_id = ? ORDER BY m.num DESC", query_selectedGames_allUsers_allMoves);
		
		assertEquals("SELECT m.num, m.move FROM " + tableMoves + " m JOIN " + tableUsers
				+ " u ON u.id = m.user_id WHERE u.username = ? ORDER BY m.num DESC", query_allGames_selectedUsers_allMoves);
		
		assertEquals("SELECT m.num, m.move FROM " + tableMoves + " m WHERE 1 ORDER BY m.num DESC LIMIT ?", query_allGames_allUsers_selectedMoves);
		
		assertEquals(
				"SELECT m.num, m.move FROM " + tableMoves + " m JOIN " + tableUsers
						+ " u ON u.id = m.user_id WHERE u.username = ? AND m.game_id = ? ORDER BY m.num DESC",
				query_selectedGames_selectedUsers_allMoves);
		
		assertEquals("SELECT m.num, m.move FROM " + tableMoves + " m WHERE m.game_id = ? ORDER BY m.num DESC LIMIT ?",
				query_selectedGames_allUsers_selectedMoves);
		
		assertEquals("SELECT m.num, m.move FROM " + tableMoves + " m JOIN " + tableUsers
				+ " u ON u.id = m.user_id WHERE u.username = ? ORDER BY m.num DESC LIMIT ?", query_allGames_selectedUsers_selectedMoves);
		
		assertEquals(
				"SELECT m.num, m.move FROM " + tableMoves + " m JOIN " + tableUsers
						+ " u ON u.id = m.user_id WHERE u.username = ? AND m.game_id = ? ORDER BY m.num DESC LIMIT ?",
				query_selectedGames_selectedUsers_selectedMoves);
	}
}