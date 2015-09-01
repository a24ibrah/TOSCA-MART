package tosca.util;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.eclipse.winery.model.tosca.ObjectFactory;
import org.eclipse.winery.model.tosca.TCapabilityType;
import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityType;
import org.eclipse.winery.model.tosca.TExtensibleElements;
import org.eclipse.winery.model.tosca.TImport;

public class TOSCAUtils {

	/**
	 * This field is needed to store the JAXB context needed for parsing/writing
	 * TOSCA specifications.
	 */
	private static JAXBContext context = null;
	
	/**
	 * This field is needed to recursively inspect the hierarchy of "derivedFrom".
	 * Thus, if the user needs to recursively inspect such hierarchies, an instance of
	 * "TOSCAUtils" must be created (by specifying the initial TDefinitions).
	 */
	private TDefinitions lastAccessedDefinitions;

	public TOSCAUtils(TDefinitions initialTDefinitions) {
		this.lastAccessedDefinitions = initialTDefinitions;
	}

	/**
	 * This method is used to retrieve the definition of the type of entity template corresponding to the specified "entityRef".
	 *
	 * @param entityRef QName of the entity template whose type is under research.
	 *
	 * @return the "TEntityType" of "entityRef", if found. "null", otherwise.
	 */
	public TEntityType retrieveType(QName entityRef) {
		TEntityType entityType = null;
		for(TImport imp : lastAccessedDefinitions.getImport()) {
			//UPDATED! "&& imp.getLocation().contains(entityRef.getLocalPart())" allows us to reduce the number of TDefinitions to be parsed to only 1!
			if(entityRef.getNamespaceURI().equals(imp.getNamespace()) && imp.getLocation().contains(entityRef.getLocalPart())) {
				TDefinitions impDefinition = null;
				impDefinition = parseDefinitions(imp.getLocation());
				if(impDefinition!=null){
					for (TExtensibleElements element : impDefinition.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
						if(element instanceof TEntityType) {
							TEntityType type = (TEntityType) element;
							//The analyzed tDefinitions are those containing the desired type
							// -> save type and corresponding tDefinitions
							if(type.getName().equals(entityRef.getLocalPart())) {
								entityType = type;
								lastAccessedDefinitions = impDefinition;
							}
						}
					}
				}
			}
		}
		return entityType;
	}

	/**
	 * This method is used to retrieve the "TDefinitions" at the specified "location", if present.
	 *
	 * @param location URL from which the "TDefinitions" should be retrieved.
	 *
	 * @return the parsed "TDefinitions", if present. "null", otherwise.
	 */
	public static TDefinitions parseDefinitions(String location) {
		//JAXBContext impContext = null;
		TDefinitions defs;
		try {
			// URL to load
			URL url = new URL(location);
			// Load TOSCA using org.eclipse.winery.model.tosca-0.1.12.jar
			//impContext = JAXBContext.newInstance(ObjectFactory.class);
			//Unmarshaller unmarshaller = impContext.createUnmarshaller();
			if(context==null)
				context = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			defs = (TDefinitions) unmarshaller.unmarshal(url.openStream());
		} catch(Exception e) {
			defs = null;
		}
		return defs;
	}

	/**
	 * This method is used to write an "output" XML document containing the definitions in "tDefinitions".
	 *
	 * @param tDefinitions Definitions to be converted into XML.
	 * @param output File name where to write the conversion of "tDefinitions".
	 * @return "true", if the conversion takes place; "false", otherwise.
	 */
	public static boolean writeDefinitions(TDefinitions tDefinitions, String output){
		try {
			if(context==null)
				context = JAXBContext.newInstance(ObjectFactory.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(tDefinitions, new File(output));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * This method is used to create a new TOSCA Definitions element.
	 *
	 * @return A new TOSCA Definitions element.
	 */
	public static TDefinitions createDefinitions() {
		ObjectFactory oFactory = new ObjectFactory();
		return oFactory.createDefinitions();
	}

	public static void main(String[] args) {
		// (TEST 1) parseDefinitions
		TDefinitions tDefinitions = null;
		tDefinitions = TOSCAUtils.parseDefinitions("http://dev.winery.opentosca.org/winery/servicetemplates/http%253A%252F%252Fwww.prova.it/ABC/?definitions");

		// (TEST2) writeDefinitions
		TOSCAUtils.writeDefinitions(tDefinitions, "temp.xml");

		// (TEST3) retrieveType
		tDefinitions = parseDefinitions("http://dev.winery.opentosca.org/winery/capabilitytypes/http%253A%252F%252Fdocs.oasis-open.org%252Ftosca%252Fns%252F2011%252F12%252FToscaSpecificTypes/ApacheModuleContainerCapability/?definitions");
		for(TExtensibleElements element : tDefinitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if(element instanceof TCapabilityType) {
				TCapabilityType cType = (TCapabilityType) element;
				System.out.println("CapabilityType = " + cType.getName());
				TOSCAUtils tu = new TOSCAUtils(tDefinitions);
				while(cType.getDerivedFrom()!=null) {
					cType = (TCapabilityType) tu.retrieveType(cType.getDerivedFrom().getTypeRef());
					System.out.println("  |__ DerivedFrom: " + cType.getName());
				}
			}
		}
	}
}
