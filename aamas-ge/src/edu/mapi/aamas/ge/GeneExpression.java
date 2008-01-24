package edu.mapi.aamas.ge;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.gep.IAamasFitness;
import ec.gep.GEPFitnessFunction2;
import ec.gep.GEPIndividual;
import ec.gep.GEPProblem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

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
				throw new IllegalArgumentException(String
								.format(
										"Evaluation function: Unexpected individual object %s (expecting %s).",
										ind.getClass().getName(),
										GEPIndividual.class.getName()));
		}
	}
	
	
	private void evaluate(EvolutionState state, GEPIndividual ind, int threadnum) {
		if (ind.fitness instanceof IAamasFitness) {

			((IAamasFitness)ind.fitness).computeFitness(ind, state);
			ind.evaluated = true;

		} else
			throw new IllegalArgumentException(String
					.format(
							"Evaluation function: Unexpected fitness object type %s (expecting %s).",
							ind.fitness.getClass().getName(),
							MultiObjectiveFitness.class.getName()));
	}

	
	@SuppressWarnings("unused")
	private void evaluate2(EvolutionState state, GEPIndividual ind, int threadnum) {

		// so we are using three types of fitness: non-triviality, accuracy and number of terminal
		// symbols
		double ntFitness = GEPFitnessFunction2.NTfitness(ind);
		double accFitness = GEPFitnessFunction2.BSACCfitness(ind);
		double ntsFitness = GEPFitnessFunction2.TSfitness(ind);

		Fitness f = ind.fitness;
		// the fitness better be MultiObjectiveFitness!
		if (f instanceof MultiObjectiveFitness) {

			MultiObjectiveFitness mof = ((MultiObjectiveFitness) f);
			// set the fitness measures in order, giving priority to the accuracy 
			mof.multifitness[0] = (float) ntFitness;
			mof.multifitness[1] = (float) accFitness;
			mof.multifitness[2] = (float) ntsFitness;
			ind.evaluated = true;
		
		} else
			throw new IllegalArgumentException(String
							.format(
									"Evaluation function: Unexpected fitness object type %s (expecting %s).",
									f.getClass().getName(),
									MultiObjectiveFitness.class.getName()));
	}
}
