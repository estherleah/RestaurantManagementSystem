package application.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import application.Order;
import application.SQLiteConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller class for the search all orders screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class SearchOrdersController implements Initializable {
	
	// list of all the orders - used to initialise the table with all the orders
	private ObservableList<Order> allOrders = FXCollections.observableArrayList();

	// values for the table number combo box
	private ObservableList<Integer> tableNumber = FXCollections.observableArrayList(1,2,3,4,5);
	
	DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MMM-yyyy");	
	
	// list of all the orders - used to search for specific orders
	private ObservableList<Order> all = FXCollections.observableArrayList();;
	
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
	 * Initialises the values in the table of all the orders.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		comboSearchTables.setItems(tableNumber);
		
		colOrderID.setCellValueFactory(
		        new PropertyValueFactory<Order,Integer>("orderID"));
		colTable.setCellValueFactory(
		        new PropertyValueFactory<Order,Integer>("tableNumber"));
		colDate.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("dateCreated"));
		colTime.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("timeCreated"));
		colTotal.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("totalCost"));
		colComments.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("comments"));
		colItemsOrdered.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("itemsOrdered"));
		
		// populate the table
		update(null);

		// enable the search functionality
		search();
	}
	
	/**
	 * Updates the values in the table of all orders so that all orders are shown.
	 * @param event
	 */
	public void update(ActionEvent event) {
		// clear all orders from table
		tblAllOrders.getItems().clear();
		// set search boxes back to default values
		comboSearchTables.getSelectionModel().clearSelection();
		txtSearch.clear();
		dateFrom.setValue(null);
		dateTo.setValue(null);
		Connection connection = SQLiteConnection.Connector();
		try {
			// select all orders from database
			String query = "select tablenumber, orders.orderid, date, time, total, comments from orders";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				Order order = new Order();
				order.orderID.set(resultSet.getInt("orderid"));
				order.tableNumber.set(resultSet.getInt("tablenumber"));
				order.dateCreated.set(resultSet.getString("date"));
				order.timeCreated.set(resultSet.getString("time"));		
				order.comments.set(resultSet.getString("comments"));
				order.itemsOrdered.set(getItems(resultSet.getInt("orderid")));
				String total = resultSet.getString("total");
				if (total.contains("£")) {
					total = total.substring(total.indexOf("£") + 1);
				}
				BigDecimal totalCost = new BigDecimal(total);
				order.totalCost.set(NumberFormat.getCurrencyInstance().format(totalCost));
				allOrders.add(order);
				all.add(order);
			}
			// add all orders to the table
			tblAllOrders.setItems(allOrders);
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
	 * Searches the table based on the text being input.
	 */
	protected void search() {
		txtSearch.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldValue, String newValue) {
				filterList(oldValue, newValue);
			}			
		});				
	}
	
	/**
	 * Searches all orders for orders from the selected table number.
	 * @param event
	 */
	public void searchTable(ActionEvent event) {
		dateFrom.setValue(null);
		dateTo.setValue(null);
		ObservableList<Order> filteredTables = FXCollections.observableArrayList();
		if (comboSearchTables.getSelectionModel().getSelectedIndex() != -1) {
			for (Order order : all) {
				if (order.getTableNumber() == comboSearchTables.getValue()) {
					filteredTables.add(order);
				}
			}
			// clear the table and set the orders in it to the ones for the selected table
			tblAllOrders.getItems().clear();
			tblAllOrders.setItems(filteredTables);
		}
	}
	
	/**
	 * Checks if the date of the order is within the range of dates specified by the user.
	 * @param date The date to check.
	 * @return True if the date is within the range. False if the date is not within the range.
	 */
	protected boolean isWithinRange(LocalDate date) {
		if ((date.isAfter(dateFrom.getValue()) && date.isBefore(dateTo.getValue())) || (date.equals(dateFrom.getValue())) || (date.equals(dateTo.getValue()))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if the dates picked by the user are a valid range - i.e. is the first date before the second date.
	 * @return True if the dates picked by the user represent a valid range of dates. False if the dates are not a valid range.
	 */
	protected boolean isRange() {
		if (dateFrom.getValue().isBefore(dateTo.getValue()) || dateFrom.getValue().equals(dateTo.getValue())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Searches the list of all orders and shows those which are in the selected date range.
	 * @param event
	 */
	public void searchDate(ActionEvent event) {
		comboSearchTables.getSelectionModel().clearSelection();
		ObservableList<Order> filteredList = FXCollections.observableArrayList();
		if (isRange()) {
			// check all the orders
			for (Order order : all) {
    			String orderDate = order.getDateCreated();
       			LocalDate date = LocalDate.parse(orderDate, format);
    			if(isWithinRange(date)) {
    				filteredList.add(order);
    			}
    		}
			// add items to the order
			tblAllOrders.setItems(filteredList);
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("The second date is before the first date.");
			alert.showAndWait();
		}
	}

	/**
	 * Gets the items from the database for the specified order and returns them as a single string.
	 * @param order The orderID of the required order.
	 * @return A string with a list of all the items ordered.
	 */
	protected String getItems(int order) {
		String items = "";
		Connection connection = SQLiteConnection.Connector();
		try {
			// select all the distinct items from database for the order
			String query = "select distinct item from individualorder where orderid = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, order);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				items = resultSet.getString(resultSet.getRow());
			}
			while(resultSet.next()) {
				items = items + ", " + resultSet.getString("item");
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
		return items;
	}

	/**
	 * Filters the list of orders in the table based on the item or comment types into the text box. 
	 * If an orders contains that item or comment then the order is shown on the table.
	 * (Start source: https://www.youtube.com/watch?v=AJc1Gsviqac).
	 * @param oldText The previous text in the text box.
	 * @param newText The current text in the text box.
	 */
	protected void filterList(String oldText, String newText) {
		ObservableList<Order> filteredList = FXCollections.observableArrayList();
		if(txtSearch == null || (newText.length() < oldText.length()) || newText == null) {
//			dateFrom.setValue(null);
//			dateTo.setValue(null);
//			comboSearchTables.getSelectionModel().clearSelection();
            tblAllOrders.setItems(all);
        }
		else {
        	newText = newText.toLowerCase();
    		for (Order order : tblAllOrders.getItems()) {
    			String filterItemsOrdered = order.getItemsOrdered();
    			String filterComments = String.valueOf(order.getComments());
    			if (filterItemsOrdered.toLowerCase().contains(newText) || filterComments.toLowerCase().contains(newText)) {
    				filteredList.add(order);
    			}
    		}
    		tblAllOrders.setItems(filteredList);
        }		
	}
}
