package net.jfabricationgames.genesis_project_server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GameDataManagerTest {
	
	@Test
	public void testBuildGameListQuery() {
		GameDataManager manager = new GameDataManager();
		String query_complete_noUser = manager.buildGameListQuery(true, "-");
		String query_complete_user42 = manager.buildGameListQuery(true, "user42");
		String query_incomplete_noUser = manager.buildGameListQuery(false, "-");
		String query_incomplete_user42 = manager.buildGameListQuery(false, "user42");
		
		assertEquals("SELECT g.id, g.started, g.last_played, g.data FROM " + manager.getTable(GameDataManager.TABLE_GAMES) + " g",
				query_complete_noUser);
		assertEquals("SELECT g.id, g.started, g.last_played, g.data FROM " + manager.getTable(GameDataManager.TABLE_GAMES) + " g JOIN "
				+ manager.getTable(GameDataManager.TABLE_PLAYERS) + " p ON g.id = p.game_id JOIN " + manager.getTable(GameDataManager.TABLE_USERS)
				+ " u ON u.id = p.user_id WHERE u.username = ?", query_complete_user42);
		assertEquals("SELECT g.id, g.started, g.last_played FROM " + manager.getTable(GameDataManager.TABLE_GAMES) + " g", query_incomplete_noUser);
		assertEquals("SELECT g.id, g.started, g.last_played FROM " + manager.getTable(GameDataManager.TABLE_GAMES) + " g JOIN "
				+ manager.getTable(GameDataManager.TABLE_PLAYERS) + " p ON g.id = p.game_id JOIN " + manager.getTable(GameDataManager.TABLE_USERS)
				+ " u ON u.id = p.user_id WHERE u.username = ?", query_incomplete_user42);
	}
	
	@Test
	public void testBuildMoveListQuery() {
		GameDataManager manager = new GameDataManager();
		String query_allGames_allUsers_allMoves = manager.buildMoveListQuery(true, true, true);
		String query_selectedGames_allUsers_allMoves = manager.buildMoveListQuery(false, true, true);
		String query_allGames_selectedUsers_allMoves = manager.buildMoveListQuery(true, false, true);
		String query_allGames_allUsers_selectedMoves = manager.buildMoveListQuery(true, true, false);
		String query_selectedGames_selectedUsers_allMoves = manager.buildMoveListQuery(false, false, true);
		String query_selectedGames_allUsers_selectedMoves = manager.buildMoveListQuery(false, true, false);
		String query_allGames_selectedUsers_selectedMoves = manager.buildMoveListQuery(true, false, false);
		String query_selectedGames_selectedUsers_selectedMoves = manager.buildMoveListQuery(false, false, false);
		
		assertEquals("SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m WHERE 1 ORDER BY m.num DESC",
				query_allGames_allUsers_allMoves);
		
		assertEquals(
				"SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m WHERE m.game_id = ? ORDER BY m.num DESC",
				query_selectedGames_allUsers_allMoves);
		
		assertEquals(
				"SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m JOIN "
						+ manager.getTable(GameDataManager.TABLE_USERS) + " u ON u.id = m.user_id WHERE u.username = ? ORDER BY m.num DESC",
				query_allGames_selectedUsers_allMoves);
		
		assertEquals("SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m WHERE 1 ORDER BY m.num DESC LIMIT ?",
				query_allGames_allUsers_selectedMoves);
		
		assertEquals("SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m JOIN "
				+ manager.getTable(GameDataManager.TABLE_USERS) + " u ON u.id = m.user_id WHERE u.username = ? AND m.game_id = ? ORDER BY m.num DESC",
				query_selectedGames_selectedUsers_allMoves);
		
		assertEquals("SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES)
				+ " m WHERE m.game_id = ? ORDER BY m.num DESC LIMIT ?", query_selectedGames_allUsers_selectedMoves);
		
		assertEquals(
				"SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m JOIN "
						+ manager.getTable(GameDataManager.TABLE_USERS) + " u ON u.id = m.user_id WHERE u.username = ? ORDER BY m.num DESC LIMIT ?",
				query_allGames_selectedUsers_selectedMoves);
		
		assertEquals(
				"SELECT m.id, m.num, m.move FROM " + manager.getTable(GameDataManager.TABLE_MOVES) + " m JOIN "
						+ manager.getTable(GameDataManager.TABLE_USERS)
						+ " u ON u.id = m.user_id WHERE u.username = ? AND m.game_id = ? ORDER BY m.num DESC LIMIT ?",
				query_selectedGames_selectedUsers_selectedMoves);
	}
}