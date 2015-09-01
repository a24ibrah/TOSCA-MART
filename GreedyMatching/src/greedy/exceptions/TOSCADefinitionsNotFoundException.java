package greedy.exceptions;

/**
 * This class models the Exception to be raised when it is not possible to retrieve any TOSCA Definition element at a certain location.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class TOSCADefinitionsNotFoundException extends Exception {

	/**
	 * Default serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TOSCADefinitionsNotFoundException() {
		super();
	}

	/**
	 * Constructor.
	 * @param why Message to be assigned to the raised exception (so as to specify why the exception has been raised).
	 */
	public TOSCADefinitionsNotFoundException(String why) {
		super(why + " cannot be resolved to a TOSCA TDefinitions element.");
	}
}
