package greedy.mapping;

import greedy.mapping.mts.MTCapability;

import org.eclipse.winery.model.tosca.TCapabilityDefinition;

/**
 * This class models the mapping between a required capability and an available capability.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class CapabilityMapping {

	/**
	 * Required capability (definition).
	 */
	private TCapabilityDefinition nodeCap;
	/**
	 * Available capability.
	 */
	private MTCapability servCap;

	/**
	 * Constructor.
	 * @param nodeCap Required capability (definition).
	 * @param servCap Available capability.
	 */
	public CapabilityMapping(TCapabilityDefinition nodeCap, MTCapability servCap) {
		super();
		this.nodeCap = nodeCap;
		this.servCap = servCap;
	}

	/**
	 * This method allows to read the required capability (definition).
	 * @return Required capability (definition).
	 */
	public TCapabilityDefinition getNodeCap() {
		return nodeCap;
	}

	/**
	 * This method allows to modify the required capability (definition).
	 * @param nodeCap Required capability (definition).
	 */
	public void setNodeCap(TCapabilityDefinition nodeCap) {
		this.nodeCap = nodeCap;
	}

	/**
	 * This method allows to read the available capability.
	 * @return Available capability.
	 */
	public MTCapability getServCap() {
		return servCap;
	}

	/**
	 * This method allows to modify the available capability.
	 * @param servCap Available capability.
	 */
	public void setServCap(MTCapability servCap) {
		this.servCap = servCap;
	}

}
