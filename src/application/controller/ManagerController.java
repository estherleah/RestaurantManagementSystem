package application.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

import application.Order;
import application.SQLiteConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Controller class for the manager screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class ManagerController extends SearchOrdersController {
	
	DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	
	@FXML
	private TableView<Order> tblAllOrders;
	
	@FXML
    private TableColumn<Order, Integer> colOrderID;
	
	@FXML
    private TableColumn<Order, Integer> colTable;
	
	@FXML
    private TableColumn<Order, String> colDate;
	
	@FXML
    private TableColumn<Order, String> colTime;
	
	@FXML
    private TableColumn<Order, String> colTotal;	
	
	@FXML
    private TableColumn<Order, String> colComments;
	
	@FXML
    private TableColumn<Order, String> colItemsOrdered;
	
	@FXML
    private TextField txtSearch;
	
	@FXML
	private ComboBox<Integer> comboSearchTables;
	
	@FXML
	private DatePicker dateFrom;
	
	@FXML
	private DatePicker dateTo;

	/**
	 * Reads the orders from a CSV file and adds them to the orders already in the database.
	 * @param event
	 */
	public void readCSV(ActionEvent event) {
		boolean success = false;
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose a CSV file to import");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
		File selectedFile = fileChooser.showOpenDialog(null);
		BufferedReader br;
		Connection connection = SQLiteConnection.Connector();
		if (selectedFile != null) {
			try {
				String query = "insert into orders (tablenumber, date, time, total, employeeid, comments, current) values (?,?,?,?,?,?,?)";
				String queryID = "select orderid from orders where tablenumber = ? and date=? and time = ? and total = ? and employeeid = ? and comments = ? and current = ?";
				String query2 = "insert into individualorder (orderid, item, quantity) values (?,?,1)";
				br = new BufferedReader(new FileReader(selectedFile));
				String text;
				// first line with list of column headings
				if ((text = br.readLine()) != null) {
					text.split(",");
				}
				while ((text = br.readLine()) != null) {
					int orderNumber = 0;
					String[] data = text.split(",");
					StringBuilder items = new StringBuilder();
					connection.setAutoCommit(false);
					// put the order into the database
					PreparedStatement preparedStatement = connection.prepareStatement(query);
					preparedStatement.setString(1, data[0]);
					preparedStatement.setString(2, data[1]);
					preparedStatement.setString(3, data[2]);
					preparedStatement.setString(4, data[3]);
					preparedStatement.setString(5, data[4]);
					preparedStatement.setString(6, data[5]);
					preparedStatement.setString(7, data[6]);
					preparedStatement.executeUpdate();
					connection.commit();
					// get the order id of the order just added
					PreparedStatement preparedStatement2 = connection.prepareStatement(queryID);
					preparedStatement2.setString(1, data[0]);
					preparedStatement2.setString(2, data[1]);
					preparedStatement2.setString(3, data[2]);
					preparedStatement2.setString(4, data[3]);
					preparedStatement2.setString(5, data[4]);
					preparedStatement2.setString(6, data[5]);
					preparedStatement2.setString(7, data[6]);
					ResultSet resultSet = preparedStatement2.executeQuery();
					while(resultSet.next()) {
						orderNumber = resultSet.getInt("orderid");				
					}
					// add the individual items ordered to the database
					for (int i = 7; i < data.length; i++) {
						items.append(data[i] + ", ");
						PreparedStatement preparedStatement3 = connection.prepareStatement(query2);
						preparedStatement3.setInt(1, orderNumber);
						preparedStatement3.setString(2, data[i]);
						preparedStatement3.executeUpdate();
						connection.commit();
					}
				}
				success = true;
			} catch (IOException | SQLException e) {
				success = false;
				e.printStackTrace();
			}
		}
		if (success) {
			successReadMessage(event);
		}
	}

	/**
	 * Writes all the orders displayed on the table to a CSV file - orders.csv.
	 * @param event
	 */
	public void writeCSV(ActionEvent event) {
		boolean success;
		FileWriter fw = null;
		File file = new File("orders.csv");
		Connection connection = SQLiteConnection.Connector();
		ObservableList<Order> selectedOrders = FXCollections.observableArrayList();
		selectedOrders = tblAllOrders.getItems();
		ResultSet resultSet;
		try {
			fw = new FileWriter(file);
			// write column headings
			fw.write("tablenumber,date,time,total,employeeid,comments,current,items");
			fw.write(System.getProperty("line.separator"));
			// for each order
			for (int i = 0; i < selectedOrders.size(); i++ ) {
				int orderID = colOrderID.getCellData(i);
				// get all the details
				String query = "select * from orders where orderid = ?";
				PreparedStatement preparedStatement = connection.prepareStatement(query);
				preparedStatement.setInt(1, orderID);
				resultSet = preparedStatement.executeQuery();
				// write to the file
				while(resultSet.next()) {
					fw.write(resultSet.getString("tablenumber"));
					fw.write(",");
					fw.write(resultSet.getString("date"));
					fw.write(",");	
					fw.write(resultSet.getString("time"));
					fw.write(",");	
					fw.write(resultSet.getString("total"));
					fw.write(",");	
					fw.write(resultSet.getString("employeeid"));
					fw.write(",");	
					if(resultSet.getString("comments") != null) {
						fw.write(resultSet.getString("comments"));
					}
					fw.write(",");	
					fw.write(resultSet.getString("current"));
					// find all the items ordered
					String query2 = "select distinct item from individualorder where orderid = ?";
					PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
					preparedStatement2.setString(1, resultSet.getString("orderid"));
					ResultSet resultSet2 = preparedStatement2.executeQuery();
					while(resultSet2.next()) {
						fw.write(",");
						fw.write(resultSet2.getString("item"));
					}
					fw.write(System.getProperty("line.separator"));
				}
			}
			success = true;
		}
		catch (IOException | SQLException e) {
			success = false;
			e.printStackTrace();
		}
		finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (success) {
			// display success message
			successWriteMessage(event);
		}
	}

	/**
	 * Displays a success message when orders have been successfully added.
	 * @param event
	 */
	private void successReadMessage(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(null);
		alert.setContentText("The data has been successfully added.");
		alert.showAndWait();
		update(event);
	}		
	
	/**
	 * Displays a success message when orders have been successfully exported.
	 * @param event
	 */
	private void successWriteMessage(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information Dialog");
		alert.setHeaderText(null);
		alert.setContentText("The data has been successfully exported.");
		alert.showAndWait();
	}
	
	/**
	 * Opens the menu screen so the menu can be viewed and edited.
	 * @param event
	 */
	public void editMenu(ActionEvent event) {	
		try {
			Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("../Menu.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Menu");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens the employees screen so the employees can be viewed and edited.
	 * @param event
	 */
	public void viewEmployees(ActionEvent event) {
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

}
