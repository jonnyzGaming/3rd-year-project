package application.view.components;

import java.io.IOException;

import application.view.DraggableNode;
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

	//reference to this components owner node
	DraggableNode sourceNode;

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
	public void registerPortLink(application.view.components.Component c, int portNum) {

		//should be 1 or 2
		if (portsOccupied < 2) {

			if (portNum == 1) leftLinked = c;
			if (portNum == 2) rightLinked = c;
		}

		portsOccupied++;

	}

	@Override
	public void registerSocketLink(application.view.components.Component c) {

		socketsOccupied++;
		mainSocket = c;

	}

	//on removing a port need to remove it from both this components port and its linked components socket.
	@Override
	public void removePortLink(int portNumber, boolean fromSource) {
		if (portNumber == 1) {

			if (fromSource) {
				leftLinked.removeSocketLink(1, false);
			}
			leftLinked = null;
			sourceNode.Port1open = true;
			portsOccupied--;

		}
		if (portNumber == 2) {
			if (fromSource) {
				rightLinked.removeSocketLink(2, false);
			}
			rightLinked = null;
			sourceNode.Port2open = true;
			portsOccupied--;
		}
	}

	@Override
	public void removeSocketLink(int originalPortNumber, boolean fromSource) {

		if (fromSource) {
			mainSocket.removePortLink(originalPortNumber, false);
		}

		mainSocket = null;
		sourceNode.Socket1open = true; //not very clearn but works
	}

	@Override
	public void setSourceDraggableNode(DraggableNode n) {
		this.sourceNode = n;
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
