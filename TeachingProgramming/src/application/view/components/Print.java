package application.view.components;

import java.io.IOException;

import application.model.GlobalVariableTable;
import application.model.VariableType;
import application.view.DraggableNode;
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

	//reference to this components owner node
	DraggableNode sourceNode;

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

	@Override
	public void executeComponentFunction() {

		//GET THE BUILDINGPANE BY CLIMBING THROUGH HEAPS OF FAMILY MEMBERS TO FINNANLY FIND THE DAMN TEXT AREA SO WE CAN APPEND TEXT THAT UPDATES
		//AnchorPane buildingPane = (AnchorPane) getParent().getParent().getParent().getParent().getParent().getParent();

		//wow much ez
		AnchorPane rootAnchor = (AnchorPane) getScene().lookup("#rootAnchor");
		TextArea area = (TextArea) rootAnchor.lookup("#consoleTextArea");

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
