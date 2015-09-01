package mart.mapping.mts;

import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.model.tosca.TRequirement;

/**
 * This (utility) class allows to rapidly gain the TNodeTemplate corresponding to the analyzed TRequirement.
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class MTRequirement {

	/**
	 * (To-Be-)Analyzed TRequirement.
	 */
	private TRequirement requirement;
	/**
	 * Corresponding TNodeTemplate.
	 */
	private TNodeTemplate nodeTemplate;

	/**
	 * Constructor.
	 *
	 * @param requirement (To-Be-)Analyzed TRequirement.
	 * @param nodeTemplate Corresponding TNodeTemplate.
	 */
	public MTRequirement(TRequirement requirement, TNodeTemplate nodeTemplate) {
		super();
		this.requirement = requirement;
		this.nodeTemplate = nodeTemplate;
	}

	/**
	 * This method allows to retrieve the (to-be-)analyzed TRequirement.
	 * @return (To-Be-)Analyzed TRequirement.
	 */
	public TRequirement getRequirement() {
		return requirement;
	}

	/**
	 * This method allows to modify the (to-be-)analyzed TRequirement.
	 * @param requirement (To-Be-)Analyzed TRequirement.
	 */
	public void setRequirement(TRequirement requirement) {
		this.requirement = requirement;
	}

	/**
	 * This method allows to retrieve the corresponding TNodeTemplate.
	 * @return Corresponding TNodeTemplate.
	 */
	public TNodeTemplate getNodeTemplate() {
		return nodeTemplate;
	}

	/**
	 * This method allows to modify the corresponding TNodeTemplate.
	 * @param nodeTemplate Corresponding TNodeTemplate.
	 */
	public void setNodeTemplate(TNodeTemplate nodeTemplate) {
		this.nodeTemplate = nodeTemplate;
	}



}
