package mart.mapping.mts;

import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.w3c.dom.Element;

/**
 * This (utility) class allows to rapidly gain the TEntityTemplate corresponding to the analyzed property.
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class MTProperty {

	/**
	 * (To-Be-)Analyzed property.
	 */
	private Element property;
	/**
	 * Corresponding TEntityTemplate.
	 */
	private TEntityTemplate entityTemplate;

	/**
	 * Constructor.
	 *
	 * @param property (To-Be-)Analyzed property.
	 * @param entityTemplate Corresponding TEntityTemplate.
	 */
	public MTProperty(Element property, TEntityTemplate entityTemplate) {
		super();
		this.property = property;
		this.entityTemplate = entityTemplate;
	}

	/**
	 * This method allows to retrieve the (to-be-)analyzed property.
	 * @return (To-Be-)Analyzed property.
	 */
	public Element getProperty() {
		return property;
	}

	/**
	 * This method allows to modify the (to-be-)analyzed property.
	 * @param property (To-Be-)Analyzed property.
	 */
	public void setProperty(Element property) {
		this.property = property;
	}

	/**
	 * This method allows to retrieve the corresponding TEntityTemplate.
	 * @return Corresponding TEntityTemplate.
	 */
	public TEntityTemplate getEntityTemplate() {
		return entityTemplate;
	}

	/**
	 * This method allows to modify the corresponding TEntityTemplate.
	 * @param entityTemplate Corresponding TEntityTemplate
	 */
	public void setEntityTemplate(TEntityTemplate entityTemplate) {
		this.entityTemplate = entityTemplate;
	}

}
