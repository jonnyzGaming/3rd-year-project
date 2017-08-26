package application.view;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

/*
 * A custom control that represents a drag icon.
 * Simply used to specify the type of component from the list.
 */
public class DragIcon extends AnchorPane {

	@FXML
	AnchorPane root_pane;

	private ComponentType mType;

	public DragIcon() {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/DragIcon.fxml"));

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
	}

	public ComponentType getType() {
		return mType;
	}

	public void relocateToPoint(Point2D p) {

		Point2D localCoords = getParent().sceneToLocal(p);

		relocate((int) (localCoords.getX() - (getBoundsInLocal().getWidth() / 2)), (int) (localCoords.getY()
				- (getBoundsInLocal().getHeight() / 2)));
	}

	//lists all the different types of components available.
	public void setType(ComponentType type) {

		mType = type;

		getStyleClass().clear();
		getStyleClass().add("dragicon");
		if (this.getChildren().size() > 0) getChildren().clear();

		Label text;
		switch (mType) {

			case start:
				getStyleClass().add("icon-brightgreen");

				text = new Label("Start");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;
			case print:
				getStyleClass().add("icon-blue");

				text = new Label("Print");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;

			case sequencer:
				getStyleClass().add("icon-green");
				text = new Label("Sequencer");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;

			case variable:
				getStyleClass().add("icon-gold");
				text = new Label("Variable");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;

			case ifcomp:
				getStyleClass().add("icon-grey");
				text = new Label("if statement");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;

			case loop:
				getStyleClass().add("icon-brown");
				text = new Label("Loop");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;
			case assignment:
				getStyleClass().add("icon-purple");
				text = new Label("Assignment");
				text.relocate(13, 20);
				text.setFont(new Font("Arial", 15));
				getChildren().add(text);
				break;

			default:
				break;
		}
	}
}
