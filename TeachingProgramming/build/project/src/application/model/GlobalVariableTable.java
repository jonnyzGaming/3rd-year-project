package application.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class maintains a table of all the current global variables that have been set and provides functions to access/change them.
 */
public class GlobalVariableTable {

	//global array list for the program that can be used for lookup.
	public static List<VariableType> variableList = new ArrayList<VariableType>();

	public static void addVariable(VariableType variable) {
		variableList.add(variable);
	}

	//gets the variable with the given name
	public static VariableType getVariable(String name) {
		for (VariableType v : variableList) {
			if (v.getName().equals(name)) {
				return v;
			}
		}

		//return null if no variable found with that name.
		return null;
	}

	public static void clearTable() {
		variableList.clear();
	}

}
