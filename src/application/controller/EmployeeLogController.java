package application.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import application.SQLiteConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controller class for the employee log screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class EmployeeLogController implements Initializable {
	
	@FXML
	private Label lblEmployee;
	
	@FXML
	private TextArea txtLog;

	/**
	 * Initialises the employee log with the log for the selected employee.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		txtLog.setEditable(false);
		lblEmployee.setText(EmployeesController.employeeName);
		Connection connection = SQLiteConnection.Connector();
		try {
			String query = "select * from employeelog where employeeid = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, EmployeesController.employeeID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				txtLog.appendText(resultSet.getString("log"));
				txtLog.appendText(System.getProperty("line.separator"));
			}
		} catch(SQLException e) {
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
