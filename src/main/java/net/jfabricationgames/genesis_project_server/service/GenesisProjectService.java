package net.jfabricationgames.genesis_project_server.service;

import java.util.List;

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

import net.jfabricationgames.genesis_project_server.database.DatabaseConnection;
import net.jfabricationgames.genesis_project_server.game.GameList;
import net.jfabricationgames.genesis_project_server.game.MoveList;
import net.jfabricationgames.genesis_project_server.user.Login;
import net.jfabricationgames.genesis_project_server.util.ErrorUtil;

@Path("/genesis_project")
public class GenesisProjectService {
	
	private static final Logger LOGGER = LogManager.getLogger(GenesisProjectService.class);
	
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
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@GET
	@Path("set_game/{id}/{game}")
	public Response updateGame(@PathParam("id") int id, @PathParam("game") String game) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
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
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
	}
	
	/**
	 * Get a configuration file.
	 * 
	 * @param config
	 *        The name of the configuration that is to be loaded. <br>
	 *        Options are:
	 *        <ul>
	 *        <li>"constants": loads constant values for the game</li>
	 *        <li>"field_positions": loads the positions of every field on the board</li>
	 *        <li>"class_effects": loads the class effect descriptions for all classes</li>
	 *        <li>"main_menu_dynamic_content": loads the dynamic content that is displayed in the main menu</li>
	 *        </ul>
	 * 
	 * @return The content of the configuration JSON file
	 */
	@GET
	@Path("get_config/{config}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig(@PathParam("config") String config) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
	}
	
	/**
	 * Set a move made in a game.
	 * 
	 * @param gameId
	 *        The id of the game in which the move was made.
	 * 
	 * @param playerId
	 *        The id of the player who did the move.
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
	@Path("set_move/{game_id}/{player_id}/{move}")
	public Response setMove(@PathParam("game_id") int gameId, @PathParam("player_id") int playerId, @PathParam("move") String move) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
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
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
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
	 *         <li>HTTP 404: The username already exists</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@POST
	@Path("create_user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(Login login) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
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
	 *         <li>HTTP 403: User validation failed</li>
	 *         <li>HTTP 404: A user with the update name already exists</li>
	 *         <li>HTTP 500: Failed</li>
	 *         </ul>
	 */
	@POST
	@Path("update_user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(List<Login> logins) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
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
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
	}
	
	/**
	 * Get a list of games in the database.
	 * 
	 * @param complete
	 *        Indicates whether complete games (true) or only ids (false) are requested.
	 * 
	 * @param userId
	 *        The id of a user to search for user specific games (-1 for all users).
	 * 
	 * @return A {@link GameList} object in JSON format that contains the games.
	 */
	@GET
	@Path("list_games/{complete}/{user_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listGames(@PathParam("complete") boolean complete, @PathParam("user_id") int userId) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
	}
	
	/**
	 * Get a list of moves for a game from the database.
	 * 
	 * @param gameId
	 *        The id of the game from which the moves should be searched (-1 for all moves of all games).
	 * 
	 * @param userId
	 *        The id of the user from which the moves should be searched (-1 for all users of the game).
	 * 
	 * @param numMoves
	 *        The number of moves that should be searched (last made moves first; -1 for all moves of the game and/or user).
	 * 
	 * @return A {@link MoveList} including the moves in JSON.
	 */
	@GET
	@Path("list_moves/{game_id}/{user_id}/{num_moves}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMoves(@PathParam("game_id") int gameId, @PathParam("user_id") int userId, @PathParam("num_moves") int numMoves) {
		try {
			//update the game
		}
		catch (Exception e) {
			//return an error response
		}
		
		return Response.status(Status.OK).build();
	}
}