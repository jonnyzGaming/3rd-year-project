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

//executes a set of instructions in sequence.
public class IfComp extends AnchorPane implements Component {
	@FXML
	AnchorPane root_pane;

	@FXML
	TextField variable_field;
	@FXML
	TextField value_field;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//if else has 1 socket and 2 ports, SUCCESS = THEN, FAILURE = ELSE.
	int portsOccupied = 0, socketsOccupied = 0;
	Component mainSocketComponent;
	Component successPortComponent;
	Component elsePortComponent;

	public IfComp() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/IfComp.fxml"));

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

	public String getVariableText() {
		return variable_field.getText();
	}

	public String getValueText() {
		return value_field.getText();
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
		portNumber = 2;
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
		portsOccupied++;

		if (portsOccupied == 1) successPortComponent = c;
		if (portsOccupied == 2) elsePortComponent = c;

	}

	@Override
	public void registerSocketLink(Component c) {
		socketsOccupied++;
		mainSocketComponent = c;
	}

	@Override
	public void removePortLink(int portNumber) {

	}

	@Override
	public void removeSocketLink(int portNumber) {
		//socketComponent.removePortLink(portNumber);
		//socketComponent = null;

	}

	@Override
	public void executeComponentFunction() {

		boolean passed = false;
		//get the var / value to be checked.
		String variableLookup = getVariableText();
		String value = getValueText();

		VariableType var = GlobalVariableTable.getVariable(variableLookup);
		if (var != null) {

			if (var.hasValue(value)) {
				passed = true;
			}

		} else {
			//alert the user they actually entered an unrecognised variable
			passed = false;
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("if statement");
			alert.setHeaderText("variable name");
			alert.setContentText("No variable name matching your entry was found");
			alert.showAndWait();
		}

		//pass control over on success to the connected port, 
		if (successPortComponent != null && passed) {
			successPortComponent.executeComponentFunction();
		} else if (elsePortComponent != null) {
			elsePortComponent.executeComponentFunction();
		}

	}
}
