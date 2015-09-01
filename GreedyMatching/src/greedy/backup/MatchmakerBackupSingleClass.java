package greedy.backup;

import greedy.candidate.Candidate;
import greedy.exceptions.TOSCADefinitionsNotFoundException;
import greedy.exceptions.TOSCADefinitionsNotValidException;
import greedy.mapping.CapabilityMapping;
import greedy.mapping.OperationMapping;
import greedy.mapping.PropertyMapping;
import greedy.mapping.RequirementMapping;
import greedy.mapping.mts.MTCapability;
import greedy.mapping.mts.MTOperation;
import greedy.mapping.mts.MTProperty;
import greedy.mapping.mts.MTRequirement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TCapabilityDefinition;
import org.eclipse.winery.model.tosca.TCapabilityType;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TInterface;
import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TOperation;
import org.eclipse.winery.model.tosca.TParameter;
import org.eclipse.winery.model.tosca.TRelationshipTemplate;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.model.tosca.TRequirement;
import org.eclipse.winery.model.tosca.TRequirementDefinition;
import org.eclipse.winery.model.tosca.TRequirementType;
import org.eclipse.winery.model.tosca.TServiceTemplate;
import org.eclipse.winery.model.tosca.TTopologyTemplate;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tosca.util.TOSCAUtils;

public class MatchmakerBackupSingleClass {

	/**
	 * Element "Definitions" containing the NodeType under consideration.
	 */
	private TDefinitions nodeTypeDefs;
	/**
	 * NodeType under consideration.
	 */
	private TNodeType nodeType;
	/**
	 * Element "Definitions" containing the ServiceTemplate under consideration.
	 */
	private TDefinitions servTempDefs;
	/**
	 * ServiceTemplate under consideration.
	 */
	private TServiceTemplate servTemp;

	/**
	 * Constructor.
	 *
	 * @param nodeTypeLoc URL from which to retrieve the "TDefinitions" of the target NodeType.
	 * @param servTempLoc URL from which to retrieve the "TDefinitions" of the available ServiceTemplate.
	 * @throws TOSCADefinitionsNotFoundException Exception illustrating the unavailability of a "TDefinitions" element at the specified location.
	 * @throws TOSCADefinitionsNotValidException Exception illustrating the impossibility to retrieve the desired TOSCA element from the specified location.
	 */
	public MatchmakerBackupSingleClass(String nodeTypeLoc, String servTempLoc) throws TOSCADefinitionsNotFoundException, TOSCADefinitionsNotValidException {
		//Parsing of the NodeType's TDefinitions
		nodeTypeDefs = TOSCAUtils.parseDefinitions(nodeTypeLoc);
		if(nodeTypeDefs == null)
			throw new TOSCADefinitionsNotFoundException(nodeTypeLoc);
		TExtensibleElements element = nodeTypeDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		if(!(element instanceof TNodeType))
			throw new TOSCADefinitionsNotValidException(nodeTypeLoc + " is not defining a TOSCA NodeType.");
		nodeType = (TNodeType) element;

		//Parsing of the ServiceTemplate's TDefinitions
		servTempDefs = TOSCAUtils.parseDefinitions(servTempLoc);
		if(servTempDefs == null)
			throw new TOSCADefinitionsNotFoundException(servTempLoc);
		element = servTempDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		if(!(element instanceof TServiceTemplate))
			throw new TOSCADefinitionsNotValidException(servTempLoc + " is not defining a TOSCA ServiceTemplate.");
		servTemp = (TServiceTemplate) element;
	}

	/**
	 * This method performs the "plug-in matching" check between the Capability "c" and the CapabilityDefinition "cd"
	 *
	 * @param c Capability to be matched.
	 * @param cd CapabilityDefinition to be matched.
	 *
	 * @return "true", if "c" matches "cd"; "false", otherwise.
	 */
	private boolean match(TCapability c, TCapabilityDefinition cd) {
		//Checks whether the names are equal
		if(!c.getName().equals(cd.getName()))
			return false;
		//Checks whether the type of "cd" is a sub-type of "c".
		//If so, "cd" plug-in matches "c".
		if(c.getType().equals(cd.getCapabilityType()))
			return true;
		TOSCAUtils tu = new TOSCAUtils(servTempDefs);
		TCapabilityType cType = (TCapabilityType) tu.retrieveType(c.getType());
		System.err.println(cType.getName());
		while(cType.getDerivedFrom()!=null) {
			if(cType.getDerivedFrom().getTypeRef().equals(cd.getCapabilityType()))
				return true;
			cType = (TCapabilityType) tu.retrieveType(cType.getDerivedFrom().getTypeRef());
		}
		//Otherwise, "cd" does not plug-in match "c".
		return false;
	}

	/**
	 * This method performs the matchmaking of capabilities.
	 * [It copes with requirement multiplicity by ensuring that the lowerBound(s) of the CapabilityDefinition(s) are satisfied]
	 *
	 * @param matched Set of capability mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of CapabilityDefinition to be matched.
	 * @param available Set of available Capability (in the form of "MTCapability").
	 * @param solutions Set of capability mappings detected by the previously run instances of the method (initially, "null").
	 * @return
	 */
	private Set<List<CapabilityMapping>> matchCapabilities(
			List<CapabilityMapping> matched,
			List<TCapabilityDefinition> needed,
			List<MTCapability> available,
			Set<List<CapabilityMapping>> solutions) {
		//Pre-processing
		if(matched==null)
			matched = new ArrayList<CapabilityMapping>();
		if(solutions==null)
			solutions = new HashSet<List<CapabilityMapping>>();

		//Checks whether there are no more available capabilities
		if(available.size()==0) {
			//Checks whether "matched" is a solution
			boolean isSolution = true;
			for(TCapabilityDefinition cd : needed) {
				if(cd.getLowerBound()!=0)
					isSolution=false;
			}
			//if(needed.size()==0)
			if(isSolution && !matched.isEmpty())
				solutions.add(matched);
			//Returns the computed set of "solutions"
			return solutions;
		}

		//Picks a capability "ca" from the "available" ones
		MTCapability ca = available.get(0);
		available.remove(0);

		//Checks whether "ca" is "needed"
		// (i.e., it looks whether there exists a capability definition cn
		// such that ca matches cn)
		Set<List<CapabilityMapping>> solutionsBis = null;
		for(TCapabilityDefinition cd : needed) {
			if(match(ca.getCapability(),cd) && cd.getLowerBound()>0) {
				//Duplicates the state
				List<CapabilityMapping> matchedBis = new ArrayList<CapabilityMapping>();
				matchedBis.addAll(matched);
				List<TCapabilityDefinition> neededBis = new ArrayList<TCapabilityDefinition>();
				neededBis.addAll(needed);
				List<MTCapability> availableBis = new ArrayList<MTCapability>();
				availableBis.addAll(available);
				//Updates the duplicated state
				neededBis.remove(cd);
				if(cd.getLowerBound()>1) {
					TCapabilityDefinition cdBis = new TCapabilityDefinition();
					cdBis.setCapabilityType(cd.getCapabilityType());
					cdBis.setLowerBound(cd.getLowerBound()-1);
					cdBis.setName(cd.getName());
					neededBis.add(cdBis);
				}
				matchedBis.add(new CapabilityMapping(cd,ca));

				//Computes the solutions including "ca"
				solutionsBis = matchCapabilities(matchedBis,neededBis,availableBis,null);
			}
		}
		if(solutionsBis!=null)
			solutions.addAll(solutionsBis);

		//Recurs on the updated state
		return matchCapabilities(matched,needed,available,solutions);
	}

	/**
	 * This method performs the "plug-in matching" check between the Requirement "c" and the RequirementDefinition "cd"
	 *
	 * @param r Requirement to be matched.
	 * @param rd RequirementDefinition to be matched.
	 *
	 * @return "true", if "r" matches "rd"; "false", otherwise.
	 */
	private boolean match(TRequirement r, TRequirementDefinition rd) {
		//Checks whether the names are equal
		if(!r.getName().equals(rd.getName()))
			return false;
		//Checks whether the type of "rd" is a sub-type of "r".
		//If so, "rd" plug-in matches "r".
		if(r.getType().equals(rd.getRequirementType()))
			return true;
		TOSCAUtils tu = new TOSCAUtils(servTempDefs);
		TRequirementType rType = (TRequirementType) tu.retrieveType(r.getType());
		System.err.println(rType.getName());
		while(rType.getDerivedFrom()!=null) {
			if(rType.getDerivedFrom().getTypeRef().equals(rd.getRequirementType()))
				return true;
			rType = (TRequirementType) tu.retrieveType(rType.getDerivedFrom().getTypeRef());
		}
		//Otherwise, "rd" does not plug-in match "r".
		return false;
	}

	/**
	 * This method performs the matchmaking of requirements.
	 * [It copes with requirement multiplicity by ensuring that the lowerBound(s) of the RequirementDefinition(s) are satisfied]
	 *
	 * @param matched Set of requirement mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of RequirementDefinition to be matched.
	 * @param available Set of available Requirement (in the form of "MTRequirement").
	 * @param solutions Set of requirement mappings detected by the previously run instances of the method (initially, "null").
	 * @return
	 */
	private Set<List<RequirementMapping>> matchRequirements(
			List<RequirementMapping> matched,
			List<TRequirementDefinition> needed,
			List<MTRequirement> available,
			Set<List<RequirementMapping>> solutions) {
		//Pre-processing
		if(matched==null)
			matched = new ArrayList<RequirementMapping>();
		if(solutions==null)
			solutions = new HashSet<List<RequirementMapping>>();

		//Checks whether there are no more available capabilities
		if(available.size()==0) {
			//Checks whether "matched" is a solution
			boolean isSolution = true;
			for(TRequirementDefinition rd : needed) {
				if(rd.getLowerBound()!=0)
					isSolution=false;
			}
			//if(needed.size()==0)
			if(isSolution && !matched.isEmpty())
				solutions.add(matched);
			//Returns the computed set of "solutions"
			return solutions;
		}

		//Picks a requirement "ra" from the "available" ones
		MTRequirement ra = available.get(0);
		available.remove(0);

		//Checks whether "ra" is "needed"
		// (i.e., it looks whether there exists a requirement definition rn
		// such that ra matches rn)
		Set<List<RequirementMapping>> solutionsBis = null;
		for(TRequirementDefinition rd : needed) {
			if(match(ra.getRequirement(),rd)) {
				//Duplicates the state
				List<RequirementMapping> matchedBis = new ArrayList<RequirementMapping>();
				matchedBis.addAll(matched);
				List<TRequirementDefinition> neededBis = new ArrayList<TRequirementDefinition>();
				neededBis.addAll(needed);
				List<MTRequirement> availableBis = new ArrayList<MTRequirement>();
				availableBis.addAll(available);
				//Updates the duplicated state
				neededBis.remove(rd);
				if(rd.getLowerBound()>1) {
					TRequirementDefinition rdBis = new TRequirementDefinition();
					rdBis.setRequirementType(rd.getRequirementType());
					rdBis.setLowerBound(rd.getLowerBound()-1);
					rdBis.setName(rd.getName());
					neededBis.add(rdBis);
				}
				matchedBis.add(new RequirementMapping(rd,ra));

				//Computes the solutions including "ra"
				solutionsBis = matchRequirements(matchedBis,neededBis,availableBis,null);
			}
		}
		if(solutionsBis!=null)
			solutions.addAll(solutionsBis);

		//Recurs on the updated state
		return matchRequirements(matched,needed,available,solutions);
	}

	/**
	 * This method performs the "exact matching" check between the Property "p1" and the Property "p2".
	 *
	 * @param p1 Property to be matched.
	 * @param p2 Property to be matched.
	 *
	 * @return "true", if "p1" matches "p2"; "false", otherwise.
	 */
	private boolean match(Element p1, Element p2) {
		//Retrieves name and type of "p1"
		NodeList key1Child = p1.getElementsByTagName("winery:key");
		String p1Name = key1Child.item(0).getTextContent();
		NodeList type1Child = p1.getElementsByTagName("winery:type");
		String p1Type = type1Child.item(0).getTextContent();
		//Retrieves name and type of "p2"
		NodeList key2Child = p2.getElementsByTagName("winery:key");
		String p2Name = key2Child.item(0).getTextContent();
		NodeList type2Child = p2.getElementsByTagName("winery:type");
		String p2Type = type2Child.item(0).getTextContent();
		//Checks whether "p1" matches "p2"
		return (p1Name.equals(p2Name)&&p1Type.equals(p2Type));
	}

	/**
	 * This method performs the matchmaking of operations.
	 *
	 * @param matched Set of operation mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of TOperation to be matched.
	 * @param available Set of available TOperation (in the form of "MTRequirement").
	 * @param solutions Set of operation mappings detected by the previously run instances of the method (initially, "null").
	 * @return
	 */
	private Set<List<PropertyMapping>> matchProperties(
			List<PropertyMapping> matched,
			List<Element> needed,
			List<MTProperty> available,
			Set<List<PropertyMapping>> solutions) {
		//Pre-processing
		if(matched==null)
			matched = new ArrayList<PropertyMapping>();
		if(solutions==null)
			solutions = new HashSet<List<PropertyMapping>>();

		//Checks whether there are no more available capabilities
		if(available.size()==0) {
			//Checks whether "matched" is a solution
			if(needed.size()==0 && !matched.isEmpty())
				solutions.add(matched);
			//Returns the computed set of "solutions"
			return solutions;
		}

		//Picks a requirement "ra" from the "available" ones
		MTProperty pa = available.get(0);
		available.remove(0);

		//Checks whether "ra" is "needed"
		// (i.e., it looks whether there exists a requirement definition rn
		// such that ra matches rn)
		Set<List<PropertyMapping>> solutionsBis = null;
		for(Element pn : needed) {
			boolean m = match(pa.getProperty(),pn);
			if(m) {
				//Duplicates the state
				List<PropertyMapping> matchedBis = new ArrayList<PropertyMapping>();
				matchedBis.addAll(matched);
				List<Element> neededBis = new ArrayList<Element>();
				neededBis.addAll(needed);
				List<MTProperty> availableBis = new ArrayList<MTProperty>();
				availableBis.addAll(available);
				//Updates the duplicated state
				neededBis.remove(pn);
				matchedBis.add(new PropertyMapping(pn,pa));

				//Computes the solutions including "ra"
				solutionsBis = matchProperties(matchedBis,neededBis,availableBis,null);
			}
		}
		if(solutionsBis!=null)
			solutions.addAll(solutionsBis);

		//Recurs on the updated state
		return matchProperties(matched,needed,available,solutions);
	}

	/** This method performs the "exact matching" check between the Operations "o1" and "o2"
	 *
	 * @param o1 TOperation to be matched.
	 * @param o2 TOperation to be matched.
	 *
	 * @return "true", if "o1" matches "o2"; "false", otherwise.
	 */
	private boolean match(TOperation o1, TOperation o2) {
		//Checks whether the names are equal
		if(!o1.getName().equals(o2.getName()))
			return false;
		//Checks whether the input parameters are the same
		List<TParameter> o1In = new ArrayList<TParameter>();
		if(o1.getInputParameters()!=null)
			o1In = o1.getInputParameters().getInputParameter();
		List<TParameter> o2In = new ArrayList<TParameter>();
		if(o2.getInputParameters()!=null)
			o2In = o2.getInputParameters().getInputParameter();
		if(o1In.size()!=o2In.size())
			return false;
		boolean checkIn;
		for(TParameter p1 : o1In) {
			checkIn = false;
			for(TParameter p2 : o2In) {
				if(p1.getName().equals(p2.getName()) &&
						p1.getType().equals(p2.getType()))
					checkIn = true;
			}
			if(!checkIn) {
				return false;
			}
		}
		//Checks whether the input parameters are the same
		List<TParameter> o1Out = new ArrayList<TParameter>();
		if(o1.getOutputParameters()!=null)
			o1Out = o1.getOutputParameters().getOutputParameter();
		List<TParameter> o2Out = new ArrayList<TParameter>();
		if(o2.getOutputParameters()!=null)
			o2Out = o2.getOutputParameters().getOutputParameter();
		if(o1Out.size()!=o2Out.size())
			return false;
		boolean checkOut;
		for(TParameter p1 : o1Out) {
			checkOut = false;
			for(TParameter p2 : o2Out) {
				if(p1.getName().equals(p2.getName()) &&
						p1.getType().equals(p2.getType()))
					checkOut = true;
			}
			if(!checkOut) {
				return false;
			}
		}
		return true;
	}


	/**
	 * This method performs the matchmaking of operations.
	 *
	 * @param matched Set of operation mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of TOperation to be matched.
	 * @param available Set of available TOperation (in the form of "MTRequirement").
	 * @param solutions Set of operation mappings detected by the previously run instances of the method (initially, "null").
	 * @return
	 */
	private Set<List<OperationMapping>> matchOperations(
			List<OperationMapping> matched,
			List<TOperation> needed,
			List<MTOperation> available,
			Set<List<OperationMapping>> solutions) {
		//Pre-processing
		if(matched==null)
			matched = new ArrayList<OperationMapping>();
		if(solutions==null)
			solutions = new HashSet<List<OperationMapping>>();

		//Checks whether there are no more available capabilities
		if(available.size()==0) {
			//Checks whether "matched" is a solution
			if(needed.size()==0 && !matched.isEmpty())
				solutions.add(matched);
			//Returns the computed set of "solutions"
			return solutions;
		}

		//Picks a requirement "ra" from the "available" ones
		MTOperation oa = available.get(0);
		available.remove(0);

		//Checks whether "ra" is "needed"
		// (i.e., it looks whether there exists a requirement definition rn
		// such that ra matches rn)
		Set<List<OperationMapping>> solutionsBis = null;
		for(TOperation on : needed) {
			boolean m = match(oa.getOperation(),on);
			if(m) {
				//Duplicates the state
				List<OperationMapping> matchedBis = new ArrayList<OperationMapping>();
				matchedBis.addAll(matched);
				List<TOperation> neededBis = new ArrayList<TOperation>();
				neededBis.addAll(needed);
				List<MTOperation> availableBis = new ArrayList<MTOperation>();
				availableBis.addAll(available);
				//Updates the duplicated state
				neededBis.remove(on);
				matchedBis.add(new OperationMapping(on,oa));

				//Computes the solutions including "ra"
				solutionsBis = matchOperations(matchedBis,neededBis,availableBis,null);
			}
		}
		if(solutionsBis!=null)
			solutions.addAll(solutionsBis);

		//Recurs on the updated state
		return matchOperations(matched,needed,available,solutions);
	}


	public List<Candidate> run() {
		TTopologyTemplate top = servTemp.getTopologyTemplate();

		//Isolates the lists of available features
		List<MTCapability> avaCaps = new ArrayList<MTCapability>();
		List<MTRequirement> avaReqs = new ArrayList<MTRequirement>();
		List<MTProperty> avaProps = new ArrayList<MTProperty>();
		List<MTOperation> avaOps = new ArrayList<MTOperation>();
		for(TEntityTemplate entity : top.getNodeTemplateOrRelationshipTemplate()){
			if(entity instanceof TNodeTemplate) {
				//Stores the current nodeTemp and retrieves its type
				TNodeTemplate nodeTemp = (TNodeTemplate) entity;
				TOSCAUtils tu = new TOSCAUtils(servTempDefs);
				TNodeType corrType = (TNodeType) tu.retrieveType(nodeTemp.getType());
				//Stores the capabilities of the current nodeTemp as available
				if(nodeTemp.getCapabilities()!=null) {
					for(TCapability c : nodeTemp.getCapabilities().getCapability()) {
						avaCaps.add(new MTCapability(c,nodeTemp));
					}
				}
				//Stores the requirements of the current nodeTemp as available
				if(nodeTemp.getRequirements()!=null) {
					for(TRequirement r : nodeTemp.getRequirements().getRequirement()) {
						avaReqs.add(new MTRequirement(r,nodeTemp));
					}
				}
				//Stores the properties of the current nodeTemp as available
				if(corrType.getPropertiesDefinition()!=null) {
                	Element propDef = (Element) corrType.getAny().get(0);
                	//System.out.println(propDef.getNodeName());
                	NodeList properties = propDef.getElementsByTagName("winery:properties");
                	for(int i=0; i<properties.getLength(); i++) {
                		Element property = (Element) properties.item(i);
                		avaProps.add(new MTProperty(property,nodeTemp));
                	}
				}
				//Stores the operations of the current nodeTemp as available
				if(corrType.getInterfaces()!=null) {
					for(TInterface i : corrType.getInterfaces().getInterface()) {
						if(i.getOperation()!=null) {
							for(TOperation o: i.getOperation()) {
								avaOps.add(new MTOperation(o,i,nodeTemp));
							}
						}
					}
				}
			}
			if(entity instanceof TRelationshipTemplate) {
				//Stores the current relTemp and retrieves its type
				TRelationshipTemplate relTemp = (TRelationshipTemplate) entity;
				TOSCAUtils tu = new TOSCAUtils(servTempDefs);
				TRelationshipType corrType = (TRelationshipType) tu.retrieveType(relTemp.getType());
				//Stores the properties of the current nodeTemp as available
				if(corrType.getPropertiesDefinition()!=null) {
					Element propDef = (Element) corrType.getAny().get(0);
					NodeList properties = propDef.getElementsByTagName("winery:properties");
					for(int i=0; i<properties.getLength(); i++) {
						Element property = (Element) properties.item(i);
						avaProps.add(new MTProperty(property,relTemp));
					}
				}
				//Stores the operations of the current nodeTemp as available
				if(corrType.getSourceInterfaces()!=null) {
					for(TInterface i : corrType.getSourceInterfaces().getInterface()) {
						if(i.getOperation()!=null) {
							for(TOperation o: i.getOperation()) {
								avaOps.add(new MTOperation(o,i,relTemp));
							}
						}
					}
				}
				if(corrType.getTargetInterfaces()!=null) {
					for(TInterface i : corrType.getTargetInterfaces().getInterface()) {
						if(i.getOperation()!=null) {
							for(TOperation o: i.getOperation()) {
								avaOps.add(new MTOperation(o,i,relTemp));
							}
						}
					}
				}
			}
		}

		//Isolates the lists of needed features
			//TODO We should consider also inherited cap/req/props/ops
		List<TCapabilityDefinition> needCaps = new ArrayList<TCapabilityDefinition>();
		if(nodeType.getCapabilityDefinitions()!=null) {
			for(TCapabilityDefinition cd : nodeType.getCapabilityDefinitions().getCapabilityDefinition())
				needCaps.add(cd);
		}
		List<TRequirementDefinition> needReqs = new ArrayList<TRequirementDefinition>();
		if(nodeType.getRequirementDefinitions()!=null) {
			for(TRequirementDefinition rd : nodeType.getRequirementDefinitions().getRequirementDefinition())
				needReqs.add(rd);
		}
		List<Element> needProps = new ArrayList<Element>();
		if(nodeType.getPropertiesDefinition()!=null) {
        	Element propDef = (Element) nodeType.getAny().get(0);
        	//System.out.println(propDef.getNodeName());
        	NodeList properties = propDef.getElementsByTagName("winery:properties");
        	for(int i=0; i<properties.getLength(); i++) {
        		Element property = (Element) properties.item(i);
        		needProps.add(property);
        	}
		}
		List<TOperation> needOps = new ArrayList<TOperation>();
		if(nodeType.getInterfaces()!=null) {
			List<TInterface> intfs = nodeType.getInterfaces().getInterface();
			for(TInterface i : intfs) {
				if(i.getOperation()!=null) {
					for(TOperation o : i.getOperation()) {
						needOps.add(o);
					}
				}
			}
		}

		//Matches capabilities
		Set<List<CapabilityMapping>> capMappings = matchCapabilities(null,needCaps,avaCaps,null);
		System.out.println("Number of mappings: " + capMappings.size());
		for(List<CapabilityMapping> lcm : capMappings) {
			System.out.println("  :::CAP Mapping:::");
			for(CapabilityMapping cm : lcm) {
				System.out.println("    " + cm.getNodeCap().getName() + " -> " + cm.getServCap().getCapability().getId());
			}
		}

		//Matches requirements
		Set<List<RequirementMapping>> reqMappings = matchRequirements(null,needReqs,avaReqs,null);
		System.out.println("Number of mappings: " + reqMappings.size());
		for(List<RequirementMapping> lcm : reqMappings) {
			System.out.println("  :::REQ Mapping:::");
			for(RequirementMapping cm : lcm) {
				System.out.println("    " + cm.getNodeReq().getName() + " -> " + cm.getServReq().getRequirement().getId());
			}
		}

		//Matches properties
		Set<List<PropertyMapping>> propMappings = matchProperties(null,needProps,avaProps,null);
		System.out.println("Number of mappings: " + propMappings.size());
		for(List<PropertyMapping> lpm : propMappings) {
			System.out.println("  :::PROP Mapping:::");
			for(PropertyMapping pm : lpm) {
				String pName = pm.getNodeProp().getElementsByTagName("winery:key").item(0).getTextContent();
				String element = pm.getServProp().getEntityTemplate().getId();
				System.out.println("   " + pName + " -> " + element);
			}
		}

		//Matches operations
		Set<List<OperationMapping>> opMappings = matchOperations(null,needOps,avaOps,null);
		System.out.println("Number of mappings: " + opMappings.size());
		for(List<OperationMapping> lom : opMappings) {
			System.out.println("  :::OP Mapping:::");
			for(OperationMapping om : lom) {
				System.out.println("    " + om.getNodeOp().getName() + " -> " + om.getServOp().getEntityTemplate().getId());
			}
		}

		return null;
	}

	public static void main(String[] args) {
		String nodeTypeLoc = "http://dev.winery.opentosca.org/winery/nodetypes/http%253A%252F%252Ftest.org/ABC/?definitions";
		String servTempLoc = "http://dev.winery.opentosca.org/winery/servicetemplates/http%253A%252F%252Fwww.prova.it/ABC/?definitions";
		MatchmakerBackupSingleClass mm;
		try {
			mm = new MatchmakerBackupSingleClass(nodeTypeLoc,servTempLoc);
			mm.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}