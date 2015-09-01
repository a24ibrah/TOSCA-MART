package greedy.candidate;

import greedy.mapping.Mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.winery.model.tosca.TDefinitions;
import org.eclipse.winery.model.tosca.TEntityTemplate;

/**
 * This class implements the Java representation of a "Candidate"
 *
 * @see greedy.GreedyMatching
 *
 * @author Jacopo Soldani (CS Department, University of Pisa)
 *
 */
public class Candidate {

	/**
	 * This field refers to the Definitions containing the TopologyTemplate from which the fragment is excerpted.
	 */
	private TDefinitions startingTopology;
	/**
	 * This field contains the Set of EntityTemplate(s) composing the candidate fragment.
	 */
	private Set<TEntityTemplate> fragment;
	/**
	 * This field contains the rating of the corresponding candidate.
	 */
	private double rate;
	/**
	 * This field stores the set of possible mappings between the required features and those offered by the fragment.
	 */
	private Set<Mapping> mappings;

	/**
	 * Constructor.
	 *
	 * @param startingTopology TDefinitions containing the TopologyTemplate from which the fragment is excerpted.
	 * @param fragment Set of EntityTemplate(s) composing the candidate fragment.
	 * @param mapping One of the possible mappings between the required features and those offered by the fragment.
	 */
	public Candidate(TDefinitions startingTopology,
			Set<TEntityTemplate> fragment,
			Mapping mapping) {
		super();
		this.startingTopology = startingTopology;
		this.fragment = fragment;
		this.rate = -1;
		this.mappings = new HashSet<Mapping>();
		mappings.add(mapping);
	}

	/**
	 * This method allows to read the rate scored by the corresponding Candidate.
	 * @return Rate of the corresponding candidate.
	 */
	public double getRate() {
		return rate;
	}

	/**
	 * This method allows to set the rate scored by the corresponding Candidate.
	 * @param rate Rate of the corresponding candidate.
	 */
	public void setRate(double rate) {
		this.rate = rate;
	}

	/**
	 * This method allows to access the TDefinitions containing the TopologyTemplate from which the fragment is excerpted.
	 * @return TDefinitions containing the TopologyTemplate from which the fragment is excerpted.
	 */
	public TDefinitions getStartingTopology() {
		return startingTopology;
	}

	/**
	 * This method allows to access the Set of EntityTemplate(s) composing the candidate fragment.
	 * @return Set of EntityTemplate(s) composing the candidate fragment.
	 */
	public Set<TEntityTemplate> getFragment() {
		return fragment;
	}

	/**
	 * This method allows to access the Set of Mapping(s) allowed by the corresponding candidate fragment.
	 * @return Set of Mapping(s) allowed by the candidate fragment.
	 * @see Mapping
	 */
	public Set<Mapping> getMappings() {
		return mappings;
	}

	/**
	 * This method allows to add a Mapping to those already allowed by the candidate fragment.
	 * @param mapping Mapping to be added.
	 * @return "true" if the Set of Mapping(s) does not already contains the input "mapping", "false" otherwise.
	 */
	public boolean addMapping(Mapping mapping) {
		return this.mappings.add(mapping);
	}

}
