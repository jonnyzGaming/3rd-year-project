package application.model;

//just a basic variable type with a name and value;
public class VariableType {

	public String type;
	public String varName;
	public int valueInt;
	public String valueString;
	public Boolean valueBool;

	public VariableType(String type, String varName, String value) {

		valueString = null;
		valueBool = null;

		//set value according to type.
		if (type.equals("Int")) valueInt = Integer.parseInt(value);
		if (type.equals("String")) valueString = value;
		if (type.equals("Boolean")) valueBool = Boolean.parseBoolean(value);

		this.varName = varName;
		this.type = type;

	}

	public String getType() {
		return type;
	}

	public String getName() {
		return varName;
	}

	public boolean hasValue(String value) {
		if (getType().equals("Int")) {

			try {
				if (getIntValue() == Integer.parseInt(value)) return true;
			} catch (NumberFormatException e) {
				return false;
			}

		}

		if (getType().equals("String")) {
			if (getStringValue().equals(value)) return true;
		}

		if (getType().equals("Boolean")) {
			try {
				if (getBooleanValue() == Boolean.parseBoolean(value)) return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return false;
	}

	//attempts to set the value of this variable, returns true if it was succesfully set false if value can not be set to this type.
	public boolean setValue(String value) {
		if (getType().equals("Int")) {
			try {
				int tempInt = Integer.parseInt(value);
				valueInt = tempInt;
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		if (getType().equals("String")) {
			valueString = value;
			return true;
		}

		if (getType().equals("Boolean")) {
			try {
				valueBool = Boolean.parseBoolean(value);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return false;

	}

	public int getIntValue() {
		return valueInt;
	}

	public String getStringValue() {
		return valueString;
	}

	public boolean getBooleanValue() {
		return valueBool;
	}

}
