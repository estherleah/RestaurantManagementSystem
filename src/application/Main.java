package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Driver class.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class Main extends Application {
	/**
	 * Starts the application with the login screen.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/application/Login.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Login");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
}