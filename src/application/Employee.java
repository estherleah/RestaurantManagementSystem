package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * An employee at the restaurant.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class Employee {
	
	public SimpleIntegerProperty employeeID = new SimpleIntegerProperty();
	public SimpleStringProperty firstname = new SimpleStringProperty();
	public SimpleStringProperty lastname = new SimpleStringProperty();
	public SimpleStringProperty name = new SimpleStringProperty();
	
	/**
	 * Sets the employee's ID.
	 * @param id The value of the employee's ID.
	 */
	public void setEmployeeID(int id) {
		employeeID.set(id);
	}
	
	/**
	 * Gets the employee's ID.
	 * @return The employee's ID.
	 */
	public int getEmployeeID() {
		return employeeID.get();
	}

	/**
	 * Sets the first name of the employee.
	 * @param first The first name of the employee.
	 */
	public void setFirstname(String first) {
		this.firstname.set(first);
	}

	/**
	 * Gets the first name of the employee.
	 * @return The first name of the employee.
	 */
	public String getFirstname() {
		return firstname.get();
	}
	
	/**
	 * Sets the last name of the employee.
	 * @param last The last name of the employee.
	 */
	public void setLastname(String last) {
		lastname.set(last);
	}

	/**
	 * Gets the last name of the employee.
	 * @return The last name of the employee.
	 */
	public String getLastname() {
		return lastname.get();
	}
	
	/**
	 * Sets the name of the employee.
	 * @param name The name of the employee.
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * Gets the name of the employee.
	 * @return The name of the employee.
	 */
	public String getName() {
		return name.get();
	}
}