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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;

//executes a set of instructions in sequence.
public class Loop extends AnchorPane implements Component {

	@FXML
	AnchorPane root_pane;
	@FXML
	TextField number_field;
	@FXML
	TextField variable_field;
	@FXML
	TextField value_field;
	@FXML
	ComboBox<String> condition_combo_box;
	@FXML
	RadioButton amount_radio;
	@FXML
	RadioButton while_radio;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//reference to this components owner node
	DraggableNode sourceNode;

	//print only has 1 control line socket
	Component socketComponent = null;
	Component portComponent = null;

	ObservableList<String> options = FXCollections.observableArrayList("==", ">", "<", ">=", "<=");

	//for differnt comp options
	final ToggleGroup group = new ToggleGroup();

	public Loop() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Loop.fxml"));

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

		//add radio buttons to a group
		amount_radio.setToggleGroup(group);
		amount_radio.setSelected(true);
		while_radio.setToggleGroup(group);

		//set some user data that we can collect from radio button
		amount_radio.setUserData("amount");
		while_radio.setUserData("while");

	}

	public String getNumberText() {
		return number_field.getText();
	}

	public String getVariableText() {
		return variable_field.getText();
	}

	public String getValueText() {
		return value_field.getText();
	}

	public String getOptionSelected() {
		return group.getSelectedToggle().getUserData().toString();
	}

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

	public boolean validateLoopInput(String value) {
		try {
			int x = Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void executeComponentFunction() {

		//handle amount seletion
		if (getOptionSelected().equals("amount")) {
			int loopTimes = 0;

			String value = getNumberText();

			//check to see if input is actually a variable first
			VariableType potentialVar = GlobalVariableTable.getVariable(value);
			if (potentialVar != null) {
				value = potentialVar.toString();
				if (potentialVar.isInt(value)) {
					loopTimes = Integer.parseInt(value);
					for (int i = 0; i < loopTimes; i++) {
						if (portComponent != null) portComponent.executeComponentFunction();
					}
				} else {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Loop");
					alert.setHeaderText("loop input failure");
					alert.setContentText(
							"Problem in loop component, option 'set amount', can only loop an integer variable.");
					alert.showAndWait();
				}
			} else if (validateLoopInput(value)) {
				loopTimes = Integer.parseInt(getNumberText());

				for (int i = 0; i < loopTimes; i++) {
					if (portComponent != null) portComponent.executeComponentFunction();
				}

			} else {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Loop");
				alert.setHeaderText("loop input failure");
				alert.setContentText(
						"Problem in loop component, option 'set amount', value inputted is not an integer");
				alert.showAndWait();
			}

		}

		//handle while selection
		if (getOptionSelected().equals("while")) {
			boolean continueLooping = true;

			//loop up until while condition is met
			while (continueLooping) {
				continueLooping = false;
				String variableLookup = getVariableText();
				String value = getValueText();

				VariableType var = GlobalVariableTable.getVariable(variableLookup);
				if (var != null) {

					String condition = getCondtionType();
					VariableType potentialVar = GlobalVariableTable.getVariable(value);
					if (potentialVar != null) value = potentialVar.toString();

					//string/bool/int equals check
					if (condition.equals("==") && var.hasValue(value)) {
						continueLooping = true;
					}

					//integer condition checks
					if (condition.equals(">") && var.isInt(value) && var.isGreaterThan(value)) {
						continueLooping = true;
					}

					if (condition.equals("<") && var.isInt(value) && var.isLessThan(value)) {
						continueLooping = true;
					}

					if (condition.equals(">=") && var.isInt(value) && var.isGreaterThanEqual(value)) {
						continueLooping = true;
					}

					if (condition.equals("<=") && var.isInt(value) && var.isLessThanEqual(value)) {
						continueLooping = true;
					}

					//actual loop branch
					if (continueLooping) {
						if (portComponent != null) portComponent.executeComponentFunction();
					}

				} else {
					continueLooping = false;
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Loop");
					alert.setHeaderText("loop input failure");
					alert.setContentText("Problem in loop component, option 'while', variable input not recognised");
					alert.showAndWait();
				}
			}

		}

	}
}
