package application.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import application.IndividualOrder;
import application.Log;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller class for the order screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class OrderController implements Initializable {
	
	private ObservableList<String> menuItems = FXCollections.observableArrayList();
	private ObservableList<String> categories = FXCollections.observableArrayList();
	private ObservableList<Integer> quantity = FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10);	
	private ObservableList<IndividualOrder> currentOrder = FXCollections.observableArrayList();	
	
	@FXML
	private Label lblTableNumber;
	
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
	private ListView<String> listItems;
	
	@FXML
	private TextField txtRequests;
	
	@FXML
	private TextArea txtComments;
	
	@FXML
	private Label lblTotal;
	
	@FXML
    private ComboBox<String> comboCategory;
	
	@FXML
    private ComboBox<Integer> comboQuantity;
	
	@FXML
	private DialogPane dialogEdit;
	
	@FXML
	private TextField txtItem;
	
	@FXML
    private ComboBox<Integer> comboUpdateQuantity;
	
	@FXML
	private TextField txtUpdateRequests;
	
	private int orderNumber;
	
	/**
	 * Initialises the items in the table of items ordered, the list of all menu items and the cost of the order.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {		
		lblTableNumber.setText("Table: " + MainController.tableNumber);		
		// put values into the drop-down menus for quantity
		comboQuantity.setItems(quantity);
		comboUpdateQuantity.setItems(quantity);
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
		// put items in order into the current order table
		Connection connection2 = SQLiteConnection.Connector();
		String totalPrice = "0";
		String query2 = "select individualorder.item, quantity, specialrequests, price, total "
				+ "from orders join individualorder join menu "
				+ "where orders.orderid = individualorder.orderid and menu.item = individualorder.item and current = 1 and orders.tablenumber = ?";
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection2.prepareStatement(query2);
			preparedStatement.setString(1, MainController.tableNumber);
			ResultSet resultSet2 = preparedStatement.executeQuery();
			while(resultSet2.next()) {
				totalPrice = resultSet2.getString("total");
				IndividualOrder iOrder = new IndividualOrder();
				iOrder.item.set(resultSet2.getString("item"));
				iOrder.quantity.set(resultSet2.getInt("quantity"));
				iOrder.requests.set(resultSet2.getString("specialrequests"));
				String price = resultSet2.getString("price");
				BigDecimal itemPrice = new BigDecimal(Double.valueOf(price));
				BigDecimal quantity = new BigDecimal(iOrder.quantity.get());
				BigDecimal total = itemPrice.multiply(quantity);
				iOrder.price.set(NumberFormat.getCurrencyInstance().format(total));
				currentOrder.add(iOrder);
			}
			tblTableOrder.setItems(currentOrder);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {				
				connection2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		colItem.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,String>("item"));
		colQuantity.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,Integer>("quantity"));
		colRequests.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,String>("requests"));
		colPrice.setCellValueFactory(
		        new PropertyValueFactory<IndividualOrder,String>("price"));
		
/*		// set value of items already on the table
		oldItems = tblTableOrder.getItems();*/
		
		// add comments
		txtComments.setText(getComments());
		
		// add total price
		if (totalPrice.contains("£")) {
			totalPrice = totalPrice.substring(totalPrice.indexOf("£") + 1);
		}
		BigDecimal totalCost = new BigDecimal(totalPrice);
		lblTotal.setText(NumberFormat.getCurrencyInstance().format(totalCost));
	}
	
	/**
	 * Gets the comments for an order from the database.
	 * @return Any comment for the current order.
	 */
	private String getComments() {
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String comments = "";
		try {
			String query = "select * from orders where tablenumber = ? and current = 1";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, MainController.tableNumber);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				comments = resultSet.getString("comments");
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				preparedStatement.close();
				resultSet.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return comments;
	}
	
	/**
	 * Adds a comment to the order.
	 * @param event
	 */
	public void addComment(ActionEvent event) {
		String comments = txtComments.getText();
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		String query = "update orders set comments = ? where orderid = ?";
		try {			
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, comments);
			preparedStatement.setInt(2, getOrderID());
			preparedStatement.executeUpdate();
			connection.commit();
			if (txtComments.getText().length() > 0) {
				Log.addToEmployeeLog("added a comment to order " + getOrderID() + ".");
			}
			else {
				Log.addToEmployeeLog("deleted a comment from order " + getOrderID() + ".");
			}
		}
		catch (SQLException e) {
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
	 * Lists all the menu items from the selected category in the menu item list.
	 * @param event
	 */
	public void listItems(ActionEvent event) {
		listItems.getItems().clear();
		Connection connection = SQLiteConnection.Connector();
		try {		
			String query = "select * from menu where category = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, comboCategory.getValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				String item = resultSet.getString("item");
				menuItems.add(item);
			}
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
	}
	
	
	/**
	 * Adds the new item to the order.
	 * @param event
	 */
	public void addItem(ActionEvent event) {	
		String price = null;
		// check if item and quantity selected
		if ((listItems.getSelectionModel().getSelectedIndex() != -1) && (comboQuantity.getSelectionModel().getSelectedIndex() != -1)) {
			String selectedItem = listItems.getSelectionModel().getSelectedItem();
			int selectedQuantity = comboQuantity.getSelectionModel().getSelectedItem();
			String specialRequests = txtRequests.getText();
			Connection connection = SQLiteConnection.Connector();
			PreparedStatement preparedStatement;
			String query = "select price from menu where item = ?";
			try {			
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, selectedItem);
				ResultSet resultSet = preparedStatement.executeQuery();
				while(resultSet.next()) {
					price = resultSet.getString("price");
				}
				Log.addToEmployeeLog("added item (" + selectedItem + ") to order " + getOrderID() + ".");
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			IndividualOrder iOrder = new IndividualOrder();
			iOrder.item.set(selectedItem);
			iOrder.quantity.set(selectedQuantity);
			iOrder.requests.set(specialRequests);
			BigDecimal itemPrice = new BigDecimal(Double.valueOf(price));
			BigDecimal quantity = new BigDecimal(iOrder.quantity.get());
			BigDecimal total = itemPrice.multiply(quantity);
			iOrder.price.set(NumberFormat.getCurrencyInstance().format(total));	
			currentOrder.add(iOrder);
			addItemToOrder();
			tblTableOrder.setItems(currentOrder);
			updateTotal();
			// clear the combo boxes and the requests box
			txtRequests.clear();
//			comboQuantity.getSelectionModel().clearSelection();
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select the item and quantity to add to the order.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Adds the added item to the order to the database.
	 */
	protected void addItemToOrder() {
		updateTotal();
		String item = listItems.getSelectionModel().getSelectedItem();
		int quantity = comboQuantity.getSelectionModel().getSelectedItem();
		String requests = txtRequests.getText();
		String orderTotal = lblTotal.getText();
		// add the new item to the database
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		String query = "insert into individualorder (orderid, item, quantity, specialrequests) values (?,?,?,?)";
		String query2 = "update orders set total = ? where orderid = ?";
		try {
			// add item
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, getOrderID());
			preparedStatement.setString(2, item);
			preparedStatement.setInt(3, quantity);
			preparedStatement.setString(4, requests);
			preparedStatement.executeUpdate();
			connection.commit();
			// update total
			preparedStatement = connection.prepareStatement(query2);
			preparedStatement.setString(1, orderTotal);
			preparedStatement.setInt(2, getOrderID());
			preparedStatement.executeUpdate();
			connection.commit();
			updateTotal();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// display message if successfully updated
	}
	
	/**
	 * Asks for confirmation to delete an item. If OK is clicked then the item is deleted from the order.
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		if (isSelected()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Are you sure you want to delete the current item?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				delete();
			} else {
				alert.close();
			}
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an item to delete.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Deletes the selected item from the order.
	 */
	protected void delete() {
		int selected = tblTableOrder.getSelectionModel().getSelectedIndex();
		IndividualOrder i = tblTableOrder.getSelectionModel().getSelectedItem();
		String item = colItem.getCellData(i);
		String requests = colRequests.getCellData(i);
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		try {
			String query = "delete from individualorder where orderid = ? and item = ? and specialrequests = ?";
			String query2 = "update orders set total = ? where orderid = ?";
			// delete item for database
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, getOrderID());
			preparedStatement.setString(2, item);
			preparedStatement.setString(3, requests);
			preparedStatement.execute();
			// remove item from table
			tblTableOrder.getItems().remove(selected);
			// update order total
			updateTotal();
			// update total in database
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query2);
			preparedStatement.setString(1, lblTotal.getText());
			preparedStatement.setInt(2, getOrderID());
			preparedStatement.executeUpdate();
			connection.commit();
			Log.addToEmployeeLog("deleted item (" + item + ") from order " + getOrderID() + ".");
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
	}
	
	/**
	 * Opens a pop-up so the selected item can be edited; the quantity changed or a special request added.
	 * @param event
	 */
	public void editItem(ActionEvent event) {
		if(isSelected()) {
			IndividualOrder iOrder = tblTableOrder.getSelectionModel().getSelectedItem();
			txtItem.setText(iOrder.getItem());
			comboUpdateQuantity.setValue(iOrder.getQuantity());
			txtUpdateRequests.setText(iOrder.getRequests());
			txtItem.setEditable(false);
			dialogEdit.setVisible(true);
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an item to edit.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Cancels the editing of an item in the order and closes the pop-up.
	 * @param event
	 */
	public void cancel(ActionEvent event) {
		dialogEdit.setVisible(false);
	}
	
	/**
	 * Updates the item that the user just updated in the pop-up.
	 * @param event
	 */
	public void updateItem(ActionEvent event) {
		ObservableList<IndividualOrder> items = tblTableOrder.getItems();
		items.remove(tblTableOrder.getSelectionModel().getSelectedItem());
		String item = txtItem.getText();
		int quantity = comboUpdateQuantity.getValue();
		String requests = txtUpdateRequests.getText();
		String price = "";
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		String query = "update individualorder set quantity = ?, specialrequests = ? where item = ? and orderid = ?";
		PreparedStatement preparedStatement2;
		String query2 = "select price from menu where item = ?";
		String query3 = "update orders set total = ? where orderid = ?";
		try {
			// update in database
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, quantity);
			preparedStatement.setString(2, requests);
			preparedStatement.setString(3, item);
			preparedStatement.setInt(4, getOrderID());
			preparedStatement.executeUpdate();
			connection.commit();
			// look for the price
			preparedStatement2 = connection.prepareStatement(query2);
			preparedStatement2.setString(1, item);
			ResultSet resultSet = preparedStatement2.executeQuery();
			while(resultSet.next()) {
				price = resultSet.getString("price");
			}
			IndividualOrder iOrder = new IndividualOrder();
			iOrder.item.set(item);
			iOrder.quantity.set(quantity);
			iOrder.requests.set(requests);
			BigDecimal itemPrice = new BigDecimal(Double.valueOf(price));
			BigDecimal amount = new BigDecimal(iOrder.quantity.get());
			BigDecimal total = itemPrice.multiply(amount);
			iOrder.price.set(NumberFormat.getCurrencyInstance().format(total));
			items.add(iOrder);
			tblTableOrder.setItems(items);
			// update the order total
			updateTotal();
			String orderTotal = lblTotal.getText();
			preparedStatement = connection.prepareStatement(query3);
			preparedStatement.setString(1, orderTotal);
			preparedStatement.setInt(2, getOrderID());
			preparedStatement.executeUpdate();
			connection.commit();
			dialogEdit.setVisible(false);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Log.addToEmployeeLog("edited item (" + item + ") from order " + getOrderID() + ".");
	}
	
	/**
	 * Closes the order window and opens the main window.
	 * @param event
	 */
	public void done(ActionEvent event) {
		((Node)event.getSource()).getScene().getWindow().hide();
		try {
			Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("../Main.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Main");
			Log.addToEmployeeLog("finished editing order " + getOrderID() + ".");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if there is an item selected in the table.
	 * @return True if an item is selected. False if no item is selected.
	 */
	protected boolean isSelected() {
		if (tblTableOrder.getSelectionModel().getSelectedIndex() != -1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Updates the total value of the order.
	 */
	protected void updateTotal() {
		BigDecimal totalPrice = new BigDecimal(0.00);
		int size = tblTableOrder.getItems().size();
		for (int i = 0; i < size; i++) {
			String cost = colPrice.getCellData(i).substring(1);
			totalPrice = totalPrice.add(new BigDecimal(cost));
			lblTotal.setText(NumberFormat.getCurrencyInstance().format(totalPrice));
		}
		if (size == 0) {
			lblTotal.setText(NumberFormat.getCurrencyInstance().format(0.00));
		}
	}
	
	/**
	 * Get the ID of the current order.
	 * @return
	 */
	protected int getOrderID() {
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			String query = "select * from orders where tablenumber = ? and current = 1";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, MainController.tableNumber);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()) {
				orderNumber = resultSet.getInt("orderid");				
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				preparedStatement.close();
				resultSet.close();
				connection.close();			
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return orderNumber;
	}
	
	/**
	 * Displays the different payment options using an alert box with multiple buttons (source: http://code.makery.ch/blog/javafx-dialogs-official/). 
	 * If payment is to be made by cash a change calculator is opened.
	 * @param event
	 */
	public void displayPayOptions(ActionEvent event) {
		int orderID = getOrderID();
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Payment options");
		alert.setHeaderText(null);
		alert.setContentText("Please choose a payment option");
		// add buttons to the alert
		ButtonType btnCash = new ButtonType("Cash");
		ButtonType btnDebit = new ButtonType("Debit card");	
		ButtonType btnCredit = new ButtonType("Credit card");		
		ButtonType btnCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(btnCash, btnDebit, btnCredit, btnCancel);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == btnCash){
			MainController.total = lblTotal.getText().substring(1);
			try {
				Stage primaryStage = new Stage();
				Parent root = FXMLLoader.load(getClass().getResource("../Calculator.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("Calculate change");
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						// if correct amount paid
						if (CalculatorController.isPaid) {
							removeCurrentOrder(orderID);
							// source: http://stackoverflow.com/questions/24483686/how-to-force-javafx-application-close-request-programmatically
							((Node)event.getSource()).getScene().getWindow().fireEvent(new 
									WindowEvent(((Node)event.getSource()).getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
							CalculatorController.isPaid = false;
						}													
					}			
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (result.get() == btnDebit) {
			removeCurrentOrder(orderID);
			((Node)event.getSource()).getScene().getWindow().fireEvent(new WindowEvent(((Node)event.getSource()).getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
		} else if (result.get() == btnCredit) {
			removeCurrentOrder(orderID);
			((Node)event.getSource()).getScene().getWindow().fireEvent(new WindowEvent(((Node)event.getSource()).getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
		} else {
			alert.close();
		}
	}

	/**
	 * Changes the selected order from being a current order to being an old order.
	 * @param orderID The orderID of the selected order.
	 */
	private void removeCurrentOrder(int orderID) {
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		try {	
			String query = "update orders set current = 0 where orderid = ?";
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, orderID);
			preparedStatement.executeUpdate();
			connection.commit();
			Log.addToEmployeeLog("accepted payment for order " + orderID + ".");
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

}
