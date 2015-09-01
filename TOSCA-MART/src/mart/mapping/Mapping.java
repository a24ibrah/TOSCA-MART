package mart.mapping;

import java.util.List;

/**
 * This class models a possible mapping between desired and available capabilities, requirements, properties and interface operations.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class Mapping {

	/**
	 * This field contains the mapping between required and available capabilities.
	 */
	private List<CapabilityMapping> capsMap;
	/**
	 * This field contains the mapping between required and available requirements.
	 */
	private List<RequirementMapping> reqsMap;
	/**
	 * This field contains the mapping between required and available properties.
	 */
	private List<PropertyMapping> propsMap;
	/**
	 * This field contains the mapping between required and available interface operations.
	 */
	private List<OperationMapping> opsMap;

	/**
	 * Constructor.
	 *
	 * @param capsMap List containing the mapping between required and available capabilities.
	 * @param reqsMap List containing the mapping between required and available requirements.
	 * @param propsMap List containing the mapping between required and available properties.
	 * @param opsMap List containing the mapping between required and available interface operations.
	 */
	public Mapping(List<CapabilityMapping> capsMap,
			List<RequirementMapping> reqsMap,
			List<PropertyMapping> propsMap,
			List<OperationMapping> opsMap) {
		super();
		this.capsMap = capsMap;
		this.reqsMap = reqsMap;
		this.propsMap = propsMap;
		this.opsMap = opsMap;
	}

	/**
	 * This method allows to access the List containing the mapping between required and available capabilities.
	 * @return List containing the mapping between required and available capabilities.
	 */
	public List<CapabilityMapping> getCapsMap() {
		return capsMap;
	}

	/**
	 * This method allows to access the List containing the mapping between required and available requirements.
	 * @return List containing the mapping between required and available requirements.
	 */
	public List<RequirementMapping> getReqsMap() {
		return reqsMap;
	}

	/**
	 * This method allows to access the List containing the mapping between required and available properties.
	 * @return List containing the mapping between required and available properties.
	 */
	public List<PropertyMapping> getPropsMap() {
		return propsMap;
	}

	/**
	 * This method allows to access the List containing the mapping between required and available interface operations.
	 * @return List containing the mapping between required and available interface operations.
	 */
	public List<OperationMapping> getOpsMap() {
		return opsMap;
	}

	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("Mapping:\n\tCapabilities: ");
		for(CapabilityMapping cm : capsMap) {
			result.append("[" + cm.getNodeCap().getName() + "->" + cm.getServCap().getCapability().getName() + "@" + cm.getServCap().getNodeTemplate().getId() + "]");
		}
		result.append("\n\tRequirements: ");
		for(RequirementMapping rm : reqsMap) {
			result.append("[" + rm.getNodeReq().getName() + "->" + rm.getServReq().getRequirement().getName() + "@" + rm.getServReq().getNodeTemplate().getId() + "]");
		}
		result.append("\n\tProperties: ");
		for(PropertyMapping pm : propsMap) {
			String pName = pm.getNodeProp().getElementsByTagName("winery:key").item(0).getTextContent();
			String element = pm.getServProp().getEntityTemplate().getId();
			result.append("[" + pName + "->" + pName + "@" + element + "]");
		}
		result.append("\n\tOperations: ");
		for(OperationMapping om : opsMap) {
			result.append("[" + om.getNodeOp().getName() + "->" + om.getServOp().getOperation().getName() + "@" + om.getServOp().getEntityTemplate().getId() + "]");
		}
		result.append("\n");
		return result.toString();
	}
}
