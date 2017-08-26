package application.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.DataFormat;
import javafx.util.Pair;

/**
 * Custom drag and drop container that acts as data model
 */
public class DragContainer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1458406119115196098L;

	private final List<Pair<String, Object>> mDataPairs = new ArrayList<Pair<String, Object>>();

	public static final DataFormat AddNode = new DataFormat("application.DragIcon.add");

	public static final DataFormat DragNode = new DataFormat("application.DraggableNode.drag");

	public static final DataFormat AddLink = new DataFormat("application.NodeLink.add");

	public DragContainer() {
	}

	public void addData(String key, Object value) {
		mDataPairs.add(new Pair<String, Object>(key, value));
	}

	public <T> T getValue(String key) {

		for (Pair<String, Object> data : mDataPairs) {

			if (data.getKey().equals(key)) return (T) data.getValue();

		}

		return null;
	}

	public List<Pair<String, Object>> getData() {
		return mDataPairs;
	}
}