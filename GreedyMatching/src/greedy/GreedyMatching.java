package greedy;

import greedy.candidate.Candidate;
import greedy.exceptions.TOSCADefinitionsNotFoundException;
import greedy.exceptions.TOSCADefinitionsNotValidException;
import greedy.mapping.CapabilityMapping;
import greedy.mapping.Mapping;
import greedy.mapping.OperationMapping;
import greedy.mapping.PropertyMapping;
import greedy.mapping.RequirementMapping;
import greedy.matchmaker.PrototypeMatchmaker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.winery.model.tosca.TBoundaryDefinitions;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Capabilities;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Interfaces;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Properties.PropertyMappings;
import org.eclipse.winery.model.tosca.TBoundaryDefinitions.Requirements;
import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TCapabilityRef;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TExportedInterface;
import org.eclipse.winery.model.tosca.TExportedOperation;
import org.eclipse.winery.model.tosca.TExportedOperation.NodeOperation;
import org.eclipse.winery.model.tosca.TExportedOperation.RelationshipOperation;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TImport;
import org.eclipse.winery.model.tosca.TInterface;
import org.eclipse.winery.model.tosca.TNodeTemplate;
import org.eclipse.winery.model.tosca.TNodeType;
import org.eclipse.winery.model.tosca.TOperation;
import org.eclipse.winery.model.tosca.TPropertyMapping;
import org.eclipse.winery.model.tosca.TRelationshipTemplate;
import org.eclipse.winery.model.tosca.TRequirement;
import org.eclipse.winery.model.tosca.TRequirementRef;
import org.eclipse.winery.model.tosca.TServiceTemplate;
import org.eclipse.winery.model.tosca.TTopologyTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tosca.util.TOSCAUtils;

/**
 * This class implements a emulates the matchmaking and adaptation approach proposed in the
 * below specified work.
 * <br>
 * Title: "Matching cloud services with TOSCA"
 * <br>
 * Authors: A. Brogi, J. Soldani
 * <br>
 * More precisely, it performs a greedy research of a matching service inside of a repository
 * of TOSCA specifications, by accessing randomly to the aforementioned repository (and by
 * assuming that each feature is exposed on the boundaries with a certain probability).
 * <br>
 * <br>
 * Its purpose is to provide a reuse method to be compared with that proposed in the below
 * specified work.
 * <br>
 * Title: "TOSCA-MART: A TOSCA-based Method for Adapting and Reusing application Topologies"
 * <br>
 * Authors: T. Binz, U. Breitenbucher, A. Brogi, F. Leymann, J. Soldani)
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class GreedyMatching {

	public static String prefix = "";

    private static List<Candidate> matchmake(TDefinitions nodeTypeDefs, String servTempLoc, int probability) throws TOSCADefinitionsNotFoundException, TOSCADefinitionsNotValidException {
        PrototypeMatchmaker mm = new PrototypeMatchmaker(nodeTypeDefs,servTempLoc, probability);
        return mm.run();
    }

    private static List<Candidate> greedySearch(TDefinitions nodeTypeDefs, List<String> servTempLocs, int probability) {
        List<Candidate> candidates = new ArrayList<Candidate>();
        //If the target is empty, returns nothing
        TNodeType targetNodeType = (TNodeType) nodeTypeDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
        if(targetNodeType.getCapabilityDefinitions()==null &&
        		targetNodeType.getRequirementDefinitions()==null &&
        		targetNodeType.getPropertiesDefinition()==null &&
        		targetNodeType.getInterfaces()==null)
        	return candidates;
        for(String servTempLoc : servTempLocs){
            try {
                candidates.addAll(matchmake(nodeTypeDefs,servTempLoc, probability));
            } catch (Exception e) {
                System.err.println("[GreedySearch]: " + e.getMessage());
            }
            if(!candidates.isEmpty())
            	return candidates;
        }
        return candidates;
    }

    private static Candidate selectMapping(Candidate cand) {
        //Takes the first mapping as the one to be selected
        Iterator<Mapping> iMap = cand.getMappings().iterator();
        Mapping selectedMapping = iMap.next();

        //=========================================
		//Duplicates the candidate cand
        // (by making it contain only the "selectedMap")
		//and returns the (duplicated) candidate
        // (with "selectedMap" as the only possible mapping)
        //=========================================
		Candidate candWithSelectedMap = new Candidate(cand.getStartingTopology(),cand.getFragment(),selectedMapping);
        candWithSelectedMap.setRate(cand.getRate());
        return candWithSelectedMap;

    }

    private static List<Candidate> mappingSelection(TDefinitions nodeTypeDefs, List<String> servTempLocs, int probability) {
        long elapsedTime = System.nanoTime();
    	List<Candidate> ratedCandidates = greedySearch(nodeTypeDefs,servTempLocs,probability);
    	elapsedTime = System.nanoTime() - elapsedTime;
    	System.out.print(elapsedTime/1000 + ",");
        List<Candidate> electedCandidates = new ArrayList<Candidate>();
        for(Candidate cand : ratedCandidates) {
            electedCandidates.add(selectMapping(cand));
        }
        return electedCandidates;
    }

    private static List<TDefinitions> adaptation(String nodeTypeLoc, List<String> servTempLocs, int probability) {
        //Parses and stores the targetNodeType
        long parsingTime = System.nanoTime();
    	TDefinitions nodeTypeDefs = TOSCAUtils.parseDefinitions(nodeTypeLoc);
    	TExtensibleElements element = nodeTypeDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
        TNodeType targetNodeType = (TNodeType) element;
        parsingTime = System.nanoTime() - parsingTime;
        //System.err.println("[Adaptation - Parsing Time(ms)]: " + parsingTime);

        long elapsedTime = System.nanoTime();
        List<Candidate> electedCandidates = mappingSelection(nodeTypeDefs, servTempLocs, probability);
    	elapsedTime = System.nanoTime() - elapsedTime;
        //System.err.println("[MappingSelection - Elapsed Time(ms)]: " + elapsedTime);
    	System.out.print(elapsedTime/1000 + ",");
    	List<TDefinitions> adaptedServices = new ArrayList<TDefinitions>();

        //Adapts each one of the "electedCandidates"
        for(Candidate cand : electedCandidates) {
            //Extracts the definition of the ServiceTemplate "candServ" from which the fragment is excerpted
            TServiceTemplate candServ = (TServiceTemplate) cand.getStartingTopology().getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);

            //Creates the list of RelationshipTemplates "relTemps" to be used to extend the fragment
            // (so as to satisfy all unsatisfied dependencies)
            List<TRelationshipTemplate> relTemps  = new ArrayList<TRelationshipTemplate>();
            for(TEntityTemplate entity : candServ.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate())
                if(entity instanceof TRelationshipTemplate)
                    relTemps.add((TRelationshipTemplate) entity);

            //Extends the fragment by adding those nodes and relationships which
            //are required to satisfy the inter-node dependencies
            boolean continueExtension = true;
            while(continueExtension) {
                continueExtension = false;
                for(int i=relTemps.size()-1; i>=0; i--) {
                    TRelationshipTemplate rel = relTemps.get(i);
                    //Extracts the "source" of the relationship
                    // (If "source" is a requirement - not present between those declared by the
                    //  target node -, the corresponding node is taken)
                    TEntityTemplate source = (TEntityTemplate) rel.getSourceElement().getRef();
                    boolean toBeAdded = true;
                    if(source instanceof TRequirement) {
                        TRequirement sourceReq = (TRequirement) source;

                        //If there exists a mapping from the "sourceReq" to one of those of the NodeType
                        //the dependency must not be added.
                        for(Mapping m : cand.getMappings())
                            for(RequirementMapping rm : m.getReqsMap())
                                if(rm.getServReq().equals(sourceReq))
                                    toBeAdded = false;

                        //Otherwise, stores the NodeTemplate in which "sourceReq" appears as the
                        //"source" of the relationship
                        if(toBeAdded){
                            for(TEntityTemplate e : candServ.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate()) {
                                if(e instanceof TNodeTemplate) {
                                    TNodeTemplate eNode = (TNodeTemplate) e;
                                    if(eNode.getRequirements() != null)
                                        if(eNode.getRequirements().getRequirement().contains(sourceReq))
                                            source = eNode;
                                }
                            }
                        }
                    }
                    //Checks whether the dependency must be added
                    if(toBeAdded) {
                        //Extracts the "target" of the relationship
                        // (If "target" is a capability, takes the corresponding node)
                        TEntityTemplate target = (TEntityTemplate) rel.getTargetElement().getRef();
                        if(target instanceof TCapability) {
                            TCapability targetCap = (TCapability) target;
                            for(TEntityTemplate e : candServ.getTopologyTemplate().getNodeTemplateOrRelationshipTemplate()) {
                                if(e instanceof TNodeTemplate) {
                                    TNodeTemplate eNode = (TNodeTemplate) e;
                                    if(eNode.getCapabilities() != null)
                                        if(eNode.getCapabilities().getCapability().contains(targetCap))
                                            target = eNode;
                                }
                            }
                        }
                        //If the source of the relationship is in the candidate fragment,
                        //the relationship itself and its target must be in the fragment.
                        if(cand.getFragment().contains(source)) {
                            relTemps.remove(rel);
                            continueExtension = true;
                            cand.getFragment().add(rel);
                            cand.getFragment().add(target);
                        }
                        //Otherwise, if the relationship is already in the candidate fragment,
                        //its source and target nodes must also be in the fragment.
                        else if(cand.getFragment().contains(rel)) {
                        	relTemps.remove(rel);
                        	continueExtension = true;
                        	cand.getFragment().add(source);
                        	cand.getFragment().add(target);
                        }
                    }
                    //Otherwise, removes the dependencies from the list of dependencies
                    //to be used while adapting the fragment
                    else {
                        relTemps.remove(rel);
                    }
                }
            }

            //========================================================
            //Once the candidate (fragment) has been properly extended,
            //creates its corresponding TDefinitions
            //========================================================
            int candNumber = electedCandidates.indexOf(cand) + 1;

            //CREATES A NEW "DEFINITIONS" ELEMENT
            TDefinitions candDefs = TOSCAUtils.createDefinitions();
            TDefinitions startDefs = cand.getStartingTopology();
            //Starting from the to-be-adapted service
            TServiceTemplate startServ = (TServiceTemplate) startDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
            //Creates a new id
            candDefs.setId(startServ.getId() + "_Adaptation" + candNumber);
            //Copies the targetNamespace
            candDefs.setTargetNamespace(startDefs.getTargetNamespace());
            //Creates the set of imports required to properly process the adapted spec
			for(TImport imp : cand.getStartingTopology().getImport())
				candDefs.getImport().add(imp);

            //CREATES A NEW "SERVICE TEMPLATE" ELEMENT (and adds it to the new Definitions)
            TServiceTemplate candAdaptedServ = new TServiceTemplate();
            candDefs.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().add(candAdaptedServ);
            //Creates a new id
            candAdaptedServ.setId(startServ.getId() + "_Adaptation" + candNumber);
            //Creates a new service name
            candAdaptedServ.setName(startServ.getName() + "_Adaptation" + candNumber);
            //Copies the targetNamespace
            candAdaptedServ.setTargetNamespace(startServ.getTargetNamespace());

            //CREATES A NEW "TOPOLOGY TEMPLATE" ELEMENT (and adds it to the new ServiceTemplate)
            TTopologyTemplate candTopo = new TTopologyTemplate();
            candAdaptedServ.setTopologyTemplate(candTopo);
            //Adds the candidate fragment to the created topology
            for(TEntityTemplate e : cand.getFragment())
                candTopo.getNodeTemplateOrRelationshipTemplate().add(e);

            //CREATES A NEW "BOUNDARY DEFINITIONS" ELEMENT (and adds it to the new ServiceTemplate)
            TBoundaryDefinitions boundaries = new TBoundaryDefinitions();
            candAdaptedServ.setBoundaryDefinitions(boundaries);
            //Adds mappings to the new boundaries
            for(Mapping m : cand.getMappings()) {
                //Capability mappings
                if(!m.getCapsMap().isEmpty()) {
                    Capabilities capabilities = new Capabilities();
                    boundaries.setCapabilities(capabilities);
                    for(CapabilityMapping cm : m.getCapsMap()) {
                        TCapabilityRef capRef = new TCapabilityRef();
                        capRef.setName(cm.getNodeCap().getName());
                        capRef.setRef(cm.getServCap().getCapability());
                        capabilities.getCapability().add(capRef);
                    }
                }
                //Requirement mappings
                if(!m.getReqsMap().isEmpty()) {
                    Requirements requirements = new Requirements();
                    boundaries.setRequirements(requirements);
                    for(RequirementMapping cm : m.getReqsMap()) {
                        TRequirementRef reqRef = new TRequirementRef();
                        reqRef.setName(cm.getNodeReq().getName());
                        reqRef.setRef(cm.getServReq().getRequirement());
                        requirements.getRequirement().add(reqRef);
                    }
                }
                //Interface operation mappings
                if(!m.getOpsMap().isEmpty()) {
                    Interfaces intfs = new Interfaces();
                    boundaries.setInterfaces(intfs);
                    if(targetNodeType.getInterfaces()!=null) {
                        for(TInterface i : targetNodeType.getInterfaces().getInterface()) {
                            TExportedInterface expI = new TExportedInterface();
                            expI.setName(i.getName());
                            for(TOperation oi : i.getOperation()) {
                                for(OperationMapping om : m.getOpsMap()) {
                                    if(oi.equals(om.getNodeOp())) {
                                        TExportedOperation expOi = new TExportedOperation();
                                        expOi.setName(om.getNodeOp().getName());
                                        if(om.getServOp().getEntityTemplate() instanceof TNodeTemplate) {
                                            NodeOperation no = new NodeOperation();
                                            no.setNodeRef(om.getServOp().getEntityTemplate());
                                            no.setInterfaceName(om.getServOp().getInterface().getName());
                                            no.setOperationName(om.getServOp().getOperation().getName());
                                            expOi.setNodeOperation(no);
                                        }
                                        if(om.getServOp().getEntityTemplate() instanceof TRelationshipTemplate) {
                                            RelationshipOperation ro = new RelationshipOperation();
                                            ro.setRelationshipRef(om.getServOp().getEntityTemplate());
                                            ro.setInterfaceName(om.getServOp().getInterface().getName());
                                            ro.setOperationName(om.getServOp().getOperation().getName());
                                            expOi.setRelationshipOperation(ro);
                                        }
                                        expI.getOperation().add(expOi);
                                    }
                                }
                            }
                            intfs.getInterface().add(expI);
                        }
                    }
                }
                //Properties mapping
                if(!m.getPropsMap().isEmpty()) {
                    Properties props = new Properties();

                    Document doc = null;
                    Element xmlProps = null;
                    try {
                    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    	DocumentBuilder db = dbf.newDocumentBuilder();
                    	doc = db.newDocument();
                    } catch(Exception e) {
                    	e.printStackTrace();
                    }
                    xmlProps = doc.createElement("targetNT:Properties");
                    Element targetProps = (Element) targetNodeType.getAny().get(0);
                    if(targetProps.hasAttribute("namespace"))
                    	xmlProps.setAttribute("xmlns:targetNT", targetProps.getAttribute("namespace"));
                    else
                    	xmlProps.setAttribute("xmlns:targetNT", "");
                	props.setAny(xmlProps);
                    PropertyMappings propMaps = new PropertyMappings();
                    props.setPropertyMappings(propMaps);
                    for(PropertyMapping pm : m.getPropsMap()) {
                    	//Isolates node/serv property names
                    	Element nodeProp = pm.getNodeProp();
                    	String nodePropName = nodeProp.getElementsByTagName("winery:key").item(0).getTextContent();
                    	Element servProp = pm.getServProp().getProperty();
                    	String servPropName = servProp.getElementsByTagName("winery:key").item(0).getTextContent();
                    	//Creates a new property on the "boundaries"
                    	// (whose name is the same as that declared in the "nodeProp")
                    	xmlProps.appendChild(doc.createElement("targetNT:" + nodePropName));
                    	//Adds a mapping between the new property and the
                    	//(internally) available property
                        TPropertyMapping p = new TPropertyMapping();
                        p.setServiceTemplatePropertyRef("//targetNT:" + nodePropName +  "[1]");
                        p.setTargetObjectRef(pm.getServProp().getEntityTemplate());
                        p.setTargetPropertyRef("//" + servPropName + "[1]");
                        propMaps.getPropertyMapping().add(p);
                    }
                    boundaries.setProperties(props);
                }
            }
            //Adds the TDefinitions containing the adaptation of "cand" into "adaptedServices"
            adaptedServices.add(candDefs);
        }
        //Returns the set of "adaptedServices"
        return adaptedServices;
    }

    /**
     * This method operates the greedy matchmaking and adaptation method.
     *
     * @param nodeTypeLoc TDefinitions of the NodeType to be matched.
     * @param servTempLocs String(s) representing the location of the ServiceTemplate(s) to be matched.
     * @param probability Threshold representing the maximum number of candidates to be detected.
     *
     * @return List of String(s) containing the names of generated TOSCA specification file.
     */
    public static String run(String nodeTypeLoc, List<String> servTempLocs, int probability) {
    	//Generates the "adaptedServices" by invoking the "Adaptation" step of "TOSCA-MART"
    	long elapsedTime = System.nanoTime();
    	List<TDefinitions> adaptedServices = adaptation(nodeTypeLoc, servTempLocs, probability);
    	elapsedTime = System.nanoTime() - elapsedTime;
    	//System.err.println("[Adaptation - Elapsed Time]: " + (elapsedTime / 1000) + "s," + (elapsedTime % 1000) + "ms");
    	System.out.print(elapsedTime/1000 + ",");

    	//Generates (and returns) the list of String(s) containing the names of the generated specs
    	String generatedSpec = null;
    	for(TDefinitions adaptedDefs : adaptedServices) {
    		String specFileName = prefix + adaptedDefs.getId() + ".tosca";
    		TOSCAUtils.writeDefinitions(adaptedDefs, specFileName);
    		generatedSpec = specFileName;
    	}
    	return generatedSpec;
    }

    /**
     * This (runnable) method is used to test the "TOSCA-MART" matchmaking and adaptation approach.
     *
     * @param args It takes a set of String(s). The former one contains the location where to retrieve the NodeType to be matched, the latter contains the number of implementations to be generated, while the in between one(s) represent the location(s) of the ServiceTemplate(s) composing the repository of available cloud application(s).
     */
    public static void main(String[] args) {
        if(args.length<3) {
        	System.err.println("[Test]: Too few command line arguments.");
        	return;
        }
    	//NodeType location
        String nodeTypeLoc = args[0];
        System.out.println("Target node location: [" + nodeTypeLoc + "]");
        //ServiceTemplate locations
        List<String> servTempLocs = new ArrayList<String>();
		try {
			// URL to load
			URL url = new URL(args[1]);

			// Read directly from file url
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String defLine;
			while((defLine = in.readLine())!=null) {
				servTempLocs.add(defLine);
			}
			in.close();
		} catch(Exception e) {
			servTempLocs = null;
		}

        System.out.println("Size of the repository: [" + servTempLocs.size() + "]");

        int probability = Integer.parseInt(args[2]);
        //Generates specs of adapted services
        long elapsedTime = System.nanoTime();
        String adaptedSpec = run(nodeTypeLoc,servTempLocs,probability);
        elapsedTime = System.nanoTime() - elapsedTime;
        System.out.println("\n Generated specification:");
        System.out.println("|_ " + adaptedSpec);
        System.out.println("\n Elapsed Time: [" + (elapsedTime / 1000) + "s," + (elapsedTime % 1000) + "ms]");
    }
}
