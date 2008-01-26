package ec.gep.statsprinter;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

/**
 * A class to abstract an entity that prints the result of a run in any desired
 * format.
 * 
 * @author Rui Meireles
 * 
 */
public interface IStatsPrinter {

	/**
	 * Execute any necessary configuration for the printer.
	 * 
	 * @param state
	 * @param base
	 */
	public void setup(EvolutionState state, Parameter base);

	/**
	 * Print statistics when a new, better solution is found.
	 * 
	 * @param state
	 * @param ind
	 */
	public void postEvaluationStatistics(final EvolutionState state,
			final Individual[] ind);
	
	/**
	 * Print the statistics at the end of the run.
	 * 
	 * @param state
	 * @param ind
	 */
	public void finalStatistics(final EvolutionState state,
			final Individual[] ind);


	/**
	 * Get an human-understandable description of the printer.
	 * 
	 * @return
	 */
	public String getDescription();

}
