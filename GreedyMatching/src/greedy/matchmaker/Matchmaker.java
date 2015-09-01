package greedy.matchmaker;

import greedy.candidate.Candidate;
import greedy.exceptions.TOSCADefinitionsNotFoundException;
import greedy.exceptions.TOSCADefinitionsNotValidException;
import greedy.mapping.CapabilityMapping;
import greedy.mapping.Mapping;
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
import java.util.Random;
import java.util.Set;

import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TCapabilityDefinition;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TInterface;
import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TOperation;
import org.eclipse.winery.model.tosca.TRelationshipTemplate;
import org.eclipse.winery.model.tosca.TRelationshipType;
import org.eclipse.winery.model.tosca.TRequirement;
import org.eclipse.winery.model.tosca.TRequirementDefinition;
import org.eclipse.winery.model.tosca.TServiceTemplate;
import org.eclipse.winery.model.tosca.TTopologyTemplate;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tosca.util.TOSCAUtils;

/**
 * This (abstract) class implements the "Matchmake" step of the paper.
 *
 * It is intentionally left abstract so as to allow developers to define their own matchmaking operators "M" for each TOSCA feature.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public abstract class Matchmaker {

	/**
	 * Element "Definitions" containing the NodeType under consideration.
	 */
	protected TDefinitions nodeTypeDefs;
	/**
	 * NodeType under consideration.
	 */
	protected TNodeType nodeType;
	/**
	 * Element "Definitions" containing the ServiceTemplate under consideration.
	 */
	protected TDefinitions servTempDefs;
	/**
	 * ServiceTemplate under consideration.
	 */
	protected TServiceTemplate servTemp;
	/**
	 * Probability of having a feature on the boundaries.
	 * (0 means 0.0, 1 means 0.1, ..., 10 means 1.0)	
	 */
	protected int probability;
	

	protected long parsingTime;

	/**
	 * Constructor.
	 *
	 * @param nodeTypeDefs TDefinitions containing the target NodeType.
	 * @param servTempLoc URL from which to retrieve the "TDefinitions" of the available ServiceTemplate.
	 * @param probability Probability of having a feature on the boundaries (0 means 0.0, 1 means 0.1, ..., 10 means 1.0).
	 * @throws TOSCADefinitionsNotFoundException Exception illustrating the unavailability of a "TDefinitions" element at the specified location.
	 * @throws TOSCADefinitionsNotValidException Exception illustrating the impossibility to retrieve the desired TOSCA element from the specified location.
	 */
	public Matchmaker(TDefinitions nodeTypeDefs, String servTempLoc, int probability) throws TOSCADefinitionsNotFoundException, TOSCADefinitionsNotValidException {
		//Parsing of the NodeType's TDefinitions
//		nodeTypeDefs = TOSCAUtils.parseDefinitions(nodeTypeLoc);
		if(nodeTypeDefs == null)
			throw new TOSCADefinitionsNotFoundException("Missing target NodeType");
		this.nodeTypeDefs = nodeTypeDefs;
		TExtensibleElements element = nodeTypeDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		if(!(element instanceof TNodeType))
			throw new TOSCADefinitionsNotValidException("Target node type location is not (really) defining a TOSCA NodeType.");
		nodeType = (TNodeType) element;

		//Parsing of the ServiceTemplate's TDefinitions
		servTempDefs = TOSCAUtils.parseDefinitions(servTempLoc);
		if(servTempDefs == null)
			throw new TOSCADefinitionsNotFoundException(servTempLoc);
		element = servTempDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		if(!(element instanceof TServiceTemplate))
			throw new TOSCADefinitionsNotValidException(servTempLoc + " is not defining a TOSCA ServiceTemplate.");
		servTemp = (TServiceTemplate) element;

		//Set "probability"
		this.probability = probability;
		parsingTime = 0;
	}

	/**
	 * This method performs the "plug-in matching" check between the Capability "c" and the CapabilityDefinition "cd"
	 *
	 * @param c Capability to be matched.
	 * @param cd CapabilityDefinition to be matched.
	 *
	 * @return "true", if "c" matches "cd"; "false", otherwise.
	 */
	protected abstract boolean match(TCapability c, TCapabilityDefinition cd);

	/**
	 * This method performs the matchmaking of capabilities.
	 * [It copes with requirement multiplicity by ensuring that the lowerBound(s) of the CapabilityDefinition(s) are satisfied]
	 *
	 * @param matched Set of capability mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of CapabilityDefinition to be matched.
	 * @param available Set of available Capability (in the form of "MTCapability").
	 * @param solutions Set of capability mappings detected by the previously run instances of the method (initially, "null").
	 *
	 * @return The Set of detected CapabilityMappings.
	 */
	protected Set<List<CapabilityMapping>> matchCapabilities(
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
			if(isSolution)
				//If so, adds "matched" to "solutions"
				//  If "needed" is initially empty, the added "matched" is empty and
				//  represents that no mappings have been/should be detected.
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
	
//	protected List<CapabilityMapping> matchCapabilities(List<TCapabilityDefinition> needed,
//			List<MTCapability> available) {
//		List<CapabilityMapping> solutions = new ArrayList<CapabilityMapping>();
//		
//		//Checks whether there are no available capabilities
//		if(available.size()==0) {
//			return solutions;
//		}
//
//		//Otherwise matchmakes
//		for(TCapabilityDefinition cd : needed) {
//			boolean matched = false;
//			for(MTCapability ca : available) {
//				if(match(ca.getCapability(),cd) && cd.getLowerBound()>0) {
//					solutions.add(new CapabilityMapping(cd,ca));
//					matched = true;
//				}
//			}
//			if(!matched) {
//				solutions.clear();
//				return solutions;
//			}
//		}
//
//		//Recurs on the updated state
//		return solutions;
//	}

	/**
	 * This method performs the "plug-in matching" check between the Requirement "c" and the RequirementDefinition "cd"
	 *
	 * @param r Requirement to be matched.
	 * @param rd RequirementDefinition to be matched.
	 *
	 * @return "true", if "r" matches "rd"; "false", otherwise.
	 */
	protected abstract boolean match(TRequirement r, TRequirementDefinition rd);

	/**
	 * This method performs the matchmaking of requirements.
	 * [It copes with requirement multiplicity by ensuring that the lowerBound(s) of the RequirementDefinition(s) are satisfied]
	 *
	 * @param matched Set of requirement mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of RequirementDefinition to be matched.
	 * @param available Set of available Requirement (in the form of "MTRequirement").
	 * @param solutions Set of requirement mappings detected by the previously run instances of the method (initially, "null").
	 *
	 * @return The Set of detected RequirementMapping.
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
			if(isSolution)
				//If so, adds "matched" to "solutions"
				//  If "needed" is initially empty, the added "matched" is empty and
				//  represents that no mappings have been/should be detected.
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
	protected abstract boolean match(Element p1, Element p2);

	/**
	 * This method performs the matchmaking of properties.
	 *
	 * @param matched Set of property mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of TProperty to be matched.
	 * @param available Set of available TOperation (in the form of "MTProperty").
	 * @param solutions Set of property mappings detected by the previously run instances of the method (initially, "null").
	 *
	 * @return The Set of detected PropertyMapping.
	 */
	protected Set<List<PropertyMapping>> matchProperties(
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
			if(needed.size()==0)
				//If so, adds "matched" to "solutions"
				//  If "needed" is initially empty, the added "matched" is empty and
				//  represents that no mappings have been/should be detected.
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

	/**
	 * This method performs the "exact matching" check between the Operations "o1" and "o2"
	 *
	 * @param o1 TOperation to be matched.
	 * @param o2 TOperation to be matched.
	 *
	 * @return "true", if "o1" matches "o2"; "false", otherwise.
	 */
	protected abstract boolean match(TOperation o1, TOperation o2);


	/**
	 * This method performs the matchmaking of operations.
	 *
	 * @param matched Set of operation mappings detected by the current instance of the method (initially, "null").
	 * @param needed Set of TOperation to be matched.
	 * @param available Set of available TOperation (in the form of "MTOperation").
	 * @param solutions Set of operation mappings detected by the previously run instances of the method (initially, "null").
	 *
	 * @return The Set of detected OperationMapping.
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
			if(needed.size()==0)
				//If so, adds "matched" to "solutions"
				//  If "needed" is initially empty, the added "matched" is empty and
				//  represents that no mappings have been/should be detected.
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

	/**
	 * This method performs the concrete execution of the matchmaking between the specified node type and service templates.
	 *
	 * @return List of detected "Candidate" topology fragments.
	 */
	public List<Candidate> run() {
		TTopologyTemplate top = servTemp.getTopologyTemplate();

		//========================================
		//Creates an empty list of candidates
		//========================================
		List<Candidate> candidates = new ArrayList<Candidate>();

		//========================================
		//Isolates the lists of available features
		//========================================
		List<MTCapability> avaCaps = new ArrayList<MTCapability>();
		List<MTRequirement> avaReqs = new ArrayList<MTRequirement>();
		List<MTProperty> avaProps = new ArrayList<MTProperty>();
		List<MTOperation> avaOps = new ArrayList<MTOperation>();
		for(TEntityTemplate entity : top.getNodeTemplateOrRelationshipTemplate()){
			Random gen = new Random();
			if(entity instanceof TNodeTemplate) {
				//Stores (with a certain "probability") the current nodeTemp and retrieves its type
				TNodeTemplate nodeTemp = (TNodeTemplate) entity;

				long startTime = System.currentTimeMillis();
				TOSCAUtils tu = new TOSCAUtils(servTempDefs);
				TNodeType corrType = (TNodeType) tu.retrieveType(nodeTemp.getType());
				parsingTime += System.currentTimeMillis() - startTime;

				//Stores (with a certain "probability") the capabilities of the current nodeTemp as available
				if(nodeTemp.getCapabilities()!=null) {
					for(TCapability c : nodeTemp.getCapabilities().getCapability()) {
						int n = gen.nextInt(10)+1;
						if(n<=probability)
							avaCaps.add(new MTCapability(c,nodeTemp));
					}
				}
				//Stores (with a certain "probability") the requirements of the current nodeTemp as available
				if(nodeTemp.getRequirements()!=null) {
					for(TRequirement r : nodeTemp.getRequirements().getRequirement()) {
						int n = gen.nextInt(10)+1;
						if(n<=probability)
							avaReqs.add(new MTRequirement(r,nodeTemp));
					}
				}
				//Stores (with a certain "probability") the properties of the current nodeTemp as available
				if(corrType.getPropertiesDefinition()!=null) {
                	Element propDef = (Element) corrType.getAny().get(0);
                	//System.out.println(propDef.getNodeName());
                	NodeList properties = propDef.getElementsByTagName("winery:properties");
                	for(int i=0; i<properties.getLength(); i++) {
                		Element property = (Element) properties.item(i);
                		int n = gen.nextInt(10)+1;
						if(n<=probability)
							avaProps.add(new MTProperty(property,nodeTemp));
                	}
				}
				//Stores (with a certain "probability") the operations of the current nodeTemp as available
				if(corrType.getInterfaces()!=null) {
					for(TInterface i : corrType.getInterfaces().getInterface()) {
						if(i.getOperation()!=null) {
							for(TOperation o: i.getOperation()) {
								int n = gen.nextInt(10)+1;
								if(n<=probability)
									avaOps.add(new MTOperation(o,i,nodeTemp));
							}
						}
					}
				}
			}
			if(entity instanceof TRelationshipTemplate) {
				//Stores (with a certain "probability") the current relTemp and retrieves its type
				TRelationshipTemplate relTemp = (TRelationshipTemplate) entity;

				long startTime = System.currentTimeMillis();
				TOSCAUtils tu = new TOSCAUtils(servTempDefs);
				TRelationshipType corrType = (TRelationshipType) tu.retrieveType(relTemp.getType());
				parsingTime += System.currentTimeMillis() - startTime;

				//Stores (with a certain "probability") the properties of the current nodeTemp as available
				if(corrType.getPropertiesDefinition()!=null) {
					Element propDef = (Element) corrType.getAny().get(0);
					NodeList properties = propDef.getElementsByTagName("winery:properties");
					for(int i=0; i<properties.getLength(); i++) {
						Element property = (Element) properties.item(i);
						int n = gen.nextInt(10)+1;
						if(n<=probability)
							avaProps.add(new MTProperty(property,relTemp));
					}
				}
				//Stores (with a certain "probability") the operations of the current relTemp as available - from SourceInterface(s)
				if(corrType.getSourceInterfaces()!=null) {
					for(TInterface i : corrType.getSourceInterfaces().getInterface()) {
						if(i.getOperation()!=null) {
							for(TOperation o: i.getOperation()) {
								int n = gen.nextInt(10)+1;
								if(n<=probability)
									avaOps.add(new MTOperation(o,i,relTemp));
							}
						}
					}
				}
				//Stores (with a certain "probability") the operations of the current relTemp as available - from TargetInterface(s)
				if(corrType.getTargetInterfaces()!=null) {
					for(TInterface i : corrType.getTargetInterfaces().getInterface()) {
						if(i.getOperation()!=null) {
							for(TOperation o: i.getOperation()) {
								int n = gen.nextInt(10)+1;
								if(n<=probability)
									avaOps.add(new MTOperation(o,i,relTemp));
							}
						}
					}
				}
			}
		}

		//========================================
		//Isolates the lists of needed features
		//========================================
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

		//==========================================
		//Performs the matchmaking of TOSCA features
		//==========================================
		//Matches capabilities
		Set<List<CapabilityMapping>> capMappings = matchCapabilities(null,needCaps,avaCaps,null);
		boolean noCaps = true;
		for(TCapabilityDefinition cd : needCaps) {
			if(cd.getLowerBound()>0)
				noCaps = false;
		}
		if(capMappings.isEmpty()&&!noCaps) {
			//System.err.println("[Matchmaker - Parsing time(ms)]: " + parsingTime);
			return candidates;
		}

		//Matches requirements
		Set<List<RequirementMapping>> reqMappings = matchRequirements(null,needReqs,avaReqs,null);
		boolean noReqs = true;
		for(TRequirementDefinition rd : needReqs) {
			if(rd.getLowerBound()>0)
				noReqs = false;
		}
		if(reqMappings.isEmpty()&&!noReqs) {
			//System.err.println("[Matchmaker - Parsing time(ms)]: " + parsingTime);
			return candidates;
		}

		//Matches properties
		Set<List<PropertyMapping>> propMappings = matchProperties(null,needProps,avaProps,null);
		if(propMappings.isEmpty()&&!needProps.isEmpty()) {
			//System.err.println("[Matchmaker - Parsing time(ms)]: " + parsingTime);
			return candidates;
		}

		//Matches operations
		Set<List<OperationMapping>> opMappings = matchOperations(null,needOps,avaOps,null);
		if(opMappings.isEmpty()&&!needOps.isEmpty()) {
			//System.err.println("[Matchmaker - Parsing time(ms)]: " + parsingTime);
			return candidates;
		}

		//==================================================
		//Computes the set of "Candidate" topology fragments
		//==================================================
		for(List<CapabilityMapping> capMap : capMappings) {
			for(List<RequirementMapping> reqMap : reqMappings) {
				for(List<PropertyMapping> propMap : propMappings) {
					for(List<OperationMapping> opMap : opMappings) {
						//Computes each one of the possible mappings
						Mapping map = new Mapping(capMap,reqMap,propMap,opMap);
						//Determines the topology fragment interested by the computed mapping
						Set<TEntityTemplate> elems = new HashSet<TEntityTemplate>();
						for(CapabilityMapping cm : capMap) {
							elems.add(cm.getServCap().getNodeTemplate());
						}
						for(RequirementMapping rm : reqMap) {
							elems.add(rm.getServReq().getNodeTemplate());
						}
						for(PropertyMapping pm : propMap) {
							elems.add(pm.getServProp().getEntityTemplate());
						}
						for(OperationMapping om : opMap) {
							elems.add(om.getServOp().getEntityTemplate());
						}
						//Checks whether the fragment is already a candidate
						Candidate alreadyIn = null;
						for(Candidate cand : candidates) {
							if(cand.getFragment().containsAll(elems) && elems.containsAll(cand.getFragment()))
								alreadyIn = cand;
						}
						if(alreadyIn!=null)
							//If so, it adds the new mapping to such a fragment
							alreadyIn.addMapping(map);
						else
							//Otherwise, creates a new candidate
							candidates.add(new Candidate(servTempDefs, elems, map));
					}
				}
			}
		}
		//System.err.println("[Matchmaker - Parsing time(ms)]: " + parsingTime);
		return candidates;
	}
}