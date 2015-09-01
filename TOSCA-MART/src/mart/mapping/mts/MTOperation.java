package mart.mapping.mts;

import org.eclipse.winery.model.tosca.TEntityTemplate;
import org.eclipse.winery.model.tosca.TInterface;
import org.eclipse.winery.model.tosca.TOperation;

/**
 * This (utility) class allows to rapidly gain the TEntityTemplate corresponding to the analyzed TOperation.
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class MTOperation {

	/**
	 * (To-Be-)Analyzed TOperation.
	 */
	private TOperation operation;
	/**
	 * Corresponding TInterface.
	 */
	private TInterface interf;
	/**
	 * Corresponding TEntityTemplate.
	 */
	private TEntityTemplate entityTemplate;

	/**
	 * Constructor.
	 *
	 * @param operation (To-Be-)Analyzed TOperation.
	 * @param interf Corresponding TInterface.
	 * @param entityTemplate Corresponding TEntityTemplate.
	 */
	public MTOperation(TOperation operation, TInterface interf,
			TEntityTemplate entityTemplate) {
		super();
		this.operation = operation;
		this.interf = interf;
		this.entityTemplate = entityTemplate;
	}

	public TOperation getOperation() {
		return operation;
	}

	public void setOperation(TOperation operation) {
		this.operation = operation;
	}

	public TInterface getInterface() {
		return interf;
	}

	public void setInterface(TInterface interf) {
		this.interf = interf;
	}

	public TEntityTemplate getEntityTemplate() {
		return entityTemplate;
	}

	public void setEntityTemplate(TEntityTemplate entityTemplate) {
		this.entityTemplate = entityTemplate;
	}



}
