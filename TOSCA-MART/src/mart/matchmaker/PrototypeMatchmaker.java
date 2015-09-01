package mart.matchmaker;

import java.util.ArrayList;
import java.util.List;

import mart.candidate.Candidate;
import mart.exceptions.TOSCADefinitionsNotFoundException;
import mart.exceptions.TOSCADefinitionsNotValidException;
import mart.mapping.Mapping;

import org.eclipse.winery.model.tosca.TCapability;
import org.eclipse.winery.model.tosca.TCapabilityDefinition;
import org.eclipse.winery.model.tosca.TCapabilityType;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TOperation;
import org.eclipse.winery.model.tosca.TParameter;
import org.eclipse.winery.model.tosca.TRequirement;
import org.eclipse.winery.model.tosca.TRequirementDefinition;
import org.eclipse.winery.model.tosca.TRequirementType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import tosca.util.TOSCAUtils;

/**
 * This class implements the "Matchmake" step of the paper by extending the abstract "Matchmaker" class.
 * It completes the specification of the matchmaking procedure by implementing the operators "M" for each TOSCA feature.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class PrototypeMatchmaker extends Matchmaker {

	/**
	 * Constructor.
	 *
	 * @param nodeTypeDefs TDefinitions containing the target NodeType.
	 * @param servTempLoc URL from which to retrieve the "TDefinitions" of the available ServiceTemplate.
	 * @throws TOSCADefinitionsNotFoundException Exception illustrating the unavailability of a "TDefinitions" element at the specified location.
	 * @throws TOSCADefinitionsNotValidException Exception illustrating the impossibility to retrieve the desired TOSCA element from the specified location.
	 */
	public PrototypeMatchmaker(TDefinitions nodeTypeDefs, String servTempLoc) throws TOSCADefinitionsNotFoundException, TOSCADefinitionsNotValidException {
		super(nodeTypeDefs,servTempLoc);
//		public PrototypeMatchmaker(String nodeTypeLoc, String servTempLoc) throws TOSCADefinitionsNotFoundException, TOSCADefinitionsNotValidException {
//			super(nodeTypeLoc,servTempLoc);
	}

	@Override
	protected boolean match(TCapability c, TCapabilityDefinition cd) {
		//Checks whether the names are equal
		if(!c.getName().equals(cd.getName()))
			return false;
		//Checks whether the type of "cd" is a sub-type of "c".
		//If so, "cd" plug-in matches "c".
		if(c.getType().equals(cd.getCapabilityType()))
			return true;

//		long startTime = System.currentTimeMillis();
		TOSCAUtils tu = new TOSCAUtils(servTempDefs);
		TCapabilityType cType = (TCapabilityType) tu.retrieveType(c.getType());
//		parsingTime += System.currentTimeMillis() - startTime;

		System.err.println(cType.getName());
		while(cType.getDerivedFrom()!=null) {
			if(cType.getDerivedFrom().getTypeRef().equals(cd.getCapabilityType()))
				return true;

//			startTime = System.currentTimeMillis();
			cType = (TCapabilityType) tu.retrieveType(cType.getDerivedFrom().getTypeRef());
//			parsingTime += System.currentTimeMillis() - startTime;
		}
		//Otherwise, "cd" does not plug-in match "c".
		return false;
	}


	@Override
	protected boolean match(TRequirement r, TRequirementDefinition rd) {
		//Checks whether the names are equal
		if(!r.getName().equals(rd.getName()))
			return false;
		//Checks whether the type of "rd" is a sub-type of "r".
		//If so, "rd" plug-in matches "r".
		if(r.getType().equals(rd.getRequirementType()))
			return true;

//		long startTime = System.currentTimeMillis();
		TOSCAUtils tu = new TOSCAUtils(servTempDefs);
		TRequirementType rType = (TRequirementType) tu.retrieveType(r.getType());
//		parsingTime += System.currentTimeMillis() - startTime;

		System.err.println(rType.getName());
		while(rType.getDerivedFrom()!=null) {
			if(rType.getDerivedFrom().getTypeRef().equals(rd.getRequirementType()))
				return true;

//			startTime = System.currentTimeMillis();
			rType = (TRequirementType) tu.retrieveType(rType.getDerivedFrom().getTypeRef());
//			parsingTime += System.currentTimeMillis() - startTime;
		}
		//Otherwise, "rd" does not plug-in match "r".
		return false;
	}

	@Override
	protected boolean match(Element p1, Element p2) {
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

	@Override
	protected boolean match(TOperation o1, TOperation o2) {
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
						p1.getType().equals(p2.getType()) &&
						p1.getRequired().value().equals(p2.getRequired().value()))
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
	 * This (runnable) class allows to test the implemented PrototypeMatchmaker.
	 *
	 * @param args The method takes two String arguments representing the locations of the NodeType and of the ServiceTemplate to be matched, respectively.
	 */
	public static void main(String[] args) {
		if(args.length!=2) {
			System.err.println("The arguments to be specified are TWO.");
			return;
		}
		String nodeTypeLoc = args[0]; //"http://dev.winery.opentosca.org/winery/nodetypes/http%253A%252F%252Ftest.org/ABC/?definitions";
		String servTempLoc = args[1]; //"http://dev.winery.opentosca.org/winery/servicetemplates/http%253A%252F%252Fwww.prova.it/ABC/?definitions";

		//Initializes the matchmaker
		Matchmaker mm = null;
		try {
			TDefinitions nodeTypeDefs = TOSCAUtils.parseDefinitions(nodeTypeLoc);
			mm = new PrototypeMatchmaker(nodeTypeDefs,servTempLoc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		//Computes "candidates"
		List<Candidate> candidates = mm.run();

		if(candidates.size()==0)
			System.err.println("NO CANDIDATES! {Pay attention to the fact that mappings can be empty (e.g., if the target node specifies no properties)}");
		//Prints results
		int i = 0;
		for(Candidate cand : candidates) {
			System.out.println("===CANDIDATE " + i + "===");
			System.out.print("[Fragment]\t");
			for(TEntityTemplate et : cand.getFragment()) {
				System.out.print(et.getId() + ", ");
			}
			System.out.println();
			System.out.println("[Rate]\t" + cand.getRate());
			System.out.println("[Mappings]\t" + "(quantity=" + cand.getMappings().size() + ")");
			for(Mapping m : cand.getMappings()) {
				System.out.print(m.toString());
			}
			i++;
		}
	}
}