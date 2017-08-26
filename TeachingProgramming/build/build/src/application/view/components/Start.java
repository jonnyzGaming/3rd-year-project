package application.view.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

//uses to start the execution tree
public class Start extends AnchorPane implements Component {

	@FXML
	AnchorPane root_pane;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//1 control line socket
	Component socketComponent = null;
	Component portComponent = null;

	public Start() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Start.fxml"));

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
		socketNumber = 0;

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

		//if we have a port component linked then execute that
		if (portComponent != null) {
			portComponent.executeComponentFunction();
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("Start must link to something!");
			alert.setContentText("No components linked to start");

			alert.showAndWait();
		}
	}

}
