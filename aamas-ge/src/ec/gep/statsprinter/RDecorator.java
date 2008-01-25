package ec.gep.statsprinter;

import java.io.File;
import java.io.IOException;

import ec.EvolutionState;
import ec.Individual;
import ec.gep.GEPIndividual2;
import ec.gep.GEPSymbolSet2;
import ec.gep.IAamasFitness;
import ec.multiobjective.SubsumptionMultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;

/**
 * A class to decorate result printers with functionality for exporting data
 * suitable for later analysis with R, the environment for statistical
 * computing.
 * 
 * @author Rui Meireles
 * 
 */
public class RDecorator extends StatsPrinterDecorator {

	private static final String P_INPUT_FILENAME = "gep.species.symbolset.terminalfilename";
	private static final String P_POP_SIZE = "pop.subpop.0.size";
	private static final String P_NUM_GENERATIONS = "generations";
	
	public static final String P_R = "r";
	/* filename parameter */
	public static final String P_FILE = "file";
	/* runid parameter */
	public static final String P_RUNID = "runid";
	/* r file handler */
	public static int rlog;

	public RDecorator() {
	}

	public RDecorator(IStatsPrinter decoratedResultPrinter) {
		super(decoratedResultPrinter);
	}

	public void printStatistics(EvolutionState state, Individual[] ind) {
		// do my own thing
		myPrintStatistics(state, ind);

		// pass on the torch
		decoratedResultPrinter.printStatistics(state, ind);
	}

	/**
	 * Print the run's best individuals to a file in a CSV file suitable for
	 * processing with R.
	 * 
	 * @param state
	 * @param ind
	 * @param best_of_run_generation
	 * @param result
	 */
	public void myPrintStatistics(EvolutionState state, Individual[] ind) {

		// iterate through all sub-populations (there will be only one, but let
		// us think about the future here)
		for (int x = 0; x < state.population.subpops.length; x++)

			if (ind[x].fitness instanceof IAamasFitness) {
				double accuracy = ((IAamasFitness) ind[x].fitness)
						.getAccuracy();
				String description = ((IAamasFitness) ind[x].fitness).getDescription();
				
				String problem = state.parameters.getString(new Parameter(
						P_INPUT_FILENAME), null);
				long generations = state.parameters.getLong(new Parameter(
						P_NUM_GENERATIONS), null);
				long popsize = state.parameters.getLong(new Parameter(
						P_POP_SIZE), null);
				
				long size = ((GEPIndividual2)ind[x]).size();
				
				
				
				//"Problem\tRun\tGene\tFitness\tOperators\tNGenerations\tPopSize\tGeneration\tAccuracy\tSize\tDepth\n"
				
				state.output.println(String.format("%s\t%d\t%s\t%s\t%s\t%d\t%d\t%d\t%f\t%d\t%s", 
						problem, 
						/*FIXME*/0,
						GEPSymbolSet2.getDependentGene(),
						description,
						"?",
						generations,
						popsize,
						state.generation,
						accuracy,
						size,
						"?"
						
					), Output.V_NO_GENERAL, rlog);

			} else
				throw new IllegalArgumentException(
						String
								.format(
										"Final statistics function: Unexpected fitness object %s (expecting %s).",
										ind[x].fitness.getClass().getName(),
										SubsumptionMultiObjectiveFitness.class
												.getName()));

	}

	public String getDescription() {

		return String.format("%s, R Statistics decorator",
				decoratedResultPrinter.getDescription());
	}

	public void setup(EvolutionState state, Parameter base) {
		// do my own thing
		mySetup(state, base);

		// pass on the torch
		decoratedResultPrinter.setup(state, base);
	}

	/**
	 * Open the file to write the data to (according to the parameter in the
	 * .params file) and write the header.
	 * 
	 * 
	 * @param state
	 * @param base
	 */
	private void mySetup(EvolutionState state, Parameter base) {

		// create the file handler
		File outFile = state.parameters.getFile(base.push(P_DECORATOR)
				.push(P_R).push(P_FILE), null);

		if (outFile != null)
			try {
				rlog = state.output.addLog(outFile, Output.V_NO_GENERAL - 1,
						false, !state.parameters
								.getBoolean(base
										.push(SimpleStatistics.P_COMPRESS),
										null, false), false);
				// outFile, Output.V_NO_GENERAL, false,
				// append);
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log "
								+ outFile + ":\n" + i);
			}

		// write the header
		writeRFileHeader(state);

	}

	/**
	 * Writes the header for the graph file, which is a constant, fixed value.
	 * 
	 * @param state
	 */
	private void writeRFileHeader(EvolutionState state) {
		// Factors: problem, run, gene, fitness, operators, ngenerations,
		// popsize
		// Measures: accuracy, size (tree), depth (tree)
		state.output
				.println(
						"Problem\tRun\tGene\tFitness\tOperators\tNGenerations\tPopSize\tGeneration\tAccuracy\tSize\tDepth",
						Output.V_NO_GENERAL, rlog);
	}

}
