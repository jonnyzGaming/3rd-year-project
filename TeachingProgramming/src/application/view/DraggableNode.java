package application.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import application.model.DragContainer;
import application.view.components.Component;
import application.view.components.IfComp;
import application.view.components.Loop;
import application.view.components.Print;
import application.view.components.Sequencer;
import application.view.components.Start;
import application.view.components.VarAssign;
import application.view.components.Variable;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;

/*
 * A custom control that represents a dragable node, it has a type.
 */
public class DraggableNode extends AnchorPane {

	//own components drag handlers
	private EventHandler mContextDragOver;
	private EventHandler mContextDragDropped;

	//drag handlers for links to this component
	//for link handlers(ports of component) (drop on sockets, drag on ports)
	//MAX 2 PORTS/SOCKETS AT THE MOMENT
	private EventHandler<DragEvent> mLinkSocketDragDropped;
	private EventHandler<MouseEvent> mLinkPortDragDetected;
	private EventHandler<DragEvent> mLinkSocketDragDropped2;
	private EventHandler<MouseEvent> mLinkPortDragDetected2;

	//occur in large context surrounding nodes(eg root pane/building area
	private EventHandler<DragEvent> mContextLinkDragOver;
	private EventHandler<DragEvent> mContextLinkDragDropped;

	private ComponentType cType = null;
	private Point2D mDragOffset = new Point2D(0.0, 0.0);

	//each dragable node has a reuseable draglink object to visually create
	//the links from nodes, also maintain reference to building pane.
	private NodeLink mDragLink = null;
	private AnchorPane building_pane = null;

	//DATA ON PORTS/SOCKETS
	public int portConnectionNumber = 0; //used to track the number of the port this component is connected to.
	public boolean Port1open = true;
	public boolean Port2open = true;
	public boolean Socket1open = true, Socket2open = true;

	//Keep a list of all link ids that are connected to this node.
	private final List mLinkIds = new ArrayList();

	@FXML
	AnchorPane root_pane;
	//current 2 ports and 2 sockets
	@FXML
	AnchorPane socket1;
	@FXML
	AnchorPane port1;
	@FXML
	AnchorPane socket2;
	@FXML
	AnchorPane port2;
	@FXML
	private Label title_bar;
	@FXML
	private Label close_button;

	//main component body for a draggable node, this is set according to
	//the components type.
	@FXML
	AnchorPane node_body;

	//The component for this draggable node that fills the body.
	Component comp;

	private final DraggableNode self;

	public DraggableNode(ComponentType type) {
		self = this;
		cType = type;

		//set id that uniquely identifies this node, lets the id tell
		//us the type of component it is to help out in introspection.
		//setNodeId();
		setId(UUID.randomUUID().toString());

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/DraggableNode.fxml"));

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

		setType(cType);

		buildNodeDragHandlers();
		buildLinkDragHandlers(comp.getPortNumber(), comp.getSocketNumber());

		//DETECT DRAG ONLY ON PORTS, DROP ONLY ONTO SOCKETS.
		if (comp.getPortNumber() >= 1) port1.setOnDragDetected(mLinkPortDragDetected);
		if (comp.getPortNumber() >= 2) port2.setOnDragDetected(mLinkPortDragDetected2);

		if (comp.getSocketNumber() >= 1) socket1.setOnDragDropped(mLinkSocketDragDropped);
		if (comp.getSocketNumber() >= 2) socket2.setOnDragDropped(mLinkSocketDragDropped2);

		mDragLink = new NodeLink();
		mDragLink.setVisible(false);

		//This is just a neat trick to ensure right pane doesnt get set to null
		//pointer(javafx is wierd, this ensures we get right pane reference
		//AFTER its been created.
		parentProperty().addListener((observable, oldValue, newValue) -> {
			building_pane = (AnchorPane) getParent();
		});

	}

	/**
	 * Drag handlers that handle this component moving around in the building
	 * area, when moving the title bar.
	 */
	public void buildNodeDragHandlers() {

		//drag detection for node dragging
		title_bar.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				getParent().setOnDragOver(mContextDragOver);
				getParent().setOnDragDropped(mContextDragDropped);

				//begin drag operation
				mDragOffset = new Point2D(event.getX(), event.getY());

				relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

				ClipboardContent content = new ClipboardContent();
				DragContainer container = new DragContainer();

				container.addData("type", cType.toString());
				content.put(DragContainer.DragNode, container);

				startDragAndDrop(TransferMode.ANY).setContent(content);

				event.consume();
			}

		});

		mContextDragOver = new EventHandler<DragEvent>() {

			//dragover to handle node dragging in the building pane view
			@Override
			public void handle(DragEvent event) {

				event.acceptTransferModes(TransferMode.ANY);
				relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

				event.consume();
			}
		};

		//dragdrop for node dragging
		mContextDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				event.setDropCompleted(true);

				event.consume();
			}
		};

		//close button click
		close_button.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				AnchorPane parent = (AnchorPane) self.getParent();
				parent.getChildren().remove(self);

				//iterate each link id connected to this node
				//find it's corresponding component in the building
				//AnchorPane and delete it.(This is just the nodelink object removal)
				for (ListIterator iterId = mLinkIds.listIterator(); iterId.hasNext();) {

					String id = (String) iterId.next();

					//loop all nodes in building pane
					for (ListIterator iterNode = parent.getChildren().listIterator(); iterNode.hasNext();) {

						Node node = (Node) iterNode.next();

						if (node.getId() == null) continue;

						if (node.getId().equals(id)) iterNode.remove();
					}
					iterId.remove();
				}

				//remove the link from the actually components it was connected to.
				if (!Port1open) freePortLink(1, true);
				if (!Port2open) freePortLink(2, true);
				if (!Socket1open) freeSocketLink(1, true);
				if (!Socket2open) freeSocketLink(2, true);

			}
		});

	}

	/**
	 * Code for link draghandlers connecting to this component
	 */
	private void buildLinkDragHandlers(int portNum, int socketNum) {

		if (portNum >= 1) {
			mLinkPortDragDetected = new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {

					getParent().setOnDragOver(null);
					getParent().setOnDragDropped(null);

					getParent().setOnDragOver(mContextLinkDragOver);
					getParent().setOnDragDropped(mContextLinkDragDropped);

					if (Port1open) {

						//SET UP USER DRAGABLE LINK
						//first add to buildingpane and set invisible.
						building_pane.getChildren().add(0, mDragLink);
						mDragLink.setVisible(false);

						//next get the origin of where to start cubic curve in buildingpane
						double widthOffsetLocal = (getWidth() / 4.0);
						double lengthOffsetLocal = getHeight();
						mDragOffset = new Point2D(getLayoutX() + widthOffsetLocal, getLayoutY() + lengthOffsetLocal);
						//Point2D p = new Point2D(getLayoutX() + (getWidth() / 2.0), getLayoutY() + (getHeight() / 2.0)); //CENTER
						mDragLink.setStart(mDragOffset);

						//Drag the sources content(put in container)
						ClipboardContent content = new ClipboardContent();
						DragContainer container = new DragContainer();

						container.addData("source", getId());
						container.addData("pointSourceX", widthOffsetLocal);
						container.addData("pointSourceY", lengthOffsetLocal);
						container.addData("sourcePortNumber", 1);

						content.put(DragContainer.AddLink, container);
						startDragAndDrop(TransferMode.ANY).setContent(content);

						event.consume();
					}
				}
			};
		}

		if (portNum >= 2) {
			mLinkPortDragDetected2 = new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {

					getParent().setOnDragOver(null);
					getParent().setOnDragDropped(null);

					getParent().setOnDragOver(mContextLinkDragOver);
					getParent().setOnDragDropped(mContextLinkDragDropped);

					if (Port2open) {

						//SET UP USER DRAGABLE LINK
						//first add to buildingpane and set invisible.
						building_pane.getChildren().add(0, mDragLink);
						mDragLink.setVisible(false);

						//next get the origin of where to start cubic curve in buildingpane
						//THESE ARE CHANGED FOR SECOND PORT
						double widthOffsetLocal = (getWidth() / 1.25);
						double lengthOffsetLocal = getHeight();
						mDragOffset = new Point2D(getLayoutX() + widthOffsetLocal, getLayoutY() + lengthOffsetLocal);
						mDragLink.setStart(mDragOffset);

						//Drag the sources content(put in container)
						ClipboardContent content = new ClipboardContent();
						DragContainer container = new DragContainer();

						container.addData("source", getId());
						container.addData("pointSourceX", widthOffsetLocal);
						container.addData("pointSourceY", lengthOffsetLocal);
						container.addData("sourcePortNumber", 2);

						content.put(DragContainer.AddLink, container);
						startDragAndDrop(TransferMode.ANY).setContent(content);

						event.consume();
					}
				}
			};
		}

		if (socketNum >= 1) {
			mLinkSocketDragDropped = new EventHandler<DragEvent>() {

				@Override
				public void handle(DragEvent event) {

					getParent().setOnDragOver(null);
					getParent().setOnDragDropped(null);

					//hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
					mDragLink.setVisible(false);
					building_pane.getChildren().remove(0);

					if (Socket1open) {

						//get the drag data, null means its not a target link so abort
						//aka we need to have connected to a source before we can drop.
						DragContainer container = (DragContainer) event.getDragboard().getContent(
								DragContainer.AddLink);

						if (container == null) return;

						//retrieve the parent dragnode of the target the link was dropped on, location to drop on.
						AnchorPane link_handle = (AnchorPane) event.getSource();
						DraggableNode parent = (DraggableNode) link_handle.getParent().getParent().getParent()
								.getParent();
						double widthOffsetLocal = parent.getWidth() / 3.0;
						double lengthOffsetLocal = 0;

						ClipboardContent content = new ClipboardContent();

						//put the target nodes ID in container and location of drop in target node for curve to link to.
						container.addData("target", getId());
						container.addData("pointTargetX", widthOffsetLocal);
						container.addData("pointTargetY", lengthOffsetLocal);
						content.put(DragContainer.AddLink, container);
						event.getDragboard().setContent(content);
						event.setDropCompleted(true);

						event.consume();
					}
				}
			};
		}

		if (socketNum >= 2) {
			mLinkSocketDragDropped2 = new EventHandler<DragEvent>() {

				@Override
				public void handle(DragEvent event) {

					getParent().setOnDragOver(null);
					getParent().setOnDragDropped(null);

					//hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
					mDragLink.setVisible(false);
					building_pane.getChildren().remove(0);

					if (Socket2open) {

						//get the drag data, null means its not a target link so aboart
						//aka we need to have connected to a source before we can drop.
						DragContainer container = (DragContainer) event.getDragboard().getContent(
								DragContainer.AddLink);

						if (container == null) return;

						//retrieve the parent dragnode of the target the link was dropped on, location to drop on.
						AnchorPane link_handle = (AnchorPane) event.getSource();
						DraggableNode parent = (DraggableNode) link_handle.getParent().getParent().getParent()
								.getParent();

						double widthOffsetLocal = parent.getWidth() * 0.75;
						double lengthOffsetLocal = 0;

						ClipboardContent content = new ClipboardContent();

						//put the target nodes ID in container and location of drop in target node for curve to link to.
						container.addData("target", getId());
						container.addData("pointTargetX", widthOffsetLocal);
						container.addData("pointTargetY", lengthOffsetLocal);
						content.put(DragContainer.AddLink, container);
						event.getDragboard().setContent(content);
						event.setDropCompleted(true);

						event.consume();
					}
				}
			};
		}

		mContextLinkDragOver = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				event.acceptTransferModes(TransferMode.ANY);

				//Relocate user draggable link, as user moves it.
				if (!mDragLink.isVisible()) mDragLink.setVisible(true);

				mDragLink.setEnd(new Point2D(event.getX(), event.getY()));

				event.consume();

			}
		};

		mContextLinkDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {

				getParent().setOnDragOver(null);
				getParent().setOnDragDropped(null);

				//hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
				mDragLink.setVisible(false);
				building_pane.getChildren().remove(0);

				event.setDropCompleted(true);
				event.consume();
			}

		};
	}

	//register a visual Nodelink ID with this node(so we can add/remove easy/efficent).
	public void registerLink(String linkId) {
		mLinkIds.add(linkId);
	}

	//updates this nodes link status, registers the fact a connection is now held from this node to a specified target,
	//this needs to be updated inside the individual components so they can handle connections.
	public void registerPortLink(DraggableNode target, int sourcePortNumber) {

		if (Port1open && sourcePortNumber == 1) {
			comp.registerPortLink(target.comp, 1);
			Port1open = false;

		} else if (Port2open && sourcePortNumber == 2) {
			comp.registerPortLink(target.comp, 2);
			Port2open = false;
		}
	}

	//updates this nodes link status, registers the fact a connection is now held thats TAKING ONE OF THIS NODES SOCKETS.
	public void registerSocketLink(DraggableNode source, int sourcePortNumber) {

		this.portConnectionNumber = sourcePortNumber;
		if (Socket1open) {
			comp.registerSocketLink(source.comp);
			Socket1open = false;

		} else if (Socket2open) {
			comp.registerSocketLink(source.comp);
			Socket2open = false;
		}

	}

	public void freePortLink(int portNum, boolean fromSource) {
		if (portNum == 1) {
			Port1open = true;
			comp.removePortLink(1, fromSource);
		}
		if (portNum == 2) {
			Port2open = true;
			comp.removePortLink(2, fromSource);
		}
	}

	public void freeSocketLink(int socketNum, boolean fromSource) {
		if (socketNum == 1) {
			Socket1open = true;
			comp.removeSocketLink(portConnectionNumber, fromSource);
		}
		if (socketNum == 2) {
			Socket2open = true;
			comp.removeSocketLink(portConnectionNumber, fromSource);
		}
	}

	public ComponentType getType() {
		return cType;
	}

	/**
	 * Sets the type of this component, sets the desired number of source/target ports
	 * and custom control needed for this particlar component.
	 */
	public void setType(ComponentType type) {

		switch (cType) {
			case start:
				getStyleClass().add("icon-brightgreen");
				title_bar.setText("Start");
				comp = new Start();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((Start) comp);

				break;
			case print:
				getStyleClass().add("icon-blue");
				title_bar.setText("Print");
				comp = new Print();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((Print) comp);

				break;
			case sequencer:
				getStyleClass().add("icon-green");

				title_bar.setText("Sequencer");
				comp = new Sequencer();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((Sequencer) comp);
				break;

			case variable:
				getStyleClass().add("icon-gold");
				title_bar.setText("variable");
				comp = new Variable();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((Variable) comp);
				break;

			case ifcomp:
				getStyleClass().add("icon-grey");
				title_bar.setText("If statement");
				comp = new IfComp();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((IfComp) comp);
				break;

			case loop:
				getStyleClass().add("icon-brown");
				title_bar.setText("Loop");
				comp = new Loop();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((Loop) comp);
				break;
			case assignment:
				getStyleClass().add("icon-purple");
				title_bar.setText("Assignmentt");
				comp = new VarAssign();
				comp.setSourceDraggableNode(this);
				node_body.getChildren().add((VarAssign) comp);
				break;

			default:
				break;
		}
	}

	//for use in links
	public AnchorPane getNodeBody() {
		return node_body;
	}

	public void relocateToPoint(Point2D p) {

		//relocates the object to a point that has been converted to
		//scene coordinates
		Point2D localCoords = getParent().sceneToLocal(p);

		relocate((int) (localCoords.getX() - mDragOffset.getX()), (int) (localCoords.getY() - mDragOffset.getY()));
	}
}
