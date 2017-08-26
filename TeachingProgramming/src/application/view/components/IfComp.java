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

//executes a set of instructions in sequence.
public class IfComp extends AnchorPane implements Component {
	@FXML
	AnchorPane root_pane;

	@FXML
	TextField variable_field;
	@FXML
	TextField value_field;
	@FXML
	ComboBox<String> condition_combo_box;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//reference to this components owner node
	DraggableNode sourceNode;

	//if else has 1 socket and 2 ports, SUCCESS = THEN, FAILURE = ELSE.
	int portsOccupied = 0, socketsOccupied = 0;
	Component mainSocketComponent;
	Component successPortComponent;
	Component elsePortComponent;

	ObservableList<String> options = FXCollections.observableArrayList("==", ">", "<", ">=", "<=");

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

		condition_combo_box.setItems(options);
		condition_combo_box.setValue("==");
	}

	public String getVariableText() {
		return variable_field.getText();
	}

	public String getValueText() {
		return value_field.getText();
	}

	//getters for variable component
	public String getCondtionType() {
		return condition_combo_box.getValue();
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
	public void setSourceDraggableNode(DraggableNode n) {
		this.sourceNode = n;
	}

	@Override
	public void registerPortLink(application.view.components.Component c, int portNum) {

		//should be 1 or 2
		if (portsOccupied < 2) {

			if (portNum == 1) successPortComponent = c;
			if (portNum == 2) elsePortComponent = c;
		}

		portsOccupied++;

	}

	@Override
	public void registerSocketLink(Component c) {
		socketsOccupied++;
		mainSocketComponent = c;
	}

	@Override
	public void removePortLink(int portNumber, boolean fromSource) {
		if (portNumber == 1) {

			if (fromSource) {
				successPortComponent.removeSocketLink(1, false);
			}
			successPortComponent = null;
			sourceNode.Port1open = true;
			portsOccupied--;

		}
		if (portNumber == 2) {
			if (fromSource) {
				elsePortComponent.removeSocketLink(2, false);
			}
			elsePortComponent = null;
			sourceNode.Port2open = true;
			portsOccupied--;
		}
	}

	@Override
	public void removeSocketLink(int originalPortNumber, boolean fromSource) {

		if (fromSource) {
			mainSocketComponent.removePortLink(originalPortNumber, false);
		}

		mainSocketComponent = null;
		sourceNode.Socket1open = true; //not very clean but works
	}

	@Override
	public void executeComponentFunction() {

		boolean passed = false;
		//get the var / value to be checked.
		String variableLookup = getVariableText();
		String value = getValueText();

		VariableType var = GlobalVariableTable.getVariable(variableLookup);
		if (var != null) {

			//can be ==,>,<,>=,<=
			String condition = getCondtionType();

			//check for if the value is actually a variable wow!
			VariableType potentialVar = GlobalVariableTable.getVariable(value);
			if (potentialVar != null) value = potentialVar.toString();

			//equals test
			if (condition.equals("==") && var.hasValue(value)) {
				passed = true;
			}

			if (condition.equals(">") && var.isInt(value) && var.isGreaterThan(value)) {
				passed = true;
			}

			if (condition.equals("<") && var.isInt(value) && var.isLessThan(value)) {
				passed = true;
			}

			if (condition.equals(">=") && var.isInt(value) && var.isGreaterThanEqual(value)) {
				passed = true;
			}

			if (condition.equals("<=") && var.isInt(value) && var.isLessThanEqual(value)) {
				passed = true;
			}

		} else {
			//alert the user they actually entered an unrecognised variable
			passed = false;
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("if statement");
			alert.setHeaderText("problem in the if statement");
			alert.setContentText("There seems to be a problem in your if statement! Make sure everything is correct.");
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
