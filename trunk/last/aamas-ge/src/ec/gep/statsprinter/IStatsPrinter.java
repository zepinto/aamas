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
	 * Print the statistics for the actual result.
	 * 
	 * @param state
	 * @param best_of_run
	 * @param best_of_run_generation
	 * @param result
	 */
	public void printStatistics(final EvolutionState state,
			final Individual[] ind);

	/**
	 * Get an human-understandable description of the printer.
	 * 
	 * @return
	 */
	public String getDescription();

}
