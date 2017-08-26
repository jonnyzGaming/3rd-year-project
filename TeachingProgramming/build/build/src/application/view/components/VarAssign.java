package application.view.components;

import java.io.IOException;

import application.model.GlobalVariableTable;
import application.model.VariableType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//print only has 1 control line socket
	Component socketComponent = null;
	Component portComponent = null;

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
	public void registerPortLink(Component c) {
		portComponent = c;

	}

	@Override
	public void registerSocketLink(Component c) {
		socketComponent = c;
	}

	@Override
	public void removePortLink(int portNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSocketLink(int portNumber) {
		socketComponent.removePortLink(portNumber);
		socketComponent = null;

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

			boolean success = var.setValue(value);

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
