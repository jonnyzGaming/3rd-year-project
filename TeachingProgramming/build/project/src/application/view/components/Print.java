package application.view.components;

import java.io.IOException;

import application.model.GlobalVariableTable;
import application.model.VariableType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

//executes a set of instructions in sequence.
public class Print extends AnchorPane implements Component {

	@FXML
	AnchorPane root_pane;

	@FXML
	TextField print_field;
	@FXML
	TextField extra_print_field;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//print only has 1 control line socket
	Component socketComponent = null;
	Component portComponent = null;

	public Print() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Print.fxml"));

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

	//get print text
	public String getPrintText() {
		return print_field.getText();
	}

	//get print for variables
	public String getVarPrintText() {
		return extra_print_field.getText();
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

	@Override
	public void executeComponentFunction() {

		//print to both standard out and the text area because its NEXT LEVEL ADVANCED print component
		System.out.print(getPrintText());

		//GET THE BUILDINGPANE BY CLIMBING THROUGH HEAPS OF FAMILY MEMBERS TO FINNANLY FIND THE DAMN TEXT AREA SO WE CAN APPEND TEXT THAT UPDATES
		AnchorPane buildingPane = (AnchorPane) getParent().getParent().getParent().getParent().getParent().getParent();
		TextArea area = (TextArea) buildingPane.lookup("#consoleTextArea");

		//print standard text
		area.appendText(getPrintText());

		//add a variable
		VariableType var = GlobalVariableTable.getVariable((getVarPrintText()));
		if (var != null) {
			if (var.getType().equals("Int")) area.appendText("" + var.getIntValue());
			if (var.getType().equals("Boolean")) area.appendText("" + var.getBooleanValue());
			if (var.getType().equals("String")) area.appendText(var.getStringValue());
		}

		area.appendText("\n>");

		//if we have a port component linked then execute that
		if (portComponent != null) portComponent.executeComponentFunction();

	}
}
