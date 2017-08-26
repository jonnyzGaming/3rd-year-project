package application.view.components;

import java.io.IOException;

import application.view.DraggableNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

//a variable with a type,name and value.
public class Variable extends AnchorPane implements Component {

	@FXML
	AnchorPane root_pane;

	@FXML
	TextField var_name;

	@FXML
	TextField var_value;
	@FXML
	ComboBox<String> type_combo;

	//reference to this components owner node
	DraggableNode sourceNode;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	ObservableList<String> options = FXCollections.observableArrayList("Int", "String", "Boolean");

	public Variable() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Variable.fxml"));

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

		//fill combo box with available set of var types
		type_combo.setItems(options);
		type_combo.setValue("Int");
	}

	//check to ensure inputted data is valid
	public boolean validateVariable() {

		//if type is set
		if (getVariableType() != null) {
			//for int need a valid int
			if (getVariableType().equals("Int")) {
				if (getVariableName() != null && getVariableValue() != null) {
					try {
						int x = Integer.parseInt(getVariableValue());
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}

			}

			//for String need a valid string, which can be anything.
			if (getVariableType().equals("String")) {
				if (getVariableName() != null && getVariableValue() != null) {
					return true;
				}
			}

			if (getVariableType().equals("Boolean")) {
				if (getVariableName() != null && getVariableValue() != null) {
					return "true".equals(getVariableValue()) || "false".equals(getVariableValue());
				}
			}
		}

		return false;
	}

	//getters for variable component
	public String getVariableType() {
		return type_combo.getValue();
	}

	public String getVariableName() {
		return var_name.getText();

	}

	public String getVariableValue() {
		return var_value.getText();
	}

	//get the data from the textboxes, check for validity.

	//getters/setters
	@Override
	public void setSockets() {
		socketType = Component.SocketType.BASIC;
		socketNumber = 0;

	}

	@Override
	public void setPorts() {
		portType = Component.PortType.BASIC;
		portNumber = 0;
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
		return;

	}

	@Override
	public void registerSocketLink(Component c) {
		return;

	}

	@Override
	public void removePortLink(int portNumber, boolean fromSource) {
		return;

	}

	@Override
	public void removeSocketLink(int portNumber, boolean fromSource) {
		return;

	}

	@Override
	public void executeComponentFunction() {
		return;

	}
}
