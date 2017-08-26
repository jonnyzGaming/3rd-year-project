package application.view.components;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

/*
 * A sequencer component has 2 ports and is used for control flow to execute 1 component after another one.
 */
public class Sequencer extends AnchorPane implements Component {

	@FXML
	AnchorPane root_pane;

	//ports and socket settings
	Component.PortType portType = null;
	SocketType socketType = null;
	public int socketNumber, portNumber;

	//ports
	int portsOccupied = 0;
	Component leftLinked, rightLinked;
	int socketsOccupied = 0;
	Component mainSocket;

	public Sequencer() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Sequencer.fxml"));

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
	public void registerPortLink(application.view.components.Component c) {
		portsOccupied++;

		if (portsOccupied == 1) leftLinked = c;
		if (portsOccupied == 2) rightLinked = c;

	}

	@Override
	public void registerSocketLink(application.view.components.Component c) {

		socketsOccupied++;
		mainSocket = c;

	}

	//on removing a port need to remove it from both this components port and its linked components socket.
	@Override
	public void removePortLink(int portNumber) {
		if (portNumber == 1) {
			leftLinked = null;
			portsOccupied--;

			leftLinked.removeSocketLink(1);

		}
		if (portNumber == 2) {
			rightLinked = null;
			portsOccupied--;
		}
	}

	@Override
	public void removeSocketLink(int portNumber) {
		mainSocket.removePortLink(portNumber);
		mainSocket = null;
	}

	//Passes control first to the left linked component then the right.
	@Override
	public void executeComponentFunction() {

		//need both ports to be filled for this component
		if (leftLinked != null && rightLinked != null) {

			leftLinked.executeComponentFunction();
			rightLinked.executeComponentFunction();
		} else {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Error");
			alert.setHeaderText("Sequencer not set up correctly");
			alert.setContentText("Sequencers are required to have exactly two port connections!");

			alert.showAndWait();
		}
		// TODO Auto-generated method stub

	}

}
