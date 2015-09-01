package mart.mapping;

import mart.mapping.mts.MTRequirement;

import org.eclipse.winery.model.tosca.TRequirementDefinition;

/**
 * This class models the mapping between a required requirement (definition) and an available requirement.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class RequirementMapping {

	/**
	 * Required requirement (definition).
	 */
	private TRequirementDefinition nodeReq;
	/**
	 * Available requirement.
	 */
	private MTRequirement servReq;

	/**
	 * Constructor.
	 *
	 * @param nodeReq Required requirement (definition).
	 * @param servReq Available requirement.
	 */
	public RequirementMapping(TRequirementDefinition nodeReq,
			MTRequirement servReq) {
		super();
		this.nodeReq = nodeReq;
		this.servReq = servReq;
	}

	/**
	 * This method allows to access the required requirement (definition).
	 * @return Required requirement (definition).
	 */
	public TRequirementDefinition getNodeReq() {
		return nodeReq;
	}

	/**
	 * This method allows to modify the required requirement (definition).
	 * @param nodeReq Required requirement (definition).
	 */
	public void setNodeReq(TRequirementDefinition nodeReq) {
		this.nodeReq = nodeReq;
	}

	/**
	 * This method allows to read the available requirement.
	 * @return Available requirement.
	 */
	public MTRequirement getServReq() {
		return servReq;
	}

	/**
	 * This method allows to modify the available requirement.
	 * @param servReq Available requirement.
	 */
	public void setServReq(MTRequirement servReq) {
		this.servReq = servReq;
	}

}
