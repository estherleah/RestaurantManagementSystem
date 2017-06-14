package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * An order in the restaurant.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class Order {	
	public SimpleIntegerProperty orderID = new SimpleIntegerProperty();
	public SimpleIntegerProperty tableNumber = new SimpleIntegerProperty();
	public SimpleStringProperty timeCreated = new SimpleStringProperty();
	public SimpleStringProperty dateCreated = new SimpleStringProperty();
	public SimpleStringProperty employee = new SimpleStringProperty();
	public SimpleStringProperty totalCost = new SimpleStringProperty();
	public SimpleStringProperty comments = new SimpleStringProperty();
	public SimpleStringProperty itemsOrdered = new SimpleStringProperty();

	/**
	 * Sets the items ordered in the order.
	 * @param items The items ordered.
	 */
	public void setItemsOrdered(String items) {
		itemsOrdered.set(items);
	}
	
	/**
	 * Gets the items ordered.
	 * @return The items ordered.
	 */
	public String getItemsOrdered() {
		return itemsOrdered.get();
	}
	
	/**
	 * Sets the table number for the order.
	 * @param tableNo The table number.
	 */
	public void setTableNumber(int tableNo) {
		tableNumber.set(tableNo);
	}
	
	/**
	 * Gets the table number for the order.
	 * @return The table number for the order.
	 */
	public Integer getTableNumber() {
		return tableNumber.get();
	}
	
	/**
	 * Sets the ID for the order.
	 * @param order The ID for the order.
	 */
	public void setOrderID(int order) {
		orderID.set(order);
	}
	
	/**
	 * Gets the ID for the order.
	 * @return The ID for the order.
	 */
	public Integer getOrderID() {
		return orderID.get();
	}

	/**
	 * Sets the time the order was created.
	 * @param time The time the order is created.
	 */
	public void setTimeCreated(String time) {
		timeCreated.set(time);
	}	
	
	/**
	 * Gets the time the order was created.
	 * @return The time the order was created.
	 */
	public String getTimeCreated() {
		return timeCreated.get();
	}
	
	/**
	 * Sets the date the order was created.
	 * @param date The date the order was created.
	 */
	public void setDateCreated(String date) {
		timeCreated.set(date);
	}	
	
	/**
	 * Gets the date the order was created.
	 * @return The date the order was created.
	 */
	public String getDateCreated() {
		return dateCreated.get();
	}
	
	/**
	 * Sets the employee who created the order.
	 * @param employeeID The employee who created the order.
	 */
	public void setEmployee(String employeeID) {
		employee.set(employeeID);
	}
	
	/**
	 * Gets the employee who created the order.
	 * @return The employee who created the order.
	 */
	public String getEmployee() {
		return employee.get();
	}
	
	/**
	 * Sets the total price of the order.
	 * @param cost The total price of the order as a String.
	 */
	public void setTotalCost(String cost) {
		totalCost.set(cost);
	}
	
	/**
	 * Gets the total price of the order.
	 * @return The total price of the order as a String.
	 */
	public String getTotalCost() {
		return totalCost.get();
	}
	
	/**
	 * Sets the comment for the order.
	 * @param comment The comment.
	 */
	public void setComments(String comment) {
		comments.set(comment);
	}
	
	/**
	 * Gets the comment for the order.
	 * @return The comment.
	 */
	public String getComments() {
		return comments.get();
	}
}