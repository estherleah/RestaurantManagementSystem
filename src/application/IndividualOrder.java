package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * An individual item in an order.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class IndividualOrder extends Order {
	
	public SimpleStringProperty item = new SimpleStringProperty();
	public SimpleIntegerProperty quantity = new SimpleIntegerProperty();
	public SimpleStringProperty requests = new SimpleStringProperty();
	public SimpleStringProperty price = new SimpleStringProperty();
	
	/**
	 * Gets the item in the order.
	 * @return The item in the order.
	 */
	public String getItem() {
		return item.get();
	}
	
	/**
	 * Sets the item in the order.
	 * @param item The item in the order
	 */
	public void setItem(String item) {
		this.item.set(item);
	}
	
	/**
	 * Gets the quantity of the item.
	 * @return The quantity of the item.
	 */
	public Integer getQuantity() {
		return quantity.get();
	}
	
	/**
	 * Sets the quantity of the item.
	 * @param quantity The quantity of the item.
	 */
	public void setQuantity(int quantity) {
		this.quantity.set(quantity);
	}
	
	/**
	 * Gets any special requests for the item.
	 * @return The special requests for the item.
	 */
	public String getRequests() {
		return requests.get();
	}
	
	/**
	 * Sets a special request for the item
	 * @param resquest The special request.
	 */
	public void setRequests(String resquest) {
		requests.set(resquest);
	}
	
	/**
	 * Gets the price of the item.
	 * @return The price of the item as a String.
	 */
	public String getPrice() {
		return price.get();
	}
	
	/**
	 * Sets the price of the item.
	 * @param cost The price of the item as a String.
	 */
	public void setPrice(String cost) {
		price.set(cost);
	}
	
}
