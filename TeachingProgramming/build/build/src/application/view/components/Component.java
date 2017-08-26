package application.view.components;

/*
 *A standardized definition of a viewable component
 */
public interface Component {

	//type of connections a component can recieve(sockets)
	enum SocketType {
		BASIC, DATA, EVENT
	}

	//type of connections a component gives out(ports)
	enum PortType {
		BASIC, DATA, EVENT
	}

	//essential methods.
	public void setSockets();

	public void setPorts();

	public int getSocketNumber();

	public int getPortNumber();

	public SocketType getSocketType();

	public PortType getPortType();

	public void registerPortLink(Component c);

	public void registerSocketLink(Component c);

	public void removePortLink(int portNumber);

	public void removeSocketLink(int portNumber);

	public void executeComponentFunction();

}
