package ec.gep;

import ec.EvolutionState;

/**
 * Interface for different fitness functions
 */
public interface IAamasFitness {

	/**
	 * The description to be used in the exported R table
	 * @return The description of the fitness function R<br>Example: <i>?</i>
	 */
	public abstract String getDescription();
	
	/**
	 * Fill-in the individual's fitness
	 * @param individual The individual to be evaluated
	 */
	public void computeFitness(GEPIndividual individual, EvolutionState state);
	
	/**
	 * The accuracy found on the individual
	 * @return A value between 0 (no hits) and 1 (100% hits)
	 */
	public double getAccuracy();
	
}
