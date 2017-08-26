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
import javafx.scene.layout.VBox;

public class BuilderMainController {

	//all the components to link with FXML file.

	@FXML
	private AnchorPane rootAnchor;
	@FXML
	private VBox componentPane;
	@FXML
	private AnchorPane buildingPane; //change to anchor maybe later
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

						int sourcePortNumber = container.getValue("sourcePortNumber");

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

							source.registerPortLink(target, sourcePortNumber);
							target.registerSocketLink(source, sourcePortNumber);
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

		//set the tutorials text.
		setTutorialTextArea("Lets get started!\n\n"
				+ "Your first task is to simply build a program that outputs the text: Hello world!\n"
				+ "To do this we first need to add a special 'start' component that tells us where to start. \n"
				+ "After adding that we can connect it with a print statement to print anything we want, give it a try!\n\n"
				+ "Next up we will learn how to use the sequencer component, the sequencer can be used to link any two sets of components together,\n"
				+ "the left side will always be executed first followed by the right side.\n"
				+ "Task: Try printing two lines in sequence, perhaps the left side can say hello to the right side!\n\n"
				+ "Variables are great ways to store data with names that we can use, your next task is to set a variable of type integer,name bob to value 42 and print it, give it a try!\n\n"
				+ "If statements are a great way to make sure computers only do things IF we want them to. We are there masters of course!\n"
				+ "Your task: Bobs age is 65, and your age is? Create a program that contratulates bob IF his age is GREATER THAN your own, and well if not, then call yourself a lair!\n"
				+ "(hint) Need 2 variables for your age and bobs, 1 if statement, 2 prints.\n\n "
				+ "Computers like to repeat things. Task: use a LOOP to print out: I AM A ROBOT 10 times.\n\n"
				+ "To finnish the starting tutorials we will take a look at the assignment component. This can be used to SET new values for variables,\n"
				+ "Task: Try and use the assingment component to add two variables together and print the results.\n"
				+ "(hint) You will need two variables, use the += selection to add one variable onto the other and print its value!\n\n");

	}

	/**
	 * Called when the user clicks on the playing around button.
	 */
	@FXML
	private void handlePlayingAround() {

		//set the tutorials text.
		setTutorialTextArea("Now lets do some usefull things with are components!\n\n"
				+ "How about we start by getting the computer to print out the 12 times table. This is going to require a few things. First we need three variables.\n"
				+ "Set the first with name multiplyNumber to equal 12\n"
				+ "Set the second with name loopValue to equal 0\n" + "Set the third with name result to equal 0\n"
				+ "What we need to do is loop 12 times, calcuating the next number in the table and printing it at each loop stage. We can do this by adding a loop that\n"
				+ "will continue while loopValue is LESS THAN 12, do this now. Connecting to the loop we want an assignment that ADDS to loopValue by 1 on each loop.\n"
				+ "Next in the loop we want an assignment that SETS result = loopValue, and then another that SET result * multiplyNumber. Finnaly the last thing in the loop\n"
				+ "should be a print that prints out the result at each stage. Give it a try!\n\n"
				+ "Try and print out other times tables, and do other interesting programs! Play around!");

	}

	/**
	 * Called when the user clicks on the having fun button.
	 */
	@FXML
	private void handleHaveFun() {

		//set the tutorials text.
		setTutorialTextArea(
				"Now we have learned the basics, all thats left is to have fun and create whatever we want. Come up with some interesting\n"
						+ "programs so show your parents.");

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
