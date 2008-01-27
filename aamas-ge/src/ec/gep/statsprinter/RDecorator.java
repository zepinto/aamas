package ec.gep.statsprinter;

import java.io.File;
import java.io.IOException;

import ec.EvolutionState;
import ec.Individual;
import ec.gep.GEPIndividual;
import ec.gep.GEPIndividual2;
import ec.gep.GEPSymbolSet2;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;
import edu.mapi.aamas.ge.fitness.IFitness;

/**
 * A class to decorate statistics printers with functionality for exporting data
 * suitable for later analysis with R statistical computing environment.
 * 
 * @author Rui Meireles
 * 
 */
public class RDecorator extends StatsPrinterDecorator {

	private static final String P_INPUT_FILENAME = "gep.species.symbolset.terminalfilename";
	private static final String P_TOURNAMENT_SIZE = "select.tournament.size";
	private static final String P_POP_SIZE = "pop.subpop.0.size";
	private static final String P_NUM_GENERATIONS = "generations";

	public static final String P_R = "r";
	/* filename parameter */
	public static final String P_FILE = "file";
	/* runid parameter */
	public static final String P_RUNID = "runid";
	/* r file handler */
	public static int rlog;

	/*
	 * variables whose value don't change during a run are defined here so we
	 * only need to compute them once
	 */
	String problemDesc = null, fitnessDesc = null, operatorsDesc = null;
	long nGenerations = 0, popSize = 0, runid = 0, tournamentSize = 0;

	public RDecorator() {
	}

	public RDecorator(IStatsPrinter decoratedResultPrinter) {
		super(decoratedResultPrinter);
	}

	public void postEvaluationStatistics(EvolutionState state, Individual[] ind) {
		// do my own thing
		myPrintStatistics(state, ind);

		// pass on the torch
		decoratedResultPrinter.postEvaluationStatistics(state, ind);
	}

	public void finalStatistics(EvolutionState state, Individual[] ind) {

		// just pass on the torch
		decoratedResultPrinter.finalStatistics(state, ind);
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

			if (ind[x].fitness instanceof IFitness) {

				String gene = GEPSymbolSet2.getDependentGene();
				String fitnessDesc = ((IFitness) ind[x].fitness)
						.getDescription();
				double accuracy = ((IFitness) ind[x].fitness).getAccuracy();
				long treeSize = ind[x].size();

				// only GEPIndividual2 can give us tree depth information
				long treeDepth = -1;
				if (ind[x] instanceof GEPIndividual)
					treeDepth = ((GEPIndividual2) ind[x]).depth();
				else
					throw new IllegalArgumentException(
							String
									.format(
											"Final statistics function: Unexpected individual object %s (expecting %s).",
											ind[x].getClass().getName(),
											GEPIndividual2.class.getName()));

				// Problem Run Gene Fitness Operators NGenerations PopSize
				// TourSize
				// Generation Accuracy Size Depth
				state.output.println(String.format(
						"%s\t%d\t%s\t%s\t%s\t%d\t%d\t%d\t%d\t%f\t%d\t%s",
						problemDesc, runid, gene, fitnessDesc, operatorsDesc,
						nGenerations, popSize, tournamentSize,
						state.generation + 1, accuracy, treeSize, treeDepth),
						Output.V_NO_GENERAL, rlog);

			} else
				throw new IllegalArgumentException(
						String
								.format(
										"Final statistics function: Unexpected fitness object %s (expecting %s).",
										ind[x].fitness.getClass().getName(),
										IFitness.class.getName()));

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

		// compute variables whose valuable remains constant
		this.problemDesc = state.parameters.getString(new Parameter(
				P_INPUT_FILENAME), null);
		this.runid = state.parameters.getLong(base.push(P_DECORATOR).push(P_R)
				.push(P_RUNID), null);
		this.nGenerations = state.parameters.getLong(new Parameter(
				P_NUM_GENERATIONS), null);
		this.popSize = state.parameters
				.getLong(new Parameter(P_POP_SIZE), null);
		this.tournamentSize = state.parameters.getLong(new Parameter(
				P_TOURNAMENT_SIZE), null);
		this.operatorsDesc = GEPSymbolSet2.getFunctionsDesc();

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
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log "
								+ outFile + ":\n" + i);
			}

		// write the header
		writeRFileHeader(state);
	}

	/**
	 * Writes the header for the R file, which is a constant, fixed value.
	 * 
	 * @param state
	 */
	private void writeRFileHeader(EvolutionState state) {
		// Factors: problem, run, gene, fitness, operators, ngenerations,
		// popsize, toursize
		// Measures: accuracy, size (tree), depth (tree)
		state.output
				.println(
						"Problem\tRun\tGene\tFitness\tOperators\tNGenerations\tPopSize\tTournSize\tGeneration\tAccuracy\tSize\tDepth",
						Output.V_NO_GENERAL, rlog);
	}

}
