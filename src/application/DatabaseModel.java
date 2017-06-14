package application;

import java.sql.*;

/**
 * Class used as a test when checking the database connection.
 * @author Esther Leah Morrison
 *
 */
public class DatabaseModel {
	private Connection connection;
	
	/**
	 * Constructs a model of the database.
	 */
	public DatabaseModel() {
		connection = SQLiteConnection.Connector();
		if (connection == null) {
			System.out.println("Connection not successful");
			System.exit(1);
		}
	}
	
	/**
	 * Checks if the database is connected.
	 * @return True if the database is currently connected. False if the database is not connected.
	 */
	@SuppressWarnings("unused")
	private boolean isDBConnected() {
		try {
			return !(connection.isClosed());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}