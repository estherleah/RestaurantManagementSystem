package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import application.controller.LoginController;

/**
 * Class to create an employee log instance.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class Log {
	
	/**
	 * Create an entry in the employeelog table in the database with the employeeID and the activity done.
	 * @param activity The current activity of the employee.
	 */
	public static void addToEmployeeLog(String activity) {
		// get date and time
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		String date = LocalDate.now().format(format).toString();
		String time = LocalTime.now().toString().substring(0, 8);
		// prepare log
		String logDetails = LoginController.employee + ": " + date + " " + time + " " + activity;		
		// write log to database
		PreparedStatement preparedStatement = null;
		Connection connection = SQLiteConnection.Connector();
		String query = "insert into employeelog (employeeid, log) values (?,?)";
		try {
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, LoginController.employeeid);
			preparedStatement.setString(2, logDetails);
			preparedStatement.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
	}
}
