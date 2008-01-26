package ec.gep.statsprinter;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

/**
 * A simple, empty statistics printer that serves as a stepping stone for all other
 * decorators.
 * 
 * @author Rui Meireles
 * 
 */
public class SimpleStatsPrinter implements IStatsPrinter {

	public void postEvaluationStatistics(EvolutionState state, Individual[] ind) {
		// does nothing
	}
	
	public void finalStatistics(EvolutionState state, Individual[] ind) {
		// does nothing
	}

	public String getDescription() {
		return "An ECJ-GEP result printer";
	}

	public void setup(EvolutionState state, Parameter base) {
		// does nothing
	}

}
