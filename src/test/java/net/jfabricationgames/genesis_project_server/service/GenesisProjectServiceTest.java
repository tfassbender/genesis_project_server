package net.jfabricationgames.genesis_project_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.jfabricationgames.genesis_project_server.user.Login;

class GenesisProjectServiceTest {
	
	private static final Login user1 = new Login("Player1", "unique_password_8621321854");
	private static final Login user2 = new Login("Player2", "unique_password_75231687");
	private static int game1Id = -1;
	
	private static String getServerUrl() {
		return "http://jfabricationgames.ddns.net:5715/genesis_project_server/genesis_project/genesis_project/";
	}
	
	private static Response sendRequest(String resource, String requestType, Entity<?> entity) {
		Client client = ClientBuilder.newClient();
		WebTarget webTarget = client.target(getServerUrl()).path(resource);
		Response response = null;
		switch (requestType) {
			case "GET":
				response = webTarget.request().get();
				break;
			case "POST":
				response = webTarget.request().post(entity);
				break;
		}
		return response;
	}
	
	@SuppressWarnings("unused")
	private void printResponse(Response response) {
		String responseText = response.readEntity(String.class);
		System.out.println(responseText);
	}
	
	@BeforeAll
	public static void setUpUsersAndGames() throws IllegalStateException {
		Response createUser1 = createUser(user1);
		Response createUser2 = createUser(user2);
		Response createGame = createGame(Arrays.asList(user1.getUsername(), user2.getUsername()));
		
		if (createUser1.getStatus() != Status.OK.getStatusCode() || createUser2.getStatus() != Status.OK.getStatusCode()) {
			throw new IllegalStateException("users couldn't be created");
		}
		if (createGame.getStatus() != Status.OK.getStatusCode()) {
			throw new IllegalStateException("game couldn't be created");
		}
		game1Id = createGame.readEntity(Integer.class);
		if (game1Id < 0) {
			throw new IllegalStateException("game id is a negative number");
		}
	}
	
	private static Response createUser(Login login) {
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
		String resource = "update_game/" + gameId + "/" + text;
		String requestType = "GET";
		
		return sendRequest(resource, requestType, null);
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
	public void testSetGame_wrongGameId_notFound() {
		String resource = "set_move/512/" + user1.getUsername() + "/a_move_text";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testSetGame_wrongPlayerId_notFound() {
		String resource = "set_move/" + game1Id + "/a_not_existing_username/a_move_text";
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testSetGame_correctIds() {
		String moveText = "a_move_was_made";
		String resource = "set_move/" + game1Id + "/" + user1.getUsername() + "/" + moveText;
		String requestType = "GET";
		
		Response response = sendRequest(resource, requestType, null);
		String responseText = response.readEntity(String.class);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(moveText, responseText);
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
		Response response = createUser(login);
		//printResponse(response);
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		
		//update the user
		Login update = new Login("unique_username_53428971", "a_different_password");
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
		//first create the user so it exists and can be updated
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
		//first create the user so it exists and can be updated
		String resource = "verify_user";
		String requestType = "POST";
		Login login = new Login("a_not_existing_username_8642363", "password");
		
		Response response = sendRequest(resource, requestType, Entity.entity(login, MediaType.APPLICATION_JSON));
		//printResponse(response);
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testListGames_complete_allUsers() {
		//TODO
		fail("not yet implemented");
	}
	@Test
	public void testListGames_complete_specificUser() {
		//TODO
		fail("not yet implemented");
	}
	@Test
	public void testListGames_incomplete_allUsers() {
		//TODO
		fail("not yet implemented");
	}
	@Test
	public void testListGames_incomplete_specificUser() {
		//TODO
		fail("not yet implemented");
	}
}