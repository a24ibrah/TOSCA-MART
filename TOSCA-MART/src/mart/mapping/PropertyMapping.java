package mart.mapping;

import mart.mapping.mts.MTProperty;

import org.w3c.dom.Element;

/**
 * This class models the mapping between a required property and an available property.
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class PropertyMapping {

	/**
	 * Required property.
	 */
	private Element nodeProp;
	/**
	 * Available property.
	 */
	private MTProperty servProp;

	/**
	 * Constructor.
	 * @param nodeProp Required property.
	 * @param servProp Available property.
	 */
	public PropertyMapping(Element nodeProp, MTProperty servProp) {
		super();
		this.nodeProp = nodeProp;
		this.servProp = servProp;
	}

	/**
	 * This method allows to read the required property.
	 * @return Required property.
	 */
	public Element getNodeProp() {
		return nodeProp;
	}

	/**
	 * This method allows to modify the required property.
	 * @param nodeProp Required property.
	 */
	public void setNodeProp(Element nodeProp) {
		this.nodeProp = nodeProp;
	}

	/**
	 * This method allows to read the available property.
	 * @return Available property.
	 */
	public MTProperty getServProp() {
		return servProp;
	}

	/**
	 * This method allows to modify the available property.
	 * @param servProp Available property.
	 */
	public void setServProp(MTProperty servProp) {
		this.servProp = servProp;
	}


}
