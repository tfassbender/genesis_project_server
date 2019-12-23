package net.jfabricationgames.genesis_project_server.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.genesis_project_server.config.ConfigurationDataManager;
import net.jfabricationgames.genesis_project_server.database.DatabaseConnection;
import net.jfabricationgames.genesis_project_server.exception.GameDataException;
import net.jfabricationgames.genesis_project_server.game.GameDataManager;
import net.jfabricationgames.genesis_project_server.game.GameList;
import net.jfabricationgames.genesis_project_server.game.MoveList;
import net.jfabricationgames.genesis_project_server.user.Login;
import net.jfabricationgames.genesis_project_server.user.UserDataManager;
import net.jfabricationgames.genesis_project_server.util.ErrorUtil;

@Path("/genesis_project")
public class GenesisProjectService {
	
	private static final Logger LOGGER = LogManager.getLogger(GenesisProjectService.class);
	
	public static final String TEST_CONFIG_RESOURCE_FILE = "config/test.properties";
	private static Properties testProperties;
	
	static {
		try {
			loadTestConfig();
		}
		catch (IOException ioe) {
			LOGGER.fatal("test configuration properties couldn't be loaded (ending programm because of unclear state)", ioe);
			System.exit(1);
		}
	}
	
	private static void loadTestConfig() throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		testProperties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream(TEST_CONFIG_RESOURCE_FILE)) {
			testProperties.load(resourceStream);
		}
		if (!testProperties.containsKey("test")) {
			throw new IOException("property 'test' not found");
		}
		else if (isTestRun()) {
			LOGGER.warn(">>>> GenesisProjectServer started as test run");
		}
		else {
			LOGGER.info(">>>> GenesisProjectServer started as productive run");
		}
	}
	
	/**
	 * A simple hello world to test whether the service is reachable
	 */
	@GET
	@Path("/hello")
	@Produces(MediaType.TEXT_PLAIN)
	public Response processHelloRequestGet() {
		LOGGER.info("Received 'hello' request (HTTP GET)");
		String answer = "Hello there!";
		return Response.status(Status.OK).entity(answer).build();
	}
	
	/**
	 * Test whether the database is working (by creating a DatabaseConnection object that creates the database and tables)
	 */
	@GET
	@Path("/test_db")
	@Produces(MediaType.TEXT_PLAIN)
	public Response testDatabase() {
		LOGGER.info("Received 'testDatabase' request (HTTP GET)");
		
		String answer;
		try {
			DatabaseConnection.getInstance();
			answer = "Database up and running";
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			answer = "Database error: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage() + "\n" + ErrorUtil.getStackTraceAsString(e);
		}
		
		return Response.status(Status.OK).entity(answer).build();
	}
	
	/**
	 * Update a game in the database.
	 * 
	 * @param id
	 *        The id of the game that is updated.
	 * 
	 * @param game
	 *        The game as JSON representation.
	 * 
	 * @return HTTP codes only:
	 *         <ul>
	 *         <li>HTTP 200: OK</li>
	 *         <li>HTTP 404: Game id not found</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@GET
	@Path("update_game/{id}/{game}")
	public Response updateGame(@PathParam("id") int id, @PathParam("game") String game) {
		LOGGER.debug("updateGame was called. parameters: {}, {}", id, game);
		try {
			GameDataManager gameDataManager = new GameDataManager();
			gameDataManager.updateGame(id, game);
			
			return Response.status(Status.OK).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
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
	@GET
	@Path("get_game/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGame(@PathParam("id") int id) {
		LOGGER.debug("getGame was called. parameters: {}", id);
		try {
			GameDataManager gameDataManager = new GameDataManager();
			String game = gameDataManager.getGame(id);
			
			return Response.status(Status.OK).entity(game).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Get a configuration file.
	 * 
	 * @param config
	 *        The name of the configuration that is to be loaded. <br>
	 *        Options are:
	 *        <ul>
	 *        <li>"constants": loads constant values for the game</li>
	 *        <li>"description_texts": loads all description texts</li>
	 *        <li>"main_menu_dynamic_content": loads the dynamic content that is displayed in the main menu</li>
	 *        </ul>
	 * 
	 * @return The content of the configuration JSON file
	 */
	@GET
	@Path("get_config/{config}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig(@PathParam("config") String config) {
		LOGGER.debug("getConfig was called. parameters: {}", config);
		try {
			ConfigurationDataManager configDataManager = ConfigurationDataManager.getInstance();
			String configurationFile = configDataManager.getConfiguration(config);
			
			if (configurationFile == null) {
				return Response.status(Status.NOT_FOUND).build();
			}
			else {
				return Response.status(Status.OK).entity(configurationFile).build();
			}
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
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
	 * 
	 * @return HTTP codes only:
	 *         <ul>
	 *         <li>HTTP 200: OK</li>
	 *         <li>HTTP 404: Game or user id not found</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@GET
	@Path("set_move/{game_id}/{username}/{move}")
	public Response setMove(@PathParam("game_id") int gameId, @PathParam("username") String username, @PathParam("move") String move) {
		LOGGER.debug("setMove was called. parameters: {}, {}, {}", gameId, username, move);
		try {
			GameDataManager gameDataManager = new GameDataManager();
			gameDataManager.setMove(gameId, username, move);
			
			return Response.status(Status.OK).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Create a new game.
	 * 
	 * @param players
	 *        The usernames of all players that take part in the game.
	 * 
	 * @return The ID of the game in the database.
	 */
	@POST
	@Path("create_game")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createGame(List<String> players) {
		LOGGER.debug("createGame was called. parameters: {}", players);
		try {
			GameDataManager gameDataManager = new GameDataManager();
			int id = gameDataManager.createGame(players);
			
			return Response.status(Status.OK).entity(id).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Creates a new user by a {@link Login} object.
	 * 
	 * @param login
	 *        The login for the user.
	 * 
	 * @return HTTP codes only:
	 *         <ul>
	 *         <li>HTTP 200: OK</li>
	 *         <li>HTTP 403: The username already exists</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@POST
	@Path("create_user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(Login login) {
		LOGGER.debug("createUser was called. parameters: {}", login);
		try {
			UserDataManager userDataManager = new UserDataManager();
			userDataManager.createUser(login);
			
			return Response.status(Status.OK).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Updates a user (username and/or password)
	 * 
	 * @param logins
	 *        The users logins: The first has to be the valid current login; The second is the update.
	 * 
	 * @return HTTP codes only:
	 *         <ul>
	 *         <li>HTTP 200: OK</li>
	 *         <li>HTTP 400: Not enough logins in the list (have to be 2)</li>
	 *         <li>HTTP 403: User validation failed</li>
	 *         <li>HTTP 404: A user with the update name already exists</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@POST
	@Path("update_user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(List<Login> logins) {
		LOGGER.debug("updateUser was called. parameters: {}", logins);
		if (logins.size() < 2) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		try {
			UserDataManager userDataManager = new UserDataManager();
			userDataManager.updateUser(logins.get(0), logins.get(1));
			
			return Response.status(Status.OK).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Verifies a user's login.
	 * 
	 * @param login
	 *        The user's login (username and password) in JSON form.
	 * 
	 * @return HTTP codes only:
	 *         <ul>
	 *         <li>HTTP 200: OK</li>
	 *         <li>HTTP 403: User validation failed</li>
	 *         <li>HTTP 404: User not found</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@POST
	@Path("verify_user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response verifyUser(Login login) {
		LOGGER.debug("verifyUser was called. parameters: {}", login);
		try {
			UserDataManager userDataManager = new UserDataManager();
			boolean verified = userDataManager.verifyUser(login);
			
			if (!verified) {
				return Response.status(Status.FORBIDDEN).build();
			}
			return Response.status(Status.OK).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
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
	 * @return A {@link GameList} object in JSON format that contains the games.
	 */
	@GET
	@Path("list_games/{complete}/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listGames(@PathParam("complete") boolean complete, @PathParam("username") String username) {
		LOGGER.debug("listGames was called. parameters: {}, {}", complete, username);
		try {
			GameDataManager gameDataManager = new GameDataManager();
			GameList gameList = gameDataManager.listGames(complete, username);
			
			return Response.status(Status.OK).entity(gameList).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
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
	 * @return A {@link MoveList} including the moves in JSON.
	 */
	@GET
	@Path("list_moves/{game_id}/{username}/{num_moves}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMoves(@PathParam("game_id") int gameId, @PathParam("username") String username, @PathParam("num_moves") int numMoves) {
		LOGGER.debug("listMoves was called. parameters: {}, {}, {}", gameId, username, numMoves);
		try {
			GameDataManager gameDataManager = new GameDataManager();
			MoveList moveList = gameDataManager.listMoves(gameId, username, numMoves);
			
			return Response.status(Status.OK).entity(moveList).build();
		}
		catch (GameDataException gde) {
			return handleGameDataException(gde);
		}
		catch (Exception e) {
			LOGGER.error("an unknown error occured: ", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	/**
	 * Reset the test database to an initial state. Only works in test environments (see configuration file config/test.properties).
	 * 
	 * 
	 */
	@GET
	@Path("reset_test_database")
	public Response resetTestDatabase() {
		LOGGER.debug("resetTestDatabase was called");
		if (!isTestRun()) {
			return Response.status(Status.FORBIDDEN).entity("The current environment is not a test environment. Resetting test database aborted")
					.build();
		}
		else {
			try {
				//the current environment is a test environment, so try to reset it
				DatabaseConnection dbConnection = DatabaseConnection.getInstance();
				dbConnection.resetTestDatabase();
				
				return Response.status(Status.OK).build();
			}
			catch (Exception e) {
				LOGGER.error("an unknown error occured: ", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
	
	/**
	 * Handle the GameDataExceptions that can be thrown by almost every method.
	 */
	private Response handleGameDataException(GameDataException gde) {
		LOGGER.error("a GameDataException occured", gde);
		Status responseStatus;
		switch (gde.getGameDataExceptionCause()) {
			case NOT_FOUND:
				responseStatus = Status.NOT_FOUND;
				break;
			case NO_PERMISSION:
				responseStatus = Status.FORBIDDEN;
				break;
			case UNKNOWN:
			case SQL_EXCEPTION:
			default:
				responseStatus = Status.INTERNAL_SERVER_ERROR;
				break;
		}
		return Response.status(responseStatus).build();
	}
	
	public static Properties getTestProperties() {
		return testProperties;
	}
	public static boolean isTestRun() {
		String testProperty = testProperties.getProperty("test");
		LOGGER.debug("test property loaded from properties file: {}", testProperty);
		return Boolean.parseBoolean(testProperty);
	}
}