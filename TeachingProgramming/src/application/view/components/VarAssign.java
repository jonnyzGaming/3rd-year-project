package application.view.components;

import java.io.IOException;

import application.model.GlobalVariableTable;
import application.model.VariableType;
import application.view.DraggableNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

//assigns global variables in system with new values.
public class VarAssign extends AnchorPane implements Component {

	@FXML
	AnchorPane root_pane;

	@FXML
	TextField name_field;
	@FXML
	TextField value_field;
	@FXML
	ComboBox<String> function_combo_box;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//reference to this components owner node
	DraggableNode sourceNode;

	//print only has 1 control line socket
	Component socketComponent = null;
	Component portComponent = null;

	ObservableList<String> options = FXCollections.observableArrayList("=", "+=", "-=", "*", "/");

	public VarAssign() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/VarAssign.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();

		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML
	private void initialize() {
		setSockets();
		setPorts();

		function_combo_box.setItems(options);
		function_combo_box.setValue("=");
	}

	public String getAssignmentType() {
		return function_combo_box.getValue();
	}

	//getters/setters
	@Override
	public void setSockets() {
		socketType = Component.SocketType.BASIC;
		socketNumber = 1;

	}

	@Override
	public void setPorts() {
		portType = Component.PortType.BASIC;
		portNumber = 1;
	}

	@Override
	public int getSocketNumber() {
		return socketNumber;

	}

	@Override
	public int getPortNumber() {
		return portNumber;
	}

	@Override
	public SocketType getSocketType() {
		return socketType;

	}

	@Override
	public PortType getPortType() {
		return portType;

	}

	@Override
	public void setSourceDraggableNode(DraggableNode n) {
		this.sourceNode = n;
	}

	@Override
	public void registerPortLink(Component c, int portNum) {
		portComponent = c;

	}

	@Override
	public void registerSocketLink(Component c) {
		socketComponent = c;
	}

	@Override
	public void removePortLink(int portNumber, boolean fromSource) {
		if (fromSource) {
			portComponent.removeSocketLink(1, false);
		}
		portComponent = null;
		sourceNode.Port1open = true;

	}

	@Override
	public void removeSocketLink(int originalPortNumber, boolean fromSource) {
		if (fromSource) {
			socketComponent.removePortLink(originalPortNumber, false);
		}

		socketComponent = null;
		sourceNode.Socket1open = true; //not very clearn but works

	}

	public String getVariableName() {
		return name_field.getText();

	}

	public String getVariableValue() {
		return value_field.getText();
	}

	@Override
	public void executeComponentFunction() {

		//lookup the variable in global var array and change its value if found
		//get the var / value to be checked.
		String variableLookup = getVariableName();
		String value = getVariableValue();

		VariableType var = GlobalVariableTable.getVariable(variableLookup);
		if (var != null) {

			//check for if value is a variable
			VariableType potentialVar = GlobalVariableTable.getVariable(value);
			if (potentialVar != null) value = potentialVar.toString();

			boolean success = true;

			//basic set to a new value
			if (getAssignmentType().equals("=")) {
				success = var.setValue(value);
			}

			//rest are for integers only.
			else if (getAssignmentType().equals("+=") && var.isInt(value)) {

				int varInt = var.getIntValue();
				int addToInt = 0;
				if (potentialVar != null) {
					addToInt = potentialVar.getIntValue();
				} else {
					//make sure are value field is an int for this case.
					try {
						addToInt = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						success = false;
					}
				}

				if (success) {
					int result = varInt + addToInt;
					String resultString = result + "";
					success = var.setValue(resultString);
				}

			} else if (getAssignmentType().equals("-=") && var.isInt(value)) {

				int varInt = var.getIntValue();
				int minusToInt = 0;
				if (potentialVar != null) {
					minusToInt = potentialVar.getIntValue();
				} else {
					//make sure are value field is an int for this case.
					try {
						minusToInt = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						success = false;
					}
				}

				if (success) {
					int result = varInt - minusToInt;
					String resultString = result + "";
					success = var.setValue(resultString);
				}

			} else if (getAssignmentType().equals("*") && var.isInt(value)) {

				int varInt = var.getIntValue();
				int multiplyToInt = 0;
				if (potentialVar != null) {
					multiplyToInt = potentialVar.getIntValue();
				} else {
					//make sure are value field is an int for this case.
					try {
						multiplyToInt = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						success = false;
					}
				}

				if (success) {
					int result = varInt * multiplyToInt;
					String resultString = result + "";
					success = var.setValue(resultString);
				}

			} else if (getAssignmentType().equals("/") && var.isInt(value)) {

				int varInt = var.getIntValue();
				int divideToInt = 0;
				if (potentialVar != null) {
					divideToInt = potentialVar.getIntValue();
				} else {
					//make sure are value field is an int for this case.
					try {
						divideToInt = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						success = false;
					}
				}

				if (success) {
					int result = varInt / divideToInt;
					String resultString = result + "";
					success = var.setValue(resultString);
				}

			}

			if (!success) {
				//alert the user that the set was invalid
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("assignment");
				alert.setHeaderText("Assignment failure");
				alert.setContentText("one of more assignments have values that cant be set to the variable type.");
				alert.showAndWait();
			}

		} else {
			//alert the user they actually entered an unrecognised variable
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("assignment");
			alert.setHeaderText("Assignment failure");
			alert.setContentText("one or more assingnments have names that are unregognised.");
			alert.showAndWait();
		}

		//if we have a port component linked then execute that
		if (portComponent != null) portComponent.executeComponentFunction();

	}
}
