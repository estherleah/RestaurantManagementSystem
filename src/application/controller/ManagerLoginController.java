package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.SQLiteConnection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller class for the manager login screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class ManagerLoginController {
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
			// open manager window
			try {
				Stage primaryStage = new Stage();
				Parent root = FXMLLoader.load(getClass().getResource("../Manager.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("Manager");
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						Stage primaryStage = new Stage();
						Parent root;
						try {
							root = FXMLLoader.load(getClass().getResource("../Main.fxml"));
							Scene scene = new Scene(root);
							scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
							primaryStage.setScene(scene);
							primaryStage.show();
							primaryStage.setTitle("Main");	
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
					}			
				});
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		else {
			lblStatus.setText("Invalid manager login");
		}
	}
	
	/**
	 * Checks if the input text is a valid username/password pair and if the username/password belong to a manager.
	 * @param username The username input by the user.
	 * @param password The password input by the user.
	 * @return True if the username/password are a valid manager login. False if they are not a valid manager login.
	 */
	private boolean isLogin(String username, String password) {
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String query = "select * from employees where username = ? and password = ? and manager = 1";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				// set the employee for the login session
				return true;
			}
			else {
				return false;
			}
		} catch (SQLException e) {
			return false;
		} finally {
			try {
				preparedStatement.close();
				resultSet.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
	}

}
