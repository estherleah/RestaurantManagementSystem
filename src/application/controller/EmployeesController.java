package application.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Employee;
import application.SQLiteConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller class for the employee options screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class EmployeesController implements Initializable {
	
	// can be used by the EmployeeLogController to know which employee's log to display
	public static int employeeID;
	public static String employeeName;
	
	// all employees
	private ObservableList<Employee> employees = FXCollections.observableArrayList();
	
	// list of managers
	private ArrayList<Integer> managers = new ArrayList<Integer>();
	
	// list of usernames
	private ArrayList<String> usernames = new ArrayList<String>();
	
	@FXML
	private TableView<Employee> tblEmployees;
	
	@FXML
    private TableColumn<Employee, Integer> colEmployeeID;
	
	@FXML
    private TableColumn<Employee, String> colName;
	
	@FXML
	private DialogPane dialogAdd;
	
	@FXML
	private Label lblMessage;
	
	@FXML
	private TextField txtFirstName;
	
	@FXML
	private TextField txtLastName;
	
	@FXML
	private TextField txtUsername;
	
	@FXML
	private PasswordField txtPassword;
	
	@FXML
	private PasswordField txtConfirmPassword;
	
	/**
	 * Initialises the values in the table of employees to show all the employees.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {	
		colEmployeeID.setCellValueFactory(
		        new PropertyValueFactory<Employee,Integer>("employeeID"));
		colName.setCellValueFactory(
		        new PropertyValueFactory<Employee,String>("name"));	
		
		updateTable();		
	}
	
	/**
	 * Updates the table of employees to show all employees.
	 */
	private void updateTable() {
		tblEmployees.getItems().clear();
		Connection connection = SQLiteConnection.Connector();
		try {		
			String query = "select * from employees";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				Employee employee = new Employee();
				employee.employeeID.set(resultSet.getInt("employeeid"));
				employee.name.set(resultSet.getString("firstname") + " " + resultSet.getString("lastname"));
				employees.add(employee);
			}
			tblEmployees.setItems(employees);		
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

	/**
	 * Shows a pop-up window to add a new employee.
	 * @param event
	 */
	public void add(ActionEvent event) {
		dialogAdd.setVisible(true);
	}

	/**
	 * Adds the employee from the new employee pop-up window to the database.
	 * @param event
	 */
	public void addEmployee(ActionEvent event) {
		// check all fields are filled in
		if (isNotEmpty()) {
			String firstname = txtFirstName.getText();
			String lastname = txtLastName.getText();
			String username = txtUsername.getText();
			String password = txtPassword.getText();
			// checks in username is unique
			if (isNotUnique(username)) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText(null);
				alert.setContentText("The username is already taken. Please choose a different username.");
				alert.showAndWait();
			}
			else {
				// checks if the password and confirm password have the same value
				if (isPassword()) {
					PreparedStatement preparedStatement = null;
					Connection connection = SQLiteConnection.Connector();
					// add employee to database
					String query = "insert into employees (firstname, lastname, username, password, manager) values (?,?,?,?,0)";
					try {
						connection.setAutoCommit(false);
						preparedStatement = connection.prepareStatement(query);
						preparedStatement.setString(1, firstname);
						preparedStatement.setString(2, lastname);
						preparedStatement.setString(3, username);
						preparedStatement.setString(4, password);
						preparedStatement.executeUpdate();
						connection.commit();
						updateTable();
						dialogAdd.setVisible(false);
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						try {
							preparedStatement.close();
							connection.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}			
					}
				}
				// if passwords don't match
				else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Dialog");
					alert.setHeaderText(null);
					alert.setContentText("The passwords do not match.");
					alert.showAndWait();
				}
			}
		}
		// if all fields not filled out
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please fill in all fields correctly.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Checks if the password and conform password  are the same.
	 * @return True if the passwords match. False if the passwords do not match.
	 */
	private boolean isPassword() {
		if (txtPassword.getText().equals(txtConfirmPassword.getText())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if the username is unique or not.
	 * @param user The new username entered by the manager for the new employee.
	 * @return True if the username is already in use. False if the username is not already in use.
	 */
	private boolean isNotUnique(String user) {
		Connection connection = SQLiteConnection.Connector();
		try {	
			String query = "select * from employees";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				usernames.add(resultSet.getString("username"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {				
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (usernames.contains(user)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if any of the fields in the new employee pop-up are empty.
	 * @return True if there are no empty fields. False if there are empty fields.
	 */
	private boolean isNotEmpty() {
		if ((txtFirstName.getText().length() > 0) && (txtLastName.getText().length() > 0) && (txtUsername.getText().length() > 0) 
				&& (txtPassword.getText().length() > 0) && (txtConfirmPassword.getText().length() > 0)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Closes the add employee pop-up box.
	 * @param event
	 */
	public void back(ActionEvent event) {
		dialogAdd.setVisible(false);
	}

	/**
	 * Asks for confirmation from the user before deleting an employee. If OK is clicked, the employee is deleted.
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		if(isSelected()) {
			int selected = tblEmployees.getSelectionModel().getSelectedIndex();
			if(isManager(colEmployeeID.getCellData(selected))) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Dialog");
				alert.setHeaderText(null);
				alert.setContentText("You cannot delete a manager.");
				alert.showAndWait();
			}
			else {
				String employeename = colName.getCellData(selected);
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setHeaderText(null);
				alert.setContentText("Are you sure you want to delete the employee " + employeename + "?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
					delete();
				} else {
					alert.close();
				}
			}
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an employee from the table.");
			alert.showAndWait();
		}
	}

	/**
	 * Deletes the selected employee from the database.
	 */
	private void delete() {
		int selected = tblEmployees.getSelectionModel().getSelectedIndex();
		int employeeID = colEmployeeID.getCellData(selected);
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		try {	
			String query = "delete from employees where employeeid = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, employeeID);
			preparedStatement.execute();
			tblEmployees.getItems().remove(selected);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {				
				preparedStatement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	/**
	 * Checks if the selected employee is a manager.
	 * @param id The employeeID of the selected employee.
	 * @return True if the selected employee is a manager. False if the selected employee is not a manager.
	 */
	private boolean isManager(int id) {
		Connection connection = SQLiteConnection.Connector();
		try {	
			String query = "select * from employees where manager = 1";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				managers.add(resultSet.getInt("employeeid"));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {				
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (managers.contains(id)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks that an employee is selected from the table.
	 * @return True if an employee is selected. False if nothing is selected.
	 */
	private boolean isSelected() {
		if (tblEmployees.getSelectionModel().getSelectedIndex() != -1) {
			return true;
		}
		else {
			return false;
		}
	}	
	
	/**
	 * Open the view log window.
	 * @param event
	 */
	public void viewLog(ActionEvent event) {
		if (isSelected()) {
			int selected = tblEmployees.getSelectionModel().getSelectedIndex();
			// set the values of employeeID and employeeName for the employee log
			employeeID = colEmployeeID.getCellData(selected);
			employeeName = colName.getCellData(selected);
			try {
				// close current window
				((Node)event.getSource()).getScene().getWindow().hide();
				// open employee log window
				Stage primaryStage = new Stage();
				Parent root = FXMLLoader.load(getClass().getResource("../EmployeeLog.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("Employee log");
				// reopen employees window when close log window
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						try {
							Stage primaryStage = new Stage();
							Parent root = FXMLLoader.load(getClass().getResource("../Employees.fxml"));
							Scene scene = new Scene(root);
							scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
							primaryStage.setScene(scene);
							primaryStage.show();
							primaryStage.setTitle("Employees");	
						} catch (IOException e) {
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
			// if no employee selected
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an employee from the table.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Closes the employees screen.
	 * @param event
	 */
	public void close(ActionEvent event) {
		((Node)event.getSource()).getScene().getWindow().hide();
	}
}
