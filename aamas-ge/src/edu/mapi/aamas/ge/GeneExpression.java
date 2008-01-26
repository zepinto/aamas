package edu.mapi.aamas.ge;

import ec.EvolutionState;
import ec.Individual;
import ec.gep.GEPIndividual;
import ec.gep.GEPProblem;
import ec.simple.SimpleProblemForm;
import edu.mapi.aamas.ge.fitness.IFitness;

/**
 * The problem here is to find logical expressions that satisfy an input boolean
 * network.
 * 
 * Data and variable names should come from a data file and are therefore not
 * specified here.
 */
public class GeneExpression extends GEPProblem implements SimpleProblemForm {

	private static final long serialVersionUID = 1L;

	public void evaluate(EvolutionState state, Individual ind, int threadnum) {

		if (!ind.evaluated) { // don't bother reevaluating
			if (ind instanceof GEPIndividual) {
				evaluate(state, (GEPIndividual) ind, threadnum);
			} else
				throw new IllegalArgumentException(
						String
								.format(
										"Evaluation function: Unexpected individual object %s (expecting %s).",
										ind.getClass().getName(),
										GEPIndividual.class.getName()));
		}
	}

	private void evaluate(EvolutionState state, GEPIndividual ind, int threadnum) {
		
		if (ind.fitness instanceof IFitness) {
			((IFitness) ind.fitness).computeFitness(ind, state);
			ind.evaluated = true;

		} else
			throw new IllegalArgumentException(
					String
							.format(
									"Evaluation function: Unexpected fitness object type %s (expecting %s).",
									ind.fitness.getClass().getName(),
									IFitness.class.getName()));
	}
}
