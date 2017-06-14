package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.Log;
import application.SQLiteConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controller class for the login screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class LoginController extends AnchorPane {
	
	// used by the log class etc. to find out which employee has logged in
	public static String employee;
	public static int employeeid;
	
	@FXML
	private Label lblStatus;
	
	@FXML
	private TextField txtUsername;
	
	@FXML
	private TextField txtPassword;
	
	/**
	 * If the login details are valid the main window is opened. Otherwise an error message is displayed.
	 * @param event
	 */
	public void login(ActionEvent event) {
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		if (isLogin(username, password)) {
			lblStatus.setText("Login successful");
			// hide login stage
			((Node)event.getSource()).getScene().getWindow().hide();
			// open main window
			try {
				Stage primaryStage = new Stage();
				Parent root = FXMLLoader.load(getClass().getResource("../Main.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("Main");
				Log.addToEmployeeLog("logged in.");
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		else {
			lblStatus.setText("Login details not found");
		}
	}
	
	/**
	 * Checks if the input text is a valid username/password pair.
	 * @param username The username to test
	 * @param password The password to test
	 * @return True if the username/password pair is valid. False if the username/password pair is invalid.
	 */
	private boolean isLogin(String username, String password) {
		Connection connection = SQLiteConnection.Connector();
		String query = "select * from employees where username = ? and password = ?";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// set the employee for the login session
				employee = resultSet.getString("firstname") + " " + resultSet.getString("lastname");
				employeeid = resultSet.getInt("employeeid");
				return true;
			}
			else {
				return false;
			}
		} catch (SQLException e) {
			return false;
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
	}

}
