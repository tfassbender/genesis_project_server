package net.jfabricationgames.genesis_project_server.database;

import java.sql.SQLException;

/**
 * A consumer that can throw an SQLException (used to set variables in prepared statements)
 */
@FunctionalInterface
public interface CheckedSqlConsumer<T> {
	
	public void accept(T t) throws SQLException;
}
