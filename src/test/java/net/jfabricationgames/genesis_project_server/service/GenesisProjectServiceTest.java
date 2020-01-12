package net.jfabricationgames.genesis_project_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.jfabricationgames.genesis_project_server.game.GameList;
import net.jfabricationgames.genesis_project_server.game.MoveList;
import net.jfabricationgames.genesis_project_server.user.Login;

class GenesisProjectServiceTest {
	
	private static final Login user1 = new Login("Player1", "unique_password_8621321854");
	private static final Login user2 = new Login("Player2", "unique_password_75231687");
	private static final Login user3 = new Login("Player3", "unique_password_2348942316");
	private static int game1Id = -1;
	private static int game2Id = -1;
	private static int game3Id = -1;
	private static final String move1Player1 = "unique_move_219805240896";
	private static final String move2Player1 = "unique_move_0850847956455564";
	private static final String move3Player1 = "unique_move_80654062304980";
	private static final String move1Player2 = "unique_move_54641384685231";
	private static final String move2Player2 = "unique_move_478465231894152";
	private static final String move3Player2 = "unique_move_897841323587";
	private static final String move1Player1Game1 = "unique_move_19804563048";
	
	private static String passwordEncryptionKey = "vcuh31250hvcsojnl312vcnlsgr329fdsip";//encryption key from UserDataManager class
	
	private static String getServerUrl() {
		return "http://jfabricationgames.ddns.net:5715/genesis_project_server/genesis_project/genesis_project/";
	}
	
	private static Response sendRequest(String resource, String requestType, Entity<?> entity) {
		return sendRequest(resource, requestType, entity, null);
	}
	private static Response sendRequest(String resource, String requestType, Entity<?> entity, Map<String, Object> headers) {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(getServerUrl()).path(resource);
		Builder builder = webTarget.request();
		if (headers != null) {
			for (Entry<String, Object> header : headers.entrySet()) {
				builder = builder.header(header.getKey(), header.getValue());
			}
		}
		Response response = null;
		switch (requestType) {
			case "GET":
				response = builder.get();
				break;
			case "POST":
				response = builder.post(entity);
				break;
		}
		return response;
	}
	
	@SuppressWarnings("unused")
	private static void printResponse(Response response) {
		String responseText = response.readEntity(String.class);
		System.out.println(responseText);
	}
	
	@BeforeAll
	public static void setUpUsersAndGames() throws IllegalStateException {
		//reset the test database
		Response resetTestDb = sendRequest("reset_test_database", "GET", null);
		
		if (resetTestDb.getStatus() != Status.OK.getStatusCode()) {
			throw new IllegalStateException("test database couldn't be reset");
		}
		
		//create test users and a test game
		Response createUser1 = createUser(user1);
		Response createUser2 = createUser(user2);
		Response createUser3 = createUser(user3);
		Response createGame1 = createGame(Arrays.asList(user1.getUsername(), user2.getUsername()));
		Response createGame2 = createGame(Arrays.asList(user1.getUsername(), user3.getUsername()));
		Response createGame3 = createGame(Arrays.asList(user1.getUsername(), user2.getUsername(), user3.getUsername()));
		
		//printResponse(createUser1);
		//printResponse(createUser2);
		//printResponse(createGame);
		
		if (createUser1.getStatus() != Status.OK.getStatusCode() || createUser2.getStatus() != Status.OK.getStatusCode()
				|| createUser3.getStatus() != Status.OK.getStatusCode()) {
			throw new IllegalStateException("users couldn't be created");
		}
		if (createGame1.getStatus() != Status.OK.getStatusCode()) {
			throw new IllegalStateException("game couldn't be created");
		}
		game1Id = createGame1.readEntity(Integer.class);
		game2Id = createGame2.readEntity(Integer.class);
		game3Id = createGame3.readEntity(Integer.class);
		if (game1Id < 0 || game2Id < 0 || game3Id < 0) {
			throw new IllegalStateException("game id is a negative number");
		}
		
		//add some moves to test the list_moves function
		Response setMove1 = setMove(move1Player1, user1, game3Id);
		Response setMove2 = setMove(move2Player1, user1, game3Id);
		Response setMove3 = setMove(move3Player1, user1, game3Id);
		Response setMove4 = setMove(move1Player2, user2, game3Id);
		Response setMove5 = setMove(move2Player2, user2, game3Id);
		Response setMove6 = setMove(move3Player2, user2, game3Id);
		Response setMove7 = setMove(move1Player1Game1, user1, game1Id);
		
		assertEquals(Status.OK.getStatusCode(), setMove1.getStatus());
		assertEquals(Status.OK.getStatusCode(), setMove2.getStatus());
		assertEquals(Status.OK.getStatusCode(), setMove3.getStatus());
		assertEquals(Status.OK.getStatusCode(), setMove4.getStatus());
		assertEquals(Status.OK.getStatusCode(), setMove5.getStatus());
		assertEquals(Status.OK.getStatusCode(), setMove6.getStatus());
		assertEquals(Status.OK.getStatusCode(), setMove7.getStatus());
	}
	
	private static Response createUser(Login login) {
		login.encryptPassword(passwordEncryptionKey);
		String resource = "create_user";
		String requestType = "POST";
		
		return sendRequest(resource, requestType, Entity.entity(login, MediaType.APPLICATION_JSON));
	}
	
	private static Response createGame(List<String> players) {
		String resource = "create_game";
		String requestType = "POST";
		return sendRequest(resource, requestType, Entity.entity(players, MediaType.APPLICATION_JSON));
	}
	
	private static Response updateGame(int gameId, String text) {
		String resource = "update_game";
		String requestType = "POST";
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("id", Integer.toString(gameId));
		
		return sendRequest(resource, requestType, Entity.entity(text, MediaType.APPLICATION_JSON), headers);
	}
	
	private static Response setMove(String move, Login user, int gameId) {
		String resource = "set_move";
		String requestType = "POST";
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("game_id", Integer.toString(gameId));
		headers.put("username", user.getUsername());
		
		return sendRequest(resource, requestType, Entity.entity(move, MediaType.APPLICATION_JSON), headers);
	}
	
	@Test
	public void testHello() {
		String resource = "hello";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertTrue(response.readEntity(String.class).toLowerCase().contains("hello"));
	}
	
	@Test
	public void testDatabase() {
		String resource = "test_db";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertFalse(response.readEntity(String.class).toLowerCase().contains("error"));
	}
	
	@Test
	public void testUpdateGame_wrongId_notFound() {
		String resource = "update_game/512/the_game_text";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testUpdateGame_correctId() {
		Response response = updateGame(game1Id, "game_text");
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetGame_wrongId_notFound() {
		String resource = "get_game/512";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetGame_correctId() {
		String gameText = "some_unique_game_text_891230184";
		updateGame(game1Id, gameText);
		
		String resource = "get_game/" + game1Id;
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		String responseText = response.readEntity(String.class);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(gameText, responseText);
	}
	
	@Test
	public void testGetConfig_notExisting() {
		String resource = "get_config/not_existing_config";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetConfig_constants() {
		String resource = "get_config/constants";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		String responseText = response.readEntity(String.class);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertTrue(responseText.contains("BUILDINGS_PER_PLANET"));
		assertTrue(responseText.contains("TURNS_PLAYED"));
		assertTrue(responseText.contains("STARTING_RESEARCH_STATES"));
	}
	
	@Test
	public void testSetMove_wrongGameId_notFound() {
		String resource = "set_move/512/" + user1.getUsername() + "/a_move_text";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testSetMove_wrongPlayerId_notFound() {
		String resource = "set_move/" + game1Id + "/a_not_existing_username/a_move_text";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testSetMove_correctIds() {
		String moveText = "a_move_was_made";
		String resource = "set_move";
		String requestType = "POST";
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("game_id", Integer.toString(game1Id));
		headers.put("username", user1.getUsername());
		
		Response response = sendRequest(resource, requestType, Entity.entity(moveText, MediaType.APPLICATION_JSON), headers);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testSetMove_correctOrderOfMoves() {
		Response createGame = createGame(Arrays.asList(user1.getUsername(), user2.getUsername()));
		int gameId = createGame.readEntity(Integer.class);
		assertEquals(Status.OK.getStatusCode(), createGame.getStatus());
		
		setMove("a_move", user1, gameId);
		setMove("another_move", user2, gameId);
		
		String resource = "list_moves/" + gameId + "/-/-1";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String moveListText = response.readEntity(String.class);
		MoveList moveList = getMoveList(moveListText);
		
		assertEquals(2, moveList.getMoves().size());
		assertEquals(2, moveList.getIdToNum().size());
		assertEquals(2, moveList.getIdToUsername().size());
		
		//the first two entries in the game have to be the move numbers 1 and 2
		assertTrue(moveList.getIdToNum().values().contains(1));
		assertTrue(moveList.getIdToNum().values().contains(2));
	}
	
	@Test
	public void testCreateGame_playersNotExisting() {
		List<String> players = Arrays.asList("FirstNotExistingPlayer", "SecondNotExistingPlayer");
		
		Response response = createGame(players);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testCreateUser() {
		Login login = new Login("a_user_that_is_created", "password");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testCreateUser_usernameAlreadyExists() {
		Login login = new Login("a_username_that_is_added_twice", "password");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//send the same request a second time so the user is already added and the second request fails
		response = createUser(login);
		//printResponse(response);
		assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testUpdateUser() {
		//first create the user so it exists and can be updated
		Login login = new Login("unique_username_4324321", "password");
		//login is encrypted in createUser method
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//update the user
		Login update = new Login("unique_username_53428971", "a_different_password");
		update.encryptPassword(passwordEncryptionKey);
		String resource = "update_user";
		String requestType = "POST";
		List<Login> updateLogins = Arrays.asList(login, update);
		
		response = sendRequest(resource, requestType, Entity.entity(updateLogins, MediaType.APPLICATION_JSON));
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testUpdateUser_wrongPassword() {
		//first create the user so it exists and can be updated
		Login login = new Login("unique_username_765314254", "password");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//update the user
		Login update = new Login("unique_username_897564", "a_different_password");
		//change the password (to test a wrong password)
		login.setPassword("a_wrong_password");
		login.encryptPassword(passwordEncryptionKey);
		String resource = "update_user";
		String requestType = "POST";
		List<Login> updateLogins = Arrays.asList(login, update);
		
		response = sendRequest(resource, requestType, Entity.entity(updateLogins, MediaType.APPLICATION_JSON));
		//printResponse(response);
		assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testUpdateUser_wrongUsername() {
		//first create the user so it exists and can be updated
		Login login = new Login("unique_username_14365432", "password");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//update the user
		Login update = new Login("unique_username_645324", "a_different_password");
		update.encryptPassword(passwordEncryptionKey);
		//change the password (to test a wrong password)
		login.setUsername("a_not_existing_username_64234e985243");
		String resource = "update_user";
		String requestType = "POST";
		
		List<Login> updateLogins = Arrays.asList(login, update);
		
		response = sendRequest(resource, requestType, Entity.entity(updateLogins, MediaType.APPLICATION_JSON));
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testUpdateUser_onlyOneLoginWithNoUpdate() {
		//first create the user so it exists and can be updated
		Login login = new Login("unique_username_8132645123", "password");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//update the user with only one login
		String resource = "update_user";
		String requestType = "POST";
		List<Login> updateLogins = Arrays.asList(login);
		
		response = sendRequest(resource, requestType, Entity.entity(updateLogins, MediaType.APPLICATION_JSON));
		//printResponse(response);
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testVerifyUser() {
		//first create the user so it exists and can be updated
		Login login = new Login("unique_username_78963321864", "unique_password_8946531846");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//verify the user
		String resource = "verify_user";
		String requestType = "POST";
		response = sendRequest(resource, requestType, Entity.entity(login, MediaType.APPLICATION_JSON));
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testVerifyUser_wrongPassword() {
		Login login = new Login("unique_username_546789456", "unique_password_78962312");
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//verify the user with a wrong password
		login.setPassword("wrong_password");
		String resource = "verify_user";
		String requestType = "POST";
		response = sendRequest(resource, requestType, Entity.entity(login, MediaType.APPLICATION_JSON));
		assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testVerifyUser_unknownUser() {
		String resource = "verify_user";
		String requestType = "POST";
		Login login = new Login("a_not_existing_username_8642363", "password");
		login.encryptPassword(passwordEncryptionKey);
		
		Response response = sendRequest(resource, requestType, Entity.entity(login, MediaType.APPLICATION_JSON));
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testListGames_complete_allUsers() {
		String resource = "list_games/true/-";
		String requestType = "GET";
		
		//update game 1 first to check the content
		String gameContent = "game_unique_content_8943218642";
		Response updateGame = updateGame(game1Id, gameContent);
		
		Response response = sendRequest(resource, requestType, null);
		String gameListText = response.readEntity(String.class);
		GameList gameList = getGameList(gameListText);
		
		assertEquals(Status.OK.getStatusCode(), updateGame.getStatus());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		assertTrue(gameList.getGames().keySet().contains(game1Id));
		assertTrue(gameList.getGames().keySet().contains(game2Id));
		assertTrue(gameList.getGames().keySet().contains(game3Id));
		assertEquals(gameContent, gameList.getGames().get(game1Id));
	}
	@Test
	public void testListGames_complete_specificUser() {
		String resource = "list_games/true/" + user2.getUsername();
		String requestType = "GET";
		
		//update game 1 first to check the content
		String gameContent = "game_unique_content_34984651563857448";
		Response updateGame = updateGame(game1Id, gameContent);
		
		Response response = sendRequest(resource, requestType, null);
		String gameListText = response.readEntity(String.class);
		GameList gameList = getGameList(gameListText);
		
		assertEquals(Status.OK.getStatusCode(), updateGame.getStatus());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		assertTrue(gameList.getGames().keySet().contains(game1Id));
		assertFalse(gameList.getGames().keySet().contains(game2Id));
		assertTrue(gameList.getGames().keySet().contains(game3Id));
		assertEquals(gameContent, gameList.getGames().get(game1Id));
	}
	@Test
	public void testListGames_incomplete_allUsers() {
		String resource = "list_games/false/-";
		String requestType = "GET";
		
		//update game 1 first to check the content
		String gameContent = "game_unique_content_786123089431";
		Response updateGame = updateGame(game1Id, gameContent);
		
		Response response = sendRequest(resource, requestType, null);
		String gameListText = response.readEntity(String.class);
		GameList gameList = getGameList(gameListText);
		
		assertEquals(Status.OK.getStatusCode(), updateGame.getStatus());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		assertTrue(gameList.getGames().keySet().contains(game1Id));
		assertTrue(gameList.getGames().keySet().contains(game2Id));
		assertTrue(gameList.getGames().keySet().contains(game3Id));
		assertNull(gameList.getGames().get(game1Id));
	}
	@Test
	public void testListGames_incomplete_specificUser() {
		String resource = "list_games/false/" + user2.getUsername();
		String requestType = "GET";
		
		//update game 1 first to check the content
		String gameContent = "game_unique_content_1896432185160";
		Response updateGame = updateGame(game1Id, gameContent);
		
		Response response = sendRequest(resource, requestType, null);
		String gameListText = response.readEntity(String.class);
		GameList gameList = getGameList(gameListText);
		
		assertEquals(Status.OK.getStatusCode(), updateGame.getStatus());
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		assertTrue(gameList.getGames().keySet().contains(game1Id));
		assertFalse(gameList.getGames().keySet().contains(game2Id));
		assertTrue(gameList.getGames().keySet().contains(game3Id));
		assertNull(gameList.getGames().get(game1Id));
	}
	
	@Test
	public void testListMoves_specificGame_specificUser_last2() {
		String resource = "list_moves/" + game3Id + "/" + user1.getUsername() + "/2";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String moveListText = response.readEntity(String.class);
		MoveList moveList = getMoveList(moveListText);
		
		assertEquals(2, moveList.getMoves().size());
		assertEquals(2, moveList.getIdToNum().size());
		assertEquals(2, moveList.getIdToUsername().size());
		assertTrue(moveList.getMoves().containsValue(move2Player1));
		assertTrue(moveList.getMoves().containsValue(move3Player1));
	}
	@Test
	public void testListMoves_allGames_specificUser_allNums() {
		String resource = "list_moves/-1/" + user1.getUsername() + "/-1";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String moveListText = response.readEntity(String.class);
		MoveList moveList = getMoveList(moveListText);
		
		assertTrue(moveList.getMoves().size() >= 4);//>= because other tests also add moves
		assertTrue(moveList.getIdToNum().size() >= 4);
		assertTrue(moveList.getIdToUsername().size() >= 4);
		assertTrue(moveList.getMoves().containsValue(move1Player1));
		assertTrue(moveList.getMoves().containsValue(move2Player1));
		assertTrue(moveList.getMoves().containsValue(move3Player1));
		assertTrue(moveList.getMoves().containsValue(move1Player1Game1));
		
		assertFalse(moveList.getMoves().containsValue(move1Player2));
		assertFalse(moveList.getMoves().containsValue(move2Player2));
		assertFalse(moveList.getMoves().containsValue(move3Player2));
	}
	@Test
	public void testListMoves_specificGame_allUsers_last2() {
		String resource = "list_moves/" + game3Id + "/-/2";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String moveListText = response.readEntity(String.class);
		MoveList moveList = getMoveList(moveListText);
		
		assertEquals(2, moveList.getMoves().size());
		assertEquals(2, moveList.getIdToNum().size());
		assertEquals(2, moveList.getIdToUsername().size());
		assertTrue(moveList.getMoves().containsValue(move2Player2));
		assertTrue(moveList.getMoves().containsValue(move3Player2));
		
		assertFalse(moveList.getMoves().containsValue(move1Player1));
		assertFalse(moveList.getMoves().containsValue(move1Player2));
	}
	@Test
	public void testListMoves_specificGame_allUsers_allNums() {
		String resource = "list_moves/" + game3Id + "/-/-1";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String moveListText = response.readEntity(String.class);
		MoveList moveList = getMoveList(moveListText);
		
		assertTrue(moveList.getMoves().size() >= 6);
		assertTrue(moveList.getIdToNum().size() >= 6);
		assertTrue(moveList.getIdToUsername().size() >= 6);
		assertTrue(moveList.getMoves().containsValue(move1Player1));
		assertTrue(moveList.getMoves().containsValue(move2Player2));
		assertTrue(moveList.getMoves().containsValue(move3Player2));
		
		assertFalse(moveList.getMoves().containsValue(move1Player1Game1));
		
		//test whether all move nums are unique
		assertEquals(moveList.getIdToNum().size(), new HashSet<Integer>(moveList.getIdToNum().values()).size());
	}
	@Test
	public void testListMoves_all() {
		String resource = "list_moves/-1/-/-1";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String moveListText = response.readEntity(String.class);
		MoveList moveList = getMoveList(moveListText);
		
		assertTrue(moveList.getMoves().size() >= 7);
		assertTrue(moveList.getIdToNum().size() >= 7);
		assertTrue(moveList.getIdToUsername().size() >= 7);
		assertTrue(moveList.getMoves().containsValue(move1Player2));
		assertTrue(moveList.getMoves().containsValue(move3Player1));
		assertTrue(moveList.getMoves().containsValue(move1Player1Game1));
	}
	
	private GameList getGameList(String gameListText) {
		ObjectMapper mapper = new ObjectMapper();
		//register the module to parse java-8 LocalDate
		mapper.registerModule(new JavaTimeModule());
		try {
			//"manually" parse JSON to Object
			GameList resp = mapper.readValue(gameListText, GameList.class);
			return resp;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("The response could not be read or parsed: " + gameListText, e);
		}
	}
	
	private MoveList getMoveList(String moveListText) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			//"manually" parse JSON to Object
			MoveList resp = mapper.readValue(moveListText, MoveList.class);
			return resp;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("The response could not be read or parsed: " + moveListText, e);
		}
	}
}