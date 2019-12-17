package net.jfabricationgames.genesis_project_server.game;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jfabricationgames.genesis_project_server.database.CheckedSqlConsumer;
import net.jfabricationgames.genesis_project_server.database.DatabaseConnection;
import net.jfabricationgames.genesis_project_server.database.SqlExecutionType;
import net.jfabricationgames.genesis_project_server.exception.GameDataException;
import net.jfabricationgames.genesis_project_server.exception.GameDataException.Cause;

public class GameDataManager {
	
	public static final String TABLE_GAMES = "games";
	public static final String TABLE_MOVES = "moves";
	public static final String TABLE_PLAYERS = "players";
	public static final String TABLE_USERS = "users";
	
	private String game;
	private int id;
	
	/**
	 * Update a game in the database.
	 * 
	 * @param id
	 *        The id of the game that is updated.
	 * 
	 * @param game
	 *        The game as JSON representation.
	 */
	public void updateGame(int id, String game) throws GameDataException {
		String query = "UPDATE " + getTable(TABLE_GAMES) + " SET data = ? WHERE id = ?";
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			ps.setString(1, game);
			ps.setInt(2, id);
		};
		
		int affectedRows = executeSQL(query, SqlExecutionType.UPDATE, variableSetter, null);
		
		//check whether a row was affected by the update (otherwise throw an exception)
		if (affectedRows == 0) {
			throw new GameDataException("no rows affected by update", Cause.NOT_FOUND);
		}
	}
	
	/**
	 * Get the last state of a game from the database.
	 * 
	 * @param id
	 *        The id of the game that is to be loaded.
	 * 
	 * @return The game (serialized as JSON)
	 */
	public String getGame(int id) throws GameDataException {
		String query = "SELECT data FROM " + getTable(TABLE_GAMES) + " WHERE id = ?";
		
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> ps.setInt(1, id);
		CheckedSqlConsumer<ResultSet> resultConsumer = resultSet -> {
			if (resultSet.next()) {
				game = resultSet.getString(1);
			}
			else {
				game = null;
			}
		};
		
		//execute the query that puts the result (the game as json string) in the global field game
		executeSQL(query, SqlExecutionType.QUERY, variableSetter, resultConsumer);
		
		if (game == null) {
			throw new GameDataException("result was not found", Cause.NOT_FOUND);
		}
		
		return game;
	}
	
	/**
	 * Create a new game.
	 * 
	 * @param players
	 *        The usernames of all players that take part in the game.
	 * 
	 * @return The ID of the game in the database.
	 */
	public int createGame(List<String> players) throws GameDataException {
		String queryCreateGame = "INSERT INTO " + getTable(TABLE_GAMES) + " (id, active, started, last_played, data) VALUES (0, 1, ?, ?, '')";
		
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			ps.setDate(1, Date.valueOf(LocalDate.now()));
			ps.setDate(2, Date.valueOf(LocalDate.now()));
		};
		CheckedSqlConsumer<ResultSet> resultConsumer = resultSet -> {
			if (resultSet.next()) {
				id = resultSet.getInt(1);
			}
			else {
				id = -1;
			}
		};
		
		//create the new game
		executeSQL(queryCreateGame, SqlExecutionType.CREATE, variableSetter, resultConsumer);
		
		if (id == -1) {
			throw new GameDataException("game could not be created", Cause.UNKNOWN);
		}
		
		String queryCreatePlayers = "INSERT INTO " + getTable(TABLE_PLAYERS) + " (user_id, game_id) VALUES ((SELECT id FROM " + getTable(TABLE_USERS)
				+ " u WHERE u.username = '?'), ?)";
		//add all players that participate in the game
		int affectedRows = 0;
		for (String player : players) {
			variableSetter = ps -> {
				ps.setString(1, player);
				ps.setInt(2, id);
			};
			
			//create the player
			affectedRows = executeSQL(queryCreatePlayers, SqlExecutionType.UPDATE, variableSetter, null);
			
			//check whether a row was affected by the update (otherwise throw an exception)
			if (affectedRows == 0) {
				throw new GameDataException("no rows affected by update", Cause.NOT_FOUND);
			}
		}
		
		return id;
	}
	
	/**
	 * Set a move made in a game.
	 * 
	 * @param gameId
	 *        The id of the game in which the move was made.
	 * 
	 * @param username
	 *        The name of the player who did the move.
	 * 
	 * @param move
	 *        The move in JSON representation.
	 */
	public void setMove(int gameId, String username, String move) throws GameDataException {
		//get the last move of the game to find the number of the next move
		MoveList moveList = listMoves(gameId, username, 1);
		int num = 1;
		Iterator<Integer> numKeys = moveList.getMoves().keySet().iterator();
		if (numKeys.hasNext()) {
			num = numKeys.next() + 1;
		}
		final int finalNum = num;
		
		String query = "INSERT INTO " + getTable(TABLE_MOVES) + " (user_id, game_id, move, num) VALUES ((SELECT u.id FROM " + getTable(TABLE_USERS)
				+ "u WHERE u.username = ?), ?, ?, ?)";
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			ps.setString(1, username);
			ps.setInt(2, gameId);
			ps.setString(3, move);
			ps.setInt(4, finalNum);
		};
		
		int affectedRows = executeSQL(query, SqlExecutionType.UPDATE, variableSetter, null);
		
		//check whether a row was affected by the update (otherwise throw an exception)
		if (affectedRows == 0) {
			throw new GameDataException("no rows affected by update", Cause.NOT_FOUND);
		}
	}
	
	/**
	 * Get a list of games in the database.
	 * 
	 * @param complete
	 *        Indicates whether complete games (true) or only ids (false) are requested.
	 * 
	 * @param username
	 *        The name of a user to search for user specific games ('-' for all users).
	 * 
	 * @return A {@link GameList} that contains the games.
	 */
	public GameList listGames(boolean complete, String username) throws GameDataException {
		boolean allUsers = username.equals("-");
		//build the query (depending on the parameters)
		String query = buildGameListQuery(complete, username);
		
		Map<Integer, String> games = new HashMap<Integer, String>();
		Map<Integer, LocalDate> started = new HashMap<Integer, LocalDate>();
		Map<Integer, LocalDate> lastPlayed = new HashMap<Integer, LocalDate>();
		
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			if (!allUsers) {
				ps.setString(1, username);
			}
		};
		CheckedSqlConsumer<ResultSet> resultConsumer = resultSet -> {
			//iterate over the result and add everything into the local maps
			if (resultSet.next()) {
				int id = resultSet.getInt(1);
				LocalDate startedDate = resultSet.getDate(2).toLocalDate();
				LocalDate lastPlayedDate = resultSet.getDate(3).toLocalDate();
				String game = null;
				if (complete) {
					game = resultSet.getString(4);
				}
				
				//add the results to the maps
				games.put(id, game);
				started.put(id, startedDate);
				lastPlayed.put(id, lastPlayedDate);
			}
		};
		
		//execute the query
		executeSQL(query, SqlExecutionType.QUERY, variableSetter, resultConsumer);
		
		//create a GameList from the results
		GameList gameList = new GameList();
		gameList.setGames(games);
		gameList.setStarted(started);
		gameList.setLastPlayed(lastPlayed);
		return gameList;
	}
	protected String buildGameListQuery(boolean complete, String username) {
		boolean allUsers = username.equals("-");
		StringBuilder sb = new StringBuilder("SELECT g.id, g.started, g.last_played");
		if (complete) {
			sb.append(", g.data");
		}
		sb.append(" FROM " + getTable(TABLE_GAMES) + " g");
		if (!allUsers) {
			sb.append(" JOIN " + getTable(TABLE_PLAYERS) + " p ON g.id = p.game_id");
			sb.append(" JOIN " + getTable(TABLE_USERS) + " u ON u.id = p.user_id");
			sb.append(" WHERE u.username = ?");
		}
		return sb.toString();
	}
	
	/**
	 * Get a list of moves for a game from the database.
	 * 
	 * @param gameId
	 *        The id of the game from which the moves should be searched (-1 for all moves of all games).
	 * 
	 * @param username
	 *        The name of the user from which the moves should be searched ('-' for all users of the game).
	 * 
	 * @param numMoves
	 *        The number of moves that should be searched (last made moves first; -1 for all moves of the game and/or user).
	 * 
	 * @return A {@link MoveList} including the moves.
	 */
	public MoveList listMoves(int gameId, String username, int num) throws GameDataException {
		boolean allGames = gameId == -1;
		boolean allUsers = username.equals("-");
		boolean allMoves = num == -1;
		
		String query = buildMoveListQuery(allGames, allUsers, allMoves);
		
		Map<Integer, String> moves = new HashMap<Integer, String>();
		Map<Integer, Integer> idToNum = new HashMap<Integer, Integer>();
		
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			int position = 1;
			if (!allUsers) {
				ps.setString(position, username);
				position++;
			}
			if (!allGames) {
				ps.setInt(position, gameId);
				position++;
			}
			if (!allMoves) {
				ps.setInt(position, num);
				position++;
			}
		};
		CheckedSqlConsumer<ResultSet> resultConsumer = resultSet -> {
			//iterate over the result and add everything into the local maps
			if (resultSet.next()) {
				int id = resultSet.getInt(1);
				int moveNum = resultSet.getInt(2);
				String move = resultSet.getString(3);
				
				//add the results to the maps
				moves.put(id, move);
				idToNum.put(id, moveNum);
			}
		};
		
		//execute the query
		executeSQL(query, SqlExecutionType.QUERY, variableSetter, resultConsumer);
		
		//create a move list from the results
		MoveList moveList = new MoveList();
		moveList.setMoves(moves);
		moveList.setIdToNum(idToNum);
		return moveList;
	}
	protected String buildMoveListQuery(boolean allGames, boolean allUsers, boolean allMoves) {
		StringBuilder sb = new StringBuilder("SELECT m.id, m.num, m.move FROM " + getTable(TABLE_MOVES) + " m");
		
		if (!allUsers) {
			//if not all users the username has to be found (by joining)
			sb.append(" JOIN " + getTable(TABLE_USERS) + " u ON u.id = m.user_id");
		}
		sb.append(" WHERE");
		if (allUsers && allGames) {
			//no conditions -> WHERE 1
			sb.append(" 1");
		}
		else {
			//add conditions
			if (!allUsers) {
				sb.append(" u.username = ?");
			}
			if (!allUsers && !allGames) {
				sb.append(" AND");
			}
			if (!allGames) {
				sb.append(" m.game_id = ?");
			}
		}
		
		//order the result by the move number
		sb.append(" ORDER BY m.num DESC");
		if (!allMoves) {
			//list only n moves
			sb.append(" LIMIT ?");
		}
		
		return sb.toString();
	}
	
	/**
	 * Creates a DatabaseConnection and capsules the SQLException that might be thrown in a GameDataException.
	 */
	private DatabaseConnection getDatabaseConnection() throws GameDataException {
		try {
			DatabaseConnection connection = DatabaseConnection.getInstance();
			return connection;
		}
		catch (SQLException e) {
			throw new GameDataException("Database connection could not be established.", Cause.SQL_EXCEPTION);
		}
	}
	
	/**
	 * Execute a SQL query in a prepared statement and return the number of affected rows (encapsules the executeSQL method of the
	 * {@link DatabaseConnection} class by covering a possible SQLException with a {@link GameDataException})
	 * 
	 * @param query
	 *        The query that is executed (as prepared statement)
	 * 
	 * @param type
	 *        The type of the execution
	 * 
	 * @param variableSetter
	 *        A consumer that prepares the statement by setting the variables
	 * 
	 * @param resultConsumer
	 *        A consumer that works on the ResultSet of a query
	 * 
	 * @return Depending on the parameter type:
	 *         <ul>
	 *         <li>UPDATE: the number of affected rows</li>
	 *         <li>CREATE: the number of affected rows</li>
	 *         <li>QUERY: 0</li>
	 *         </ul>
	 * 
	 * @throws GameDataException
	 *         A {@link GameDataException} is thrown if the update fails for some reason
	 */
	private int executeSQL(String query, SqlExecutionType type, CheckedSqlConsumer<PreparedStatement> variableSetter,
			CheckedSqlConsumer<ResultSet> resultConsumer) throws GameDataException {
		DatabaseConnection dbConnection = getDatabaseConnection();
		
		try {
			return dbConnection.executeSQL(query, type, variableSetter, resultConsumer);
		}
		catch (SQLException sqle) {
			throw new GameDataException("query execution failed with an SQLException", sqle, Cause.SQL_EXCEPTION);
		}
	}
	
	protected String getTable(String table) {
		return DatabaseConnection.getDATABASE() + "." + table;
	}
}