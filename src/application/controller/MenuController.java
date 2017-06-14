package application.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Menu;
import application.SQLiteConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller class for the edit menu screen.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class MenuController implements Initializable {

	private int selectedItemID;

	private boolean isItemSelected = false;

	private ObservableList<Menu> menuItems = FXCollections.observableArrayList();

	private ObservableList<String> categories = FXCollections.observableArrayList();

	@FXML
	private TableView<Menu> tblMenu;

	@FXML
	private TableColumn<Menu, Integer> colItemID;

	@FXML
	private TableColumn<Menu, String> colCategory;

	@FXML
	private TableColumn<Menu, String> colItem;

	@FXML
	private TableColumn<Menu, String> colPrice;

	@FXML
	private DialogPane dialogEditMenu;

	@FXML
	private ComboBox<String> comboCategory;

	@FXML
	private TextField txtItem;

	@FXML
	private TextField txtPrice;

	/**
	 * Initialises the table of menu items and the dropdown menu of categories.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		colItemID.setCellValueFactory(
				new PropertyValueFactory<Menu,Integer>("itemID"));
		colCategory.setCellValueFactory(
				new PropertyValueFactory<Menu,String>("category"));		
		colItem.setCellValueFactory(
				new PropertyValueFactory<Menu,String>("item"));
		colPrice.setCellValueFactory(
				new PropertyValueFactory<Menu,String>("price"));

		Connection connection = SQLiteConnection.Connector();
		try {		
			String query = "select * from categories";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				categories.add(resultSet.getString("categoryname"));
			}
			comboCategory.setItems(categories);		
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		updateTable();		
	}

	/**
	 * Updates the items in the table to show the current menu items.
	 */
	private void updateTable() {
		// clear the table
		tblMenu.getItems().clear();
		Connection connection = SQLiteConnection.Connector();
		try {
			String query = "select * from menu order by category desc";
			ResultSet resultSet = connection.createStatement().executeQuery(query);
			while(resultSet.next()) {
				Menu menu = new Menu();
				menu.itemID.set(resultSet.getInt("itemid"));
				menu.category.set(resultSet.getString("category"));
				menu.item.set(resultSet.getString("item"));
				String price = resultSet.getString("price");
				// check  if there is a pound sign
				if (price.contains("£")) {
					price = price.substring(price.indexOf("£") + 1);
				}
				// convert to a BigDecimal and then into a currency instance
				BigDecimal totalCost = new BigDecimal(price);
				menu.price.set(NumberFormat.getCurrencyInstance().format(totalCost));
				menuItems.add(menu);
			}
			// add all the items to the table
			tblMenu.setItems(menuItems);
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
	 * Asks the user for confirmation before deleting an item. If the user clicks OK the item is deleted from the menu.
	 * @param event
	 */
	public void confirmDelete(ActionEvent event) {
		if(isSelected()) {
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
			// if no item selected
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an item from the table.");
			alert.showAndWait();
		}
	}

	/**
	 * Deletes the selected item from the database.
	 */
	private void delete() {
		int selected = tblMenu.getSelectionModel().getSelectedIndex();
		int orderID = colItemID.getCellData(selected);
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		try {	
			String query = "delete from menu where itemid = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, orderID);
			preparedStatement.execute();
			tblMenu.getItems().remove(selected);
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
	 * Displays a pop-up box so an item can be added to the menu.
	 * @param event
	 */
	public void addItem(ActionEvent event) {
		isItemSelected = false;
		comboCategory.getSelectionModel().clearSelection();
		txtItem.clear();
		txtPrice.clear();
		dialogEditMenu.setVisible(true);
	}
	
	/**
	 * Shows a pop-up box so an item from the menu can be edited.
	 * @param event
	 */
	public void editItem(ActionEvent event) {
		isItemSelected = true;
		if(isSelected()) {
			int selected = tblMenu.getSelectionModel().getSelectedIndex();
			selectedItemID = colItemID.getCellData(selected);
			getItem(selectedItemID);
			dialogEditMenu.setVisible(true);
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please select an item from the table.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Gets the details of the selected item and sets the initial values of the edit pop-up to match.
	 * @param id The itemID of the selected item.
	 */
	private void getItem(int id) {
		Connection connection = SQLiteConnection.Connector();
		try {
			String query = "select * from menu where itemid = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				comboCategory.setValue(resultSet.getString("category"));
				txtItem.setText(resultSet.getString("item"));
				BigDecimal price = new BigDecimal(resultSet.getString("price"));
				txtPrice.setText(NumberFormat.getCurrencyInstance().format(price));
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

	/**
	 * Updates the details of the selected menu item in the database.
	 */
	private void update() {
		String category = comboCategory.getSelectionModel().getSelectedItem();
		String item = txtItem.getText();
		String price = txtPrice.getText();
		if (price.contains("£")) {
			price = price.substring(price.indexOf("£") + 1);
		}
		Connection connection = SQLiteConnection.Connector();
		PreparedStatement preparedStatement = null;
		try {	
			// update the item
			String query = "update menu set item = ?, price = ?, category = ? where itemid = ?";
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, item);
			preparedStatement.setString(2, price);
			preparedStatement.setString(3, category);
			preparedStatement.setInt(4, selectedItemID);
			preparedStatement.executeUpdate();
			connection.commit();
			updateTable();
			// hide pop-up
			dialogEditMenu.setVisible(false);
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
	 * Adds a new menu item to the database.
	 */
	private void add() {
		String category = comboCategory.getSelectionModel().getSelectedItem();
		String item = txtItem.getText();
		String price = txtPrice.getText();
		if (price.contains("£")) {
			price = price.substring(price.indexOf("£") + 1);
		}
		PreparedStatement preparedStatement = null;
		Connection connection = SQLiteConnection.Connector();
		// add item to menu
		String query = "insert into menu (item, price, category) values (?,?,?)";
		try {
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, item);
			preparedStatement.setString(2, price);
			preparedStatement.setString(3, category);
			preparedStatement.executeUpdate();
			connection.commit();
			updateTable();
			// hide pop-up
			dialogEditMenu.setVisible(false);
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

	/**
	 * If an item was edited, the item is updated in the database. If a new item was added, the item is added to the database.
	 * @param event
	 */
	public void updateMenu(ActionEvent event) {
		// if valid input
		if(isCategorySelected() && isValidNumber() && isNotEmpty()) {
			// if editing an item
			if(isItemSelected) {
				update();
			}
			// if adding a new item
			else {
				add();
			}
		}
		else {
			// if invalid input
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Warning Dialog");
			alert.setHeaderText(null);
			alert.setContentText("Please fill in all fields correctly.");
			alert.showAndWait();
		}
	}
	
	/**
	 * Closes the pop-up edit/add item.
	 * @param event
	 */
	public void close(ActionEvent event) {
		dialogEditMenu.setVisible(false);
	}

	/**
	 * Checks if an item from the menu is selected.
	 * @return True if an item is selected. False if no item is selected.
	 */
	private boolean isSelected() {
		if (tblMenu.getSelectionModel().getSelectedIndex() != -1) {
			isItemSelected = true;
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if a category has been selected.
	 * @return True if a category has been selected. False if a category has not been selected.
	 */
	private boolean isCategorySelected() {
		if (!(comboCategory.getSelectionModel().getSelectedIndex() == -1)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Checks if the price field contains a valid number that can be used as a price (source: http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java)
	 * @return True if the number can be used as a price. False if the number is not in a valid format.
	 */
	private boolean isValidNumber() {
		String number = txtPrice.getText();
		if (number.length() > 0) {
			try {
				if (number.contains("£")) {
					number = number.substring(number.indexOf("£") + 1);
				}
				Double.parseDouble(number);
				return true;
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		else {
			return false;
		}
	}

	/**
	 * Checks if the text box for the item's name is empty.
	 * @return True if the text box is not empty. False if it is empty.
	 */
	private boolean isNotEmpty() {
		if (txtItem.getText().length() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

}