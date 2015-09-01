package greedy.exceptions;

/**
 * This class models the Exception to be raised when it is not possible to retrieve the expected kind of definitions (e.g., ServiceTemplate, NodeType, etc.) at a certain location.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class TOSCADefinitionsNotValidException extends Exception {

	/**
	 * Default serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TOSCADefinitionsNotValidException() {
		super();
	}

	/**
	* Constructor.
	 * @param why Message to be assigned to the raised exception (so as to specify why the exception has been raised).
	 */
	public TOSCADefinitionsNotValidException(String why) {
		super(why);
	}
}

