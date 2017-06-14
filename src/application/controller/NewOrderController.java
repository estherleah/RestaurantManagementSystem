package application.controller;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import application.IndividualOrder;
import application.Log;
import application.SQLiteConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller class for the new order.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class NewOrderController extends OrderController implements Initializable {
	
	private ObservableList<String> menuItems = FXCollections.observableArrayList();
	private ObservableList<String> categories = FXCollections.observableArrayList();
	private ObservableList<Integer> quantity = FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10);	

	@FXML
	private Label lblTableNumber;
	
	@FXML
	private Label lblTotal;
	
	@FXML
	private TableView<IndividualOrder> tblTableOrder;
	
	@FXML
    private TableColumn<IndividualOrder, String> colItem;
	
	@FXML
    private TableColumn<IndividualOrder, Integer> colQuantity;
	
	@FXML
    private TableColumn<IndividualOrder, String> colRequests;
	
	@FXML
    private TableColumn<IndividualOrder, String> colPrice;
	
	@FXML
	private TextArea txtComments;
	
	@FXML
	private TextField txtRequests;
	
	@FXML
    private ComboBox<String> comboCategory;
	
	@FXML
    private ComboBox<Integer> comboQuantity;
	
	@FXML
	private ListView<String> listItems;
	
	DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy");	
	String currentDate;
	String currentTime;
	int orderNumber;
	String comments;
	
	/**
	 * Initialises the menu items and creates a new order.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// prepare date and time for the order
		currentTime = LocalTime.now().toString().substring(0, 5);
		currentDate = LocalDate.now().format(format).toString();
		
		// set label for table number and total
		lblTableNumber.setText("Table: " + MainController.tableNumber);
		lblTotal.setText("£0.00");
		
		// add values to combo boxes
		comboQuantity.setItems(quantity);
		Connection connection = SQLiteConnection.Connector();
		try {		
			String query = "select * from categories";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				String category = resultSet.getString("categoryname");
				categories.add(category);
			}
			// add categories
			comboCategory.setItems(categories);
			String query2 = "select * from menu order by category desc";
			ResultSet resultSet2 = connection.createStatement().executeQuery(query2);
			while(resultSet2.next()) {
				String item = resultSet2.getString("item");
				menuItems.add(item);
			}
			// add menu items to list
			listItems.setItems(menuItems);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// prepare columns in the table
		colItem.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,String>("item"));
		colQuantity.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,Integer>("quantity"));
		colRequests.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,String>("requests"));
		colPrice.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,String>("price"));
		
		// create order
		createNewOrder();
		Log.addToEmployeeLog("created a new order " + getOrderID() + ".");
	}
	
	/**
	 * Creates a new order for the table.
	 */
	private void createNewOrder() {
		PreparedStatement preparedStatement = null;
		Connection connection = SQLiteConnection.Connector();
		String query = "insert into orders (tablenumber, date, time, total, employeeid, current) values (?,?,?,?,?,1)";
		try {
			// create order in the database
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, MainController.tableNumber);
			preparedStatement.setString(2, currentDate);
			preparedStatement.setString(3, currentTime);
			preparedStatement.setString(4, "0");
			preparedStatement.setInt(5, LoginController.employeeid);
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
	
	/**
	 * Closes the order window and opens the main window.
	 * @param event
	 */
	public void done(ActionEvent event) {	
		// checks if there are items in the order
		// if want to have an order with no items e.g. if customers just want tap water while they are deciding what to order - need to close the window with the X in the corner
		if(tblTableOrder.getItems().size() > 0) {
			super.done(event);
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("There are no items in the current order. Please add items to complete the order.");
			alert.showAndWait();
		}
	}
}
