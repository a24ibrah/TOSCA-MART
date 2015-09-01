package mart.mapping.mts;

import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TNodeTemplate;

/**
 * This (utility) class allows to rapidly gain the TNodeTemplate corresponding to the analyzed TCapability.
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class MTCapability {

	/**
	 * (To-Be-)Analyzed TCapability.
	 */
	private TCapability capability;
	/**
	 * Corresponding TNodeTemplate.
	 */
	private TNodeTemplate nodeTemplate;

	/**
	 * Constructor.
	 *
	 * @param capability (To-Be-)Analyzed TCapability.
	 * @param nodeTemplate Corresponding TNodeTemplate.
	 */
	public MTCapability(TCapability capability, TNodeTemplate nodeTemplate) {
		super();
		this.capability = capability;
		this.nodeTemplate = nodeTemplate;
	}

	/**
	 * This method allows to retrieve the (to-be-)analyzed TCapability.
	 * @return (To-Be-)Analyzed TCapability.
	 */
	public TCapability getCapability() {
		return capability;
	}

	/**
	 * This method allows to modify the (to-be-)analyzed TCapability.
	 * @param capability (To-Be-)Analyzed TCapability.
	 */
	public void setCapability(TCapability capability) {
		this.capability = capability;
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
