package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * An item in the restaurant's menu.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class Menu {
	
	public SimpleIntegerProperty itemID = new SimpleIntegerProperty();
	public SimpleStringProperty item = new SimpleStringProperty();
	public SimpleStringProperty price = new SimpleStringProperty();
	public SimpleStringProperty category = new SimpleStringProperty();
	
	/**
	 * Sets the menu item's ID.
	 * @param id The value of the menu item's ID.
	 */
	public void setItemID(int id) {
		itemID.set(id);
	}

	/**
	 * Gets the menu item's ID.
	 * @return The menu item's ID.
	 */
	public int getItemID() {
		return itemID.get();
	}		
	
	/**
	 * Sets the name of the item.
	 * @param item The name of the item.
	 */
	public void setItem(String item) {
		this.item.set(item);
	}

	/**
	 * Gets the name of the item.
	 * @return The name of the item.
	 */
	public String getItem() {
		return item.get();
	}
	
	/**
	 * Sets the price of the item.
	 * @param price The price of the item as a String.
	 */
	public void setPrice(String price) {
		this.price.set(price);
	}

	/**
	 * Gets the price of the item as a String.
	 * @return The price of the item.
	 */
	public String getPrice() {
		return price.get();
	}
	
	/**
	 * Sets the category of the item.
	 * @param category The category of the item.
	 */
	public void setCategory(String category) {
		this.category.set(category);
	}
	
	/**
	 * Gets the category of the item.
	 * @return The category of the item.
	 */
	public String getCategory() {
		return category.get();
	}
}
