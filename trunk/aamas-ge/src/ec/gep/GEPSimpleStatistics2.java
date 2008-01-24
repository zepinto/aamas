package ec.gep;

import ec.EvolutionState;
import ec.gep.resultprinter.ResultPrinter;
import ec.gep.resultprinter.ResultPrinterDecorator;
import ec.gep.resultprinter.SimpleResultPrinter;
import ec.util.Output;
import ec.util.Parameter;

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
	ResultPrinter resultPrinter = new SimpleResultPrinter();

	@Override
	public void setup(EvolutionState state, Parameter base) {

		super.setup(state, base);

		// load the decorators
		// get the size
		int numOfDecorators = state.parameters.getInt(base.push(
				ResultPrinterDecorator.P_DECORATOR).push(P_SIZE), null);

		// decorator loading loop
		for (int i = 0; i < numOfDecorators; i++) {
			ResultPrinterDecorator current = (ResultPrinterDecorator) state.parameters
					.getInstanceForParameter(base.push(
							ResultPrinterDecorator.P_DECORATOR).push("" + i),
							null, ResultPrinterDecorator.class);

			// compose the decorator's structure
			current.setDecoratedResultPrinter(resultPrinter);
			resultPrinter = current;
		}

		// call the decorator's setup so they can initialize themselves
		resultPrinter.setup(state, base);

	}

	@Override
	public void finalStatistics(final EvolutionState state, final int result) {

		super.finalStatistics(state, result);

		// call the decorator's setup so they can initialize themselves
		resultPrinter.finalStatistics(state, best_of_run,
				best_of_run_generation, result);

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
			int confusionMatrix[] = GEPFitnessFunction2
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
