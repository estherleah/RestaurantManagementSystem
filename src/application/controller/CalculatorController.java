package application.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;
import javafx.scene.control.Alert.AlertType;

/**
 * Calculates the change for the order if the order is paid for with cash.
 * @author Esther Leah Morrison
 * @version 1.0
 *
 */
public class CalculatorController {
	@FXML
	private Label lblDisplay;
	
	private boolean isCorrectPayment = false;	
	protected static boolean isPaid = false;	
	private String input = "";
	private String total = MainController.total;
	
	/**
	 * Add the input number to the display.
	 * @param event
	 */
	public void addNumber(ActionEvent event) {
		if (isDecimal()) { 
			if (input.substring(input.indexOf(".")).length() == 3) {
				displayError();
			}
			else {
				input = input + ((Button)event.getSource()).getText();
				lblDisplay.setText(input);
			}
		}
		else {
			input = input + ((Button)event.getSource()).getText();
			lblDisplay.setText(input);
		}		
	}
	
	/**
	 * Checks a decimal point has not already been added and add a decimal point to the number on the display.
	 * @param event
	 */
	public void addDecimal(ActionEvent event) {
		if (isDecimal()) {
			displayError();
		}
		else {
			if (input.length() == 0) {
				input = "0";
			}
			input = input + ".";
			lblDisplay.setText(input);
		}
	}
	
	/**
	 * Deletes the previously input value on the display.
	 * @param event
	 */
	public void deletePrevious(ActionEvent event) {
		if (input.length() > 0) {
			input = input.substring(0, input.length()-1);
			lblDisplay.setText(input);
		}
	}
	
	/**
	 * Calculates the change given for the order based on the amount input on the calculator. Shows the change on the display and through a dialog box.
	 * @param event
	 */
	public void calculateChange(ActionEvent event) {
		if(input.length() != 0) {	
			BigDecimal cost = new BigDecimal(Double.valueOf(total));
			BigDecimal money = new BigDecimal(Double.valueOf(input));
			BigDecimal change = money.subtract(cost);
			if (change.doubleValue() < 0) {
				// if change is negative - not given enough money
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning Dialog");
				alert.setHeaderText(null);
				alert.setContentText("Not enough money given.");
				alert.showAndWait();
				// reset the value on the calculator
				lblDisplay.setText("0");
				input = "";
			}
			else {
				// display the change and ask for confirmation
				lblDisplay.setText(change.setScale(2, RoundingMode.HALF_UP).toString());
				isCorrectPayment = true;
				// ((Node)event.getSource()).getScene().getWindow().hide();
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setHeaderText(null);
				alert.setContentText("The order cost " + NumberFormat.getCurrencyInstance().format(cost) + 
						". The customer is paying " + NumberFormat.getCurrencyInstance().format(money) + 
						". Please give " + NumberFormat.getCurrencyInstance().format(change) + " change.");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK){
				    pay(event);
				}
			}	
		}
		else {
			displayError();
		}
	}
	
	/**
	 * Pays for the order and changes it from a current order to a not current order 
	 * (source: http://stackoverflow.com/questions/24483686/how-to-force-javafx-application-close-request-programmatically).
	 * @param event
	 */
	private void pay(ActionEvent event) {
		input = "";
		if (isCorrectPayment) {
			isPaid = true;
			// forces the window to close using the window close request
			((Node)event.getSource()).getScene().getWindow().fireEvent(
					new WindowEvent(((Node)event.getSource()).getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
		}
	}
	
	/**
	 * Checks if the input value contains a decimal point.
	 * @return True if it contains a decimal. False if it does not contain a decimal.
	 */
	private boolean isDecimal() {
		if (input.contains(".")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Displays the error showing that the input is not valid.
	 */
	private void displayError() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(null);
		alert.setContentText("Please enter a valid number.");
		alert.showAndWait();
	}

}