package net.jfabricationgames.genesis_project_server.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.cj.jdbc.MysqlDataSource;

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
	public static final String DATABASE = "notebook";
	public static final String TABLE_NOTES = "notes";
	public static final String TABLE_EXECUTION_DATES = "execution_dates";
	public static final String TABLE_REMINDER_DATES = "reminder_dates";
	
	public static final String DATABASE_CONFIG_RESOURCE_FILE = "database.properties";
	
	/**
	 * The passwords are loaded from a properties file "database.properties" which is not added to the git-repository (for obvious reasons)
	 * <p>
	 * The file is located in src/main/resources/passwords.properties
	 * <p>
	 * Add the same password in the environment variables of the docker-compose.ylm
	 */
	private static String USER_PASSWORD;
	private static String USER;
	
	public static final String VERSION = "1.0.0";
	
	private static final boolean autoCommit = false;
	
	private static DatabaseConnection instance;
	
	private DatabaseConnection() throws SQLException {
		LOGGER.info("Creating DatabaseConnection; current version is " + VERSION);
		try {
			loadConfig();
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
	 * 
	 * @throws IOException
	 */
	private void loadConfig() throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties databaseConfigProperties = new Properties();
		try (InputStream resourceStream = loader.getResourceAsStream(DATABASE_CONFIG_RESOURCE_FILE)) {
			databaseConfigProperties.load(resourceStream);
		}
		USER_PASSWORD = databaseConfigProperties.getProperty("MYSQL_USER_PASSWORD");
		USER = databaseConfigProperties.getProperty("MYSQL_USER");
		
		if (USER_PASSWORD == null || USER_PASSWORD.equals("")) {
			throw new IOException("No password could be loaded from properties.");
		}
		if (USER == null || USER.equals("")) {
			throw new IOException("No user could be loaded from properties.");
		}
	}
	
	private void createDatabaseResourcesIfNotExists() throws SQLException {
		String query = "CREATE DATABASE IF NOT EXISTS " + DATABASE + ";";
		DataSource dataSource = getDataSourceWithoutDatabase();
		try (Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				connection.setAutoCommit(autoCommit);
				LOGGER.info("Creating database (if not exists); sending query: " + query);
				statement.execute(query);
				
				connection.commit();
			}
			catch (SQLException sqle) {
				connection.rollback();
				throw sqle;
			}
		}
	}
	
	@SuppressWarnings("unused")
	private MysqlDataSource getDataSource() {
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
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return dataSource;
	}
	
}