package net.jfabricationgames.genesis_project_server.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.jfabricationgames.genesis_project_server.database.DatabaseConnection;
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
}