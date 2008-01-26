package ec.gep;

import ec.EvolutionState;
import ec.gep.statsprinter.IStatsPrinter;
import ec.gep.statsprinter.SimpleStatsPrinter;
import ec.gep.statsprinter.StatsPrinterDecorator;
import ec.util.Output;
import ec.util.Parameter;
import edu.mapi.aamas.ge.fitness.FitnessPrimitives;

/**
 * Class that extends GEPSimpleStatistics by printing the best individual to an
 * extra graph file.
 * 
 * @author Rui Meireles
 * 
 */
public class GEPSimpleStatistics2 extends GEPSimpleStatistics {

	private static final long serialVersionUID = 1L;

	/* parameter for the number of decorators */
	public static final String P_SIZE = "size";

	// contributes with custom printing functionality
	IStatsPrinter statsPrinter = new SimpleStatsPrinter();

	@Override
	public void setup(EvolutionState state, Parameter base) {

		super.setup(state, base);

		// load the decorators
		// get the size
		int numOfDecorators = state.parameters.getInt(base.push(
				StatsPrinterDecorator.P_DECORATOR).push(P_SIZE), null);

		// decorator loading loop
		for (int i = 0; i < numOfDecorators; i++) {
			StatsPrinterDecorator current = (StatsPrinterDecorator) state.parameters
					.getInstanceForParameter(base.push(
							StatsPrinterDecorator.P_DECORATOR).push("" + i),
							null, StatsPrinterDecorator.class);

			// compose the decorator's structure
			current.setDecoratedResultPrinter(statsPrinter);
			statsPrinter = current;
		}

		// call the decorator's setup so they can initialize themselves
		statsPrinter.setup(state, base);

	}

	@Override
	public void postEvaluationStatistics(EvolutionState state) {
		super.postEvaluationStatistics(state);

		for (int x = 0; x < state.population.subpops.length; x++)
			// only print if a new best was found
			if (best_of_run_generation[x] == state.generation)
				statsPrinter.postEvaluationStatistics(state, best_of_run);
	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {

		super.finalStatistics(state, result);

		// pass on the torch
		statsPrinter.finalStatistics(state, best_of_run);

	}

	/**
	 * Uses getConfusionMatrix from the GEPFitnessFunction2 class.
	 * 
	 * @param state
	 * @param species
	 * @param ind
	 */
	@Override
	void displayStatistics(EvolutionState state, GEPSpecies species,
			GEPIndividual ind) {
		// if classification or boolean problem then show the confusion matrix
		// as well
		if (species.problemType == GEPSpecies.PT_CLASSIFICATION
				|| species.problemType == GEPSpecies.PT_LOGICAL) {
			state.output.println("Confusion Matrix: ", Output.V_NO_GENERAL,
					statisticslog);
			state.output
					.println(
							"                Predicted Value\n               |  Yes\t|  No\n               |----------------",
							Output.V_NO_GENERAL, statisticslog);
			int confusionMatrix[] = FitnessPrimitives
					.getConfusionMatrixValues(ind);
			state.output.println("            Yes|  " + confusionMatrix[0]
					+ "\t|  " + confusionMatrix[1], Output.V_NO_GENERAL,
					statisticslog);
			state.output.println("Actual Value   |----------------",
					Output.V_NO_GENERAL, statisticslog);
			state.output.println("            No |  " + confusionMatrix[2]
					+ "\t|  " + confusionMatrix[3], Output.V_NO_GENERAL,
					statisticslog);
			state.output.println("               |----------------",
					Output.V_NO_GENERAL, statisticslog);
		} else {
			state.output.println("Statistics: ", Output.V_NO_GENERAL,
					statisticslog);
			state.output.println("MSE:  "
					+ GEPFitnessFunction.MSErawFitness(ind) + "    \tRAE:  "
					+ GEPFitnessFunction.RAErawFitness(ind) + "    \tRSE:  "
					+ GEPFitnessFunction.RSErawFitness(ind),
					Output.V_NO_GENERAL, statisticslog);
			state.output.println("RMSE: "
					+ GEPFitnessFunction.RMSErawFitness(ind) + "    \tMAE:  "
					+ GEPFitnessFunction.MAErawFitness(ind) + "    \tRRSE: "
					+ GEPFitnessFunction.RRSErawFitness(ind),
					Output.V_NO_GENERAL, statisticslog);
		}
		state.output.println("", Output.V_NO_GENERAL, statisticslog);
	}
}
