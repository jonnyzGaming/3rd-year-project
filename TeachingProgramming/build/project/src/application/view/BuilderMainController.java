package application.view;

import java.util.ArrayList;
import java.util.List;

import application.MainApp;
import application.model.DragContainer;
import application.model.GlobalVariableTable;
import application.model.VariableType;
import application.view.components.Start;
import application.view.components.Variable;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class BuilderMainController {

	//all the components to link with FXML file.

	@FXML
	private AnchorPane rootAnchor;
	@FXML
	private VBox componentPane;
	@FXML
	private Pane buildingPane; //change to anchor maybe later
	@FXML
	private SplitPane base_pane; //the main split pane in tool

	//icon drag drop elements.
	private DragIcon mDragOverIcon = null;
	private EventHandler mIconDragOverRoot = null;
	private EventHandler mIconDragDropped = null;
	private EventHandler mIconDragOverRightPane = null;

	@FXML
	private Label tutorialLabel;
	@FXML
	private Label PaletteLabel;
	@FXML
	private Label trackingLabel;
	@FXML
	private TextArea tutorialTextArea;
	@FXML
	private TextArea consoleTextArea;

	// Reference to the main application.
	private MainApp mainApp;

	/**
	 * The constructor.
	 * The constructor is called before the initialize() method.
	 */
	public BuilderMainController() {
	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded. Used for extra view stuff which hasnt been done with scenebuilder(fxml file).
	 * and events etc that are more complex and cannot be linked tradionality between scenebuilder-controller.
	 */
	@FXML
	private void initialize() {

		//Add one icon that will be used for the drag-drop process
		//This is added as a child to the root AnchorPane so it can be 
		//visible on both sides of the split pane.
		mDragOverIcon = new DragIcon();

		mDragOverIcon.setVisible(false);
		mDragOverIcon.setOpacity(0.65);
		rootAnchor.getChildren().add(mDragOverIcon);

		//populate left pane with multiple colored icons for testing
		for (int i = 0; i < ComponentType.values().length; i++) {

			DragIcon icn = new DragIcon();

			icn.setType(ComponentType.values()[i]);
			componentPane.getChildren().add(icn);

			addDragDetection(icn);
		}

		buildDragHandlers();

	}

	/**
	 * Builds reusable Event handler objects.
	 */
	private void buildDragHandlers() {

		//drag over transition to move comp from left pane to right pane
		mIconDragOverRoot = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				Point2D p = buildingPane.sceneToLocal(event.getSceneX(), event.getSceneY());

				if (!buildingPane.boundsInLocalProperty().get().contains(p)) {
					mDragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
					return;
				}

				event.consume();
			}
		};

		mIconDragOverRightPane = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				event.acceptTransferModes(TransferMode.ANY);

				mDragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

				event.consume();
			}
		};

		mIconDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				//retrieve container created in dragdetected handler from the dragbaord, update that container with
				//the co ords of where its been dropped, key = scene_coords, value = Point2d object.
				DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);
				container.addData("scene_coords", new Point2D(event.getSceneX(), event.getSceneY()));

				//add new clipboard to store updated container, then set the new info onto the main dragboard.
				ClipboardContent content = new ClipboardContent();
				content.put(DragContainer.AddNode, container);
				event.getDragboard().setContent(content);
				event.setDropCompleted(true);
			}
		};

		//handles cleanup
		rootAnchor.setOnDragDone(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				//remove the handlers from panes.
				buildingPane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRightPane);
				buildingPane.removeEventHandler(DragEvent.DRAG_DROPPED, mIconDragDropped);
				base_pane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRoot);

				mDragOverIcon.setVisible(false);

				//create a new draggable located at where it was dropped by getting data the icon(type).
				//THIS CODE ACTS AS THE DELEGATION, WHAT TO DO AFTER ITS DROPPED.
				DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

				if (container != null) {
					//only called if valid drag and drop done, create are new component in the building pane
					if (container.getValue("scene_coords") != null) {

						//create a new draggable node of the icons component type gathered from info in the container.
						DraggableNode node = new DraggableNode(ComponentType.valueOf(container.getValue("type")));
						buildingPane.getChildren().add(node);

						Point2D cursorPoint = container.getValue("scene_coords");

						node.relocateToPoint(new Point2D(cursorPoint.getX() - 32, cursorPoint.getY() - 32));
					}
				}

				//check data transfer works when nodes are moved around after(dragged nodes)
				//container = (DragContainer) event.getDragboard().getContent(DragContainer.DragNode);
				//
				//if (container != null) {
				//	if (container.getValue("type") != null) System.out.println("Moved node " + container.getValue(
				//			"type"));
				//}

				//AddLink drag operation
				container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

				if (container != null) {

					//bind the ends of our link to the nodes whose id's are stored in the drag container
					String sourceId = container.getValue("source");
					String targetId = container.getValue("target");

					if (sourceId != null && targetId != null) {

						Point2D pointSource = new Point2D(container.getValue("pointSourceX"), container.getValue(
								"pointSourceY"));
						Point2D pointTarget = new Point2D(container.getValue("pointTargetX"), container.getValue(
								"pointTargetY"));

						//visual nodelink.
						NodeLink link = new NodeLink();

						//add our link at the top of the rendering order so it's rendered first
						buildingPane.getChildren().add(0, link);

						//get the source/target objects from the ids transfered in the dragbaord container
						DraggableNode source = null;
						DraggableNode target = null;
						for (Node n : buildingPane.getChildren()) {

							if (n.getId() == null) continue;

							if (n.getId().equals(sourceId)) source = (DraggableNode) n;

							if (n.getId().equals(targetId)) target = (DraggableNode) n;

						}

						//link nodes ends visually and create link data into each of the nodes.
						if (source != null && target != null) {
							link.bindEnds(source, target, pointSource, pointTarget);

							source.registerPortLink(target);
							target.registerSocketLink(source);
						}

						//CODE TO DEFINE BETTER THAT THERE WAS A LINK MADE BETWEEN THE TWO DRAGGABLE COMPONENTS, NEED TO DSIABLE OTHER LINKS FROM BEING MADE TO THE
						//SAME SOCKET/PORT THAT JUST CREATED A LINK.
					}

				}

				event.consume();

			}
		});
	}

	/**
	 * Called when a drag is detected on one of the components.
	 */
	private void addDragDetection(DragIcon dragIcon) {

		dragIcon.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				// set the other drag event handles on their respective objects
				base_pane.setOnDragOver(mIconDragOverRoot);
				buildingPane.setOnDragOver(mIconDragOverRightPane);
				buildingPane.setOnDragDropped(mIconDragDropped);

				// get a reference to the clicked DragIcon object
				DragIcon icn = (DragIcon) event.getSource();

				//begin drag ops
				mDragOverIcon.setType(icn.getType());
				mDragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

				//content here is just the color of the icon, added to are container and but on board for storage.
				ClipboardContent content = new ClipboardContent();
				DragContainer container = new DragContainer();

				container.addData("type", mDragOverIcon.getType().toString());
				content.put(DragContainer.AddNode, container);

				mDragOverIcon.startDragAndDrop(TransferMode.ANY).setContent(content);
				mDragOverIcon.setVisible(true);
				mDragOverIcon.setMouseTransparent(true);
				event.consume();
			}
		});
	}

	//PROGRAM GENERATION/LOAD
	/**
	 * Called when the user clicks on the generate button. introspects the
	 * building area for the draggable nodes and reads types/links etc of
	 * all the components to generate some output.
	 */
	@FXML
	private void handleProgramGeneration() {
		//INSTROSPECT THE BUILDING AREA!

		//clear out table to start with of previous program vars.
		GlobalVariableTable.clearTable();

		//First get the entire list of components placed in the building area
		List<DraggableNode> componentList = new ArrayList<DraggableNode>();
		for (Node n : buildingPane.getChildren()) {
			if (n.getId() == null) continue;
			if (n instanceof DraggableNode) componentList.add((DraggableNode) n);
		}

		//first scan for global variables and maintain a program memory for them in objects
		for (DraggableNode N : componentList) {
			if (N.getType() == ComponentType.variable) {
				//if variable has valid input then put it in are simulation global program memory
				Variable var = ((Variable) (N.comp));
				if (var.validateVariable()) {
					//add to memory
					VariableType variableToStore = new VariableType(var.getVariableType(), var.getVariableName(), var
							.getVariableValue());

					//add to global lookup table.
					GlobalVariableTable.addVariable(variableToStore);

				} else {
					Alert alert = new Alert(AlertType.WARNING);
					alert.initOwner(mainApp.getPrimaryStage());
					alert.setTitle("Error");
					alert.setHeaderText("Construction Error");
					alert.setContentText("Error in global variables declared");
					alert.showAndWait();
					return;
				}
			}
		}

		//Next scan for root elements that starts the program tree. 
		for (DraggableNode N : componentList) {
			if (N.getType() == ComponentType.start) {
				((Start) N.comp).executeComponentFunction();
			}
		}

	}

	/**
	 * Called when the user clicks on the load button.
	 */
	@FXML
	private void handleDesignLoad() {

		//loads in a component design from an xml file which will fill the building area.

		//error found
		Alert alert = new Alert(AlertType.WARNING);
		alert.initOwner(mainApp.getPrimaryStage());
		alert.setTitle("Error");
		alert.setHeaderText("Load error");
		alert.setContentText("There was a problem loading the file");
		alert.showAndWait();
	}

	//TUTORIAL TABS
	/**
	 * Called when the user clicks on the getting started button.
	 */
	@FXML
	private void handleGettingStarted() {

		//fill text area with details on getting started tutorial and fill in palette with its set of components
		//PaletteArea.fillWithGettingStartedComponents();

		//set the tutorials text.
		setTutorialTextArea("Lets get started!\n\n"
				+ "Your first task is simply build a program that outputs the text: hello world.\n"
				+ "To do this we first need add a special 'start' component that tells us where to start, \n"
				+ "after adding that we can connect it with a print statement to print anything we want, give it a try!\n\n"
				+ "Next up how about we try and sequence two print statements together, try printing two lines one after another using the seqeuncer component!\n\n"
				+ "Variables are great ways to store data that we can use, your next task is to set a variable and print it, give it a try!\n\n"
				+ "Bobs age is 25, or it it? create a program that contratulates bob if his age is indeed 25, and curse him if not. You \n"
				+ "You will need to create a variable, and use an if statement to make sure he is not lying!\n\n"
				+ "Computers like to repeat things, use a loop to print out something as many times as you want.\n");

	}

	/**
	 * Handles mouse events
	 */
	@FXML
	private void handleMouse(MouseEvent event) {

		//Tracks the location of x, y co ords in the building area
		trackingLabel.setText("x: " + event.getX() + " y: " + event.getY());

	}

	/**
	 * Fills the tutorial text area with a string.
	 */
	@FXML
	public void setTutorialTextArea(String text) {

		tutorialTextArea.setText(text);
	}

	/**
	 * Adds text to the consoles text area
	 */
	@FXML
	public void setOutput(String text) {
		consoleTextArea.appendText(text + "\n>");
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

	}
}
