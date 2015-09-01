package mart.mapping;

import mart.mapping.mts.MTOperation;

import org.eclipse.winery.model.tosca.TOperation;

/**
 * This class models the mapping between a required interface operation and an available one.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class OperationMapping {

	/**
	 * Required interface operation.
	 */
	private TOperation nodeOp;
	/**
	 * Available interface operation.
	 */
	private MTOperation servOp;

	/**
	 * Constructor.
	 * @param nodeOp Required interface operation.
	 * @param servOp Available interface operation.
	 */
	public OperationMapping(TOperation nodeOp, MTOperation servOp) {
		super();
		this.nodeOp = nodeOp;
		this.servOp = servOp;
	}

	/**
	 * This method allows to read the required interface operation.
	 * @return Required interface operation.
	 */
	public TOperation getNodeOp() {
		return nodeOp;
	}

	/**
	 * This method allows to modify the required interface operation.
	 * @param nodeOp Required interface operation.
	 */
	public void setNodeOp(TOperation nodeOp) {
		this.nodeOp = nodeOp;
	}

	/**
	 * This method allows to read the available interface operation.
	 * @return Available interface operation.
	 */
	public MTOperation getServOp() {
		return servOp;
	}

	/**
	 * This method allows to modify the available interface operation.
	 * @param servOp Available interface operation.
	 */
	public void setServOp(MTOperation servOp) {
		this.servOp = servOp;
	}

}
