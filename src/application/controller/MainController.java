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

import application.Log;
import application.Order;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Controller class for the main screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class MainController implements Initializable {
	
	// the table number of the selected order
	protected static String tableNumber;
	// the total of the selected order - for payment
	protected static String total;
	
	private ObservableList<Order> currentOrders = FXCollections.observableArrayList();
	
	@FXML
	private Label lblEmployee;
	
	@FXML
	private TableView<Order> tblCurrentOrders;
	
	@FXML
    private TableColumn<Order, Integer> colOrderID;
	
	@FXML
    private TableColumn<Order, Integer> colTable;
	
	@FXML
    private TableColumn<Order, String> colTime;
	
	@FXML
    private TableColumn<Order, String> colCreatedBy;
	
	@FXML
    private TableColumn<Order, String> colTotal;
	
	
	/**
	 * Initialise the values in the table of current orders.
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		colOrderID.setCellValueFactory(
		        new PropertyValueFactory<Order,Integer>("orderID"));
		colTable.setCellValueFactory(
		        new PropertyValueFactory<Order,Integer>("tableNumber"));		
		colTime.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("timeCreated"));
		colCreatedBy.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("employee"));
		colTotal.setCellValueFactory(
		        new PropertyValueFactory<Order,String>("totalCost"));
		
		// connect to database
		Connection connection = SQLiteConnection.Connector();
		try {
			// select all current orders from database
			String query = "select tablenumber, orderid, time, firstname, lastname, total from orders join employees "
					+ "where current = 1 and orders.employeeid = employees.employeeid";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			String total = "";
			while(resultSet.next()) {
				Order order = new Order();
				order.tableNumber.set(resultSet.getInt("tablenumber"));
				order.orderID.set(resultSet.getInt("orderid"));
				order.timeCreated.set(resultSet.getString("time"));
				order.employee.set(resultSet.getString("firstname") + " " + resultSet.getString("lastname"));				
				total = resultSet.getString("total");
				if (total.contains("£")) {
					total = total.substring(total.indexOf("£") + 1);
				}
				BigDecimal totalCost = new BigDecimal(total);
				order.totalCost.set(NumberFormat.getCurrencyInstance().format(totalCost));
				currentOrders.add(order);
			}
			tblCurrentOrders.setItems(currentOrders);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}				
		
		// add name to welcome label
		lblEmployee.setText("Welcome " + LoginController.employee);
	}
	
	/**
	 * Signs out of the application and shows the login screen.
	 * @param event
	 */
	public void signOut(ActionEvent event) {
		((Node)event.getSource()).getScene().getWindow().hide();	
		try {
			Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("../Login.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Login");
			Log.addToEmployeeLog("logged out.");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Checks if there is a current order for a table.
	 * @param event
	 * @return True of the table has a current order. False if the table does not have a current order.
	 */
	private boolean isCurrentOrder(MouseEvent event) {
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		String table = ((Node)event.getSource()).getId();
		tableNumber = table.substring(5);
		String query = "select * from orders where tablenumber = ? and current = 1";
		// check no current order on database
		try {			
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, tableNumber);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return true;
			}
			else {
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		finally {
			try {
				preparedStatement.close();
				resultSet.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * If there is a current order, the order is opened. If there is no current order, a new order is created.
	 * @param event
	 */
	public void editTable(MouseEvent event) {
		Stage primaryStage = new Stage();
		((Node)event.getSource()).getScene().getWindow().hide();
		if (isCurrentOrder(event)) {
			try {
				// open the order to edit
				Parent root = FXMLLoader.load(getClass().getResource("../Order.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("Order");				
				Log.addToEmployeeLog("opened order " + getOrderID() + ".");
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				// open a new order
				Parent root = FXMLLoader.load(getClass().getResource("../NewOrder.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("New order");
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		// when close the new/edit order - reopen the main window
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
					Log.addToEmployeeLog("closed order " + getOrderID() + ".");
				} catch (IOException e) {
					e.printStackTrace();
				}						
			}			
		});
	}
	
	/**
	 * Gets the orderID of the selected order from the database.
	 * @return The orderID of the selected order.
	 */
	private int getOrderID() {
		int orderNumber = 0;
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			// find the current order for the selected table
			String query = "select * from orders where tablenumber = ? and current = 1";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, tableNumber);
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
	 * Opens the selected current order so it can be edited.
	 * @param event
	 */
	public void editOrder(ActionEvent event) {
		if (tblCurrentOrders.getSelectionModel().getSelectedIndex() != -1) {
			int selected = tblCurrentOrders.getSelectionModel().getSelectedIndex();
			int selectedTable = colTable.getCellData(selected);
			// sets the tableNumber to the selected table
			tableNumber = String.valueOf(selectedTable);
			try {
				// hide current window
				((Node)event.getSource()).getScene().getWindow().hide();
				// open edit order window
				Stage primaryStage = new Stage();
				Parent root = FXMLLoader.load(getClass().getResource("../Order.fxml"));
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				primaryStage.setTitle("Order");
				Log.addToEmployeeLog("opened order " + colOrderID.getCellData(selected) + ".");
				// when close edit window, open main window
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
							e.printStackTrace();
						}						
					}			
				});
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		// if no order selected
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an order from the table.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Asks for confirmation to delete an order. If OK is clicked then the order is deleted from the records.
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		if (tblCurrentOrders.getSelectionModel().getSelectedIndex() != -1) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Are you sure you want to delete the current order from the records?");
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
			alert.setContentText("Please select an order from the table.");
			alert.showAndWait();
		}
	}

	/**
	 * Deletes the selected order from the records.
	 */
	private void delete() {
		int selected = tblCurrentOrders.getSelectionModel().getSelectedIndex();
		int orderID = colOrderID.getCellData(selected);
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;
		try {	
			String query = "delete from orders where orderid = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, orderID);
			preparedStatement.execute();
			tblCurrentOrders.getItems().remove(selected);
			String query2 = "delete from individualorder where orderid = ?";
			preparedStatement2 = connection.prepareStatement(query2);
			preparedStatement2.setInt(1, orderID);
			preparedStatement2.execute();
			Log.addToEmployeeLog("deleted order " + orderID + ".");
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
	 * Opens a new window where all the orders can be viewed and searched.
	 * @param event
	 */
	public void viewAllOrders(ActionEvent event){
//		((Node)event.getSource()).getScene().getWindow().hide();		
		try {
			Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("../SearchOrders.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Search orders");
			Log.addToEmployeeLog("viewed all orders.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Displays the different payment options using an alert box with multiple buttons (source: http://code.makery.ch/blog/javafx-dialogs-official/). 
	 * If payment is to be made by cash a change calculator is opened.
	 * @param event
	 */
	public void displayPayOptions(ActionEvent event) {
		if (tblCurrentOrders.getSelectionModel().getSelectedIndex() != -1) {
			int selected = tblCurrentOrders.getSelectionModel().getSelectedIndex();
			int orderID = colOrderID.getCellData(selected);
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
				// calculate change
				total = colTotal.getCellData(selected).substring(1);
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
								tblCurrentOrders.getItems().remove(selected);
								CalculatorController.isPaid = false;
							}													
						}			
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (result.get() == btnDebit) {
				removeCurrentOrder(orderID);
				tblCurrentOrders.getItems().remove(selected);
			} else if (result.get() == btnCredit) {
				removeCurrentOrder(orderID);
				tblCurrentOrders.getItems().remove(selected);
			} else {
				alert.close();
			}
		}
		else {
			// if no order selected
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an order from the table.");
			alert.showAndWait();
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

	/**
	 * Open the manager login screen.
	 * @param event
	 */
	public void viewManager(ActionEvent event){
		((Node)event.getSource()).getScene().getWindow().hide();
		try {
			Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("../ManagerLogin.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("../application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setTitle("Manager login");
			// when close reopen main screen
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}