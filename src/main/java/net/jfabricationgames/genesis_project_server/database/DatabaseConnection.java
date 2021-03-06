package net.jfabricationgames.genesis_project_server.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.cj.jdbc.MysqlDataSource;

import net.jfabricationgames.genesis_project_server.exception.GameDataException;
import net.jfabricationgames.genesis_project_server.exception.GameDataException.Cause;
import net.jfabricationgames.genesis_project_server.service.GenesisProjectService;

/**
 * Create a connection to a database and add or get values of one specific table for testing.
 * 
 * @author Tobias Faßbender
 */
public class DatabaseConnection {
	
	private static final Logger LOGGER = LogManager.getLogger(DatabaseConnection.class);
	
	/**
	 * The connection url for the mysql database in the docker
	 * <ul>
	 * <li>"jdbc:mysql://" just tells the driver to use jdbc:mysql (needed)</li>
	 * <li>"mysql" is the name or alias of the docker container</li>
	 * <li>"useSSL=false" obviously tells to not use SSL (and don't warn because of no SSL every time), because SSL is not needed when communicating
	 * between docker containers</li>
	 * </ul>
	 */
	public static final String URL = "jdbc:mysql://mysql?useSSL=false";
	public static final String DATABASE_CONFIG_RESOURCE_FILE = "config/database.properties";
	public static final String DATABASE_NAME_REPLACEMENT = "<<DATABASE_NAME>>";
	
	public static final String TABLE_GAMES = "games";
	public static final String TABLE_MOVES = "moves";
	public static final String TABLE_PLAYERS = "players";
	public static final String TABLE_USERS = "users";
	
	/**
	 * The passwords are loaded from a properties file "database.properties" which is not added to the git-repository (for obvious reasons)
	 * <p>
	 * The file is located in src/main/resources/passwords.properties
	 * <p>
	 * Add the same password in the environment variables of the docker-compose.ylm
	 */
	private String USER_PASSWORD;
	private String USER;
	private String DATABASE;
	private String DATABASE_BUILD_FILE;
	
	public static final String VERSION = "1.0.0";
	
	private static final boolean autoCommit = false;
	
	private static DatabaseConnection instance;
	
	private DatabaseConnection() throws SQLException {
		LOGGER.info("Creating DatabaseConnection; current version is " + VERSION);
		try {
			loadConfig();
			
			if (GenesisProjectService.isTestRun()) {
				//drop the test database before use to ensure a clean test environment
				dropTestDatabase();
			}
			
			//test the privileges for testing purposes
			testPrivileges(USER);
			
			createDatabaseResourcesIfNotExists();
		}
		catch (SQLException sqle) {
			LOGGER.error("Error while creating the database resources", sqle);
			throw sqle;
		}
		catch (IOException ioe) {
			LOGGER.error("MySQL passwords couldn't be loaded", ioe);
			throw new SQLException("MySQL passwords couldn't be loaded", ioe);
		}
	}
	
	public static synchronized DatabaseConnection getInstance() throws SQLException {
		if (instance == null) {
			instance = new DatabaseConnection();
		}
		return instance;
	}
	
	/**
	 * Load the password from the properties
	 */
	private void loadConfig() throws IOException, SQLException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties databaseConfigProperties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream(DATABASE_CONFIG_RESOURCE_FILE)) {
			databaseConfigProperties.load(resourceStream);
		}
		USER_PASSWORD = databaseConfigProperties.getProperty("MYSQL_USER_PASSWORD");
		USER = databaseConfigProperties.getProperty("MYSQL_USER");
		DATABASE = databaseConfigProperties.getProperty("DATABASE");
		DATABASE_BUILD_FILE = databaseConfigProperties.getProperty("DATABASE_BUILD_FILE");
		
		if (USER_PASSWORD == null || USER_PASSWORD.equals("")) {
			throw new IOException("No password could be loaded from properties.");
		}
		if (USER == null || USER.equals("")) {
			throw new IOException("No user could be loaded from properties.");
		}
		
		if (GenesisProjectService.isTestRun()) {
			//use a test database that is deleted before use to create a new testing environment
			DATABASE = GenesisProjectService.getTestProperties().getProperty("test_db", "genesis_project_test");
			LOGGER.warn("Starting DatabaseConnection as test run using database: {}", DATABASE);
		}
		if (DATABASE == null || DATABASE.equals("")) {
			throw new IOException("No database could be loaded from properties.");
		}
		
		LOGGER.info("configuration loaded: [USER: {}, DATABASE: {}, USER_PASSWORD loaded: {}]", USER, DATABASE, USER_PASSWORD != null);
	}
	
	/**
	 * Test which privileges the current user (or a null user) has on the database.
	 */
	private void testPrivileges(String user) throws SQLException {
		LOGGER.info("testing privileges for user: {}", user);
		//drop the test database before a test to create a new testing environment
		try (Connection connection = getDataSourceWithoutDatabase().getConnection()) {
			String query;
			if (user != null) {
				query = "SHOW GRANTS FOR " + user + ";";
			}
			else {
				query = "SHOW GRANTS;";
			}
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				//log the result set and the content
				ResultSet result = statement.executeQuery();
				ResultSetMetaData meta = result.getMetaData();
				while (result.next()) {
					for (int i = 1; i <= meta.getColumnCount(); i++) {
						String privileges = result.getString(i);
						LOGGER.info("privileges on database for user {}: {}", user, privileges);
					}
				}
			}
		}
	}
	
	public void resetTestDatabase() throws SQLException {
		if (GenesisProjectService.isTestRun()) {
			LOGGER.warn("resetting test database");
			dropTestDatabase();
			createDatabaseResourcesIfNotExists();
		}
		else {
			throw new SQLException("The current environment is no test environment. Abborting reset of test database.");
		}
	}
	
	private void dropTestDatabase() throws SQLException {
		final String databaseNotExistingMessage = "database doesn't exist";
		LOGGER.info("dropping the test database: {}", getDATABASE());
		//drop the test database before a test to create a new testing environment
		try (Connection connection = getDataSourceWithoutDatabase().getConnection()) {
			String query = "DROP DATABASE " + getDATABASE();
			try (PreparedStatement statement = connection.prepareStatement(query)) {
				try {
					statement.execute();
				}
				catch (SQLException sqle) {
					//ignore the exception if it just says that the database doesn't exist (because that's just want we want here)
					if (!sqle.getMessage().toLowerCase().contains(databaseNotExistingMessage)) {
						throw sqle;
					}
				}
			}
		}
		LOGGER.info("test database was dropped successfully");
	}
	
	private void createDatabaseResourcesIfNotExists() throws SQLException {
		String query;
		try {
			//load the database script as a resource
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			String filename = loader.getResource(DATABASE_BUILD_FILE).getFile();
			File databaseBuild = new File(filename);
			query = Files.readAllLines(databaseBuild.toPath()).stream().collect(Collectors.joining("\n"));
			//replace the database name with the name of the current database
			query = query.replaceAll(DATABASE_NAME_REPLACEMENT, DATABASE);
		}
		catch (IOException ioe) {
			throw new SQLException("query couldn't be loaded", ioe);
		}
		
		DataSource dataSource = getDataSourceWithoutDatabase();
		try (Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				connection.setAutoCommit(autoCommit);
				LOGGER.info("Creating database resources (if not exists); sending query:\n" + query);
				statement.execute(query);
				
				connection.commit();
			}
			catch (SQLException sqle) {
				connection.rollback();
				throw sqle;
			}
		}
	}
	
	public MysqlDataSource getDataSource() {
		//https://www.journaldev.com/2509/java-datasource-jdbc-datasource-example
		MysqlDataSource dataSource = getDataSourceWithoutDatabase();
		dataSource.setDatabaseName(DATABASE);
		return dataSource;
	}
	private MysqlDataSource getDataSourceWithoutDatabase() {
		//https://www.journaldev.com/2509/java-datasource-jdbc-datasource-example
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setURL(URL);
		dataSource.setUser(USER);
		dataSource.setPassword(USER_PASSWORD);
		dataSource.setPort(3306);//the default mysql port
		try {
			//enable public key retrieval because I want to get the keys of the inserted data 
			//(which seems to cause problems when using useSSL=false in the url)
			dataSource.setAllowPublicKeyRetrieval(true);
			//enable multiple queries in one statement
			dataSource.setAllowMultiQueries(true);
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return dataSource;
	}
	
	/**
	 * Execute a SQL query in a prepared statement and return the number of affected rows.
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
	public int executeSQL(String query, SqlExecutionType type, CheckedSqlConsumer<PreparedStatement> variableSetter,
			CheckedSqlConsumer<ResultSet> resultConsumer) throws SQLException {
		MysqlDataSource dataSource = getDataSource();
		
		LOGGER.debug("executeSQL was called (query: {}, type: {} type)", query, type);
		
		int affectedRows = 0;
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(false);
			
			if (type == SqlExecutionType.CREATE) {
				//create a prepared statement that returns the id of the created object(s)
				try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
					variableSetter.accept(statement);
					
					LOGGER.info("executing prepared statement: {}", statement);
					affectedRows = statement.executeUpdate();
					
					//get the generated keys and let the consumer accept them
					ResultSet result = statement.getGeneratedKeys();
					resultConsumer.accept(result);
					
					connection.commit();
				}
				catch (SQLException sqle) {
					connection.rollback();
					throw sqle;
				}
			}
			else {
				//type is UPDATE or QUERY: create a prepared statement without returning the generated keys
				try (PreparedStatement statement = connection.prepareStatement(query)) {
					variableSetter.accept(statement);
					
					LOGGER.info("executing prepared statement: {}", statement);
					if (type == SqlExecutionType.UPDATE) {
						//execute the update and list the number of affected rows
						affectedRows = statement.executeUpdate();
					}
					else if (type == SqlExecutionType.QUERY) {
						//execute the query and let the consumer accept the result set
						ResultSet result = statement.executeQuery();
						resultConsumer.accept(result);
					}
					
					connection.commit();
				}
				catch (SQLException sqle) {
					connection.rollback();
					throw sqle;
				}
			}
		}
		
		return affectedRows;
	}
	
	/**
	 * Creates a DatabaseConnection and capsules the SQLException that might be thrown in a GameDataException.
	 */
	public static DatabaseConnection getCheckedDatabaseConnection() throws GameDataException {
		try {
			DatabaseConnection connection = getInstance();
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
	public static int executeCheckedSQL(String query, SqlExecutionType type, CheckedSqlConsumer<PreparedStatement> variableSetter,
			CheckedSqlConsumer<ResultSet> resultConsumer) throws GameDataException {
		DatabaseConnection dbConnection = DatabaseConnection.getCheckedDatabaseConnection();
		
		try {
			return dbConnection.executeSQL(query, type, variableSetter, resultConsumer);
		}
		catch (SQLException sqle) {
			throw new GameDataException("query execution failed with an SQLException", sqle, Cause.SQL_EXCEPTION);
		}
	}
	
	/**
	 * Get the name of the table with a leading database name.
	 */
	public static String getTable(String table) throws GameDataException {
		try {
			//execute get instance first to load the configuration
			DatabaseConnection dbConnection = getInstance();
			return dbConnection.getDATABASE() + "." + table;
		}
		catch (SQLException sqle) {
			throw new GameDataException("An SQLException occured while trying to get a DatabaseConnection instance", sqle, Cause.SQL_EXCEPTION);
		}
	}
	
	public String getUSER() {
		return USER;
	}
	public String getDATABASE() {
		return DATABASE;
	}
}