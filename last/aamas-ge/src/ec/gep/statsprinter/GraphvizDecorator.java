package ec.gep.statsprinter;

import java.io.File;
import java.io.IOException;

import ec.EvolutionState;
import ec.Individual;
import ec.gep.GEPIndividual2;
import ec.gep.GEPSymbolSet2;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;

/**
 * A class to decorate result printers with Graphviz functionality.
 * 
 * @author Rui Meireles
 * 
 */
public class GraphvizDecorator extends StatsPrinterDecorator {

	/* Graphviz identifier parameter */
	public static final String P_GRAPHVIZ = "graphviz";
	/* graph filename parameter */
	public static final String P_FILE = "file";
	/* graph log handler */
	public static int graphlog;

	public GraphvizDecorator() {

	}

	public GraphvizDecorator(IStatsPrinter decoratedResultPrinter) {
		super(decoratedResultPrinter);
	}
	

	public void printStatistics(EvolutionState state, Individual[] ind) {
		// do my own thing
		myPrintStatistics(state, ind);

		// pass on the torch
		decoratedResultPrinter.printStatistics(state, ind);
	}

	/**
	 * Print the run's best individuals to a file in the Graphviz format.
	 * 
	 * @param state
	 * @param ind
	 */
	public void myPrintStatistics(EvolutionState state,
			Individual[] ind) {

		// iterate through all sub-populations (there will be only one, but let
		// us think about the future here)
		for (int x = 0; x < state.population.subpops.length; x++)
			if (ind[x] instanceof GEPIndividual2) {
				((GEPIndividual2) ind[x]).printIndividualForGraph(
						state, graphlog, Output.V_NO_GENERAL);
			} else
				throw new IllegalArgumentException(
						String
								.format(
										"Final statistics function: Unexpected individual object %s (expecting %s).",
										ind[x].getClass().getName(),
										GEPIndividual2.class.getName()));

		// print the footer in the last iteration
		if (GEPSymbolSet2.isComplete())
			writeGraphFileFooter(state);

	}

	public String getDescription() {

		return String.format("%s, Graphviz decorator", decoratedResultPrinter
				.getDescription());
	}

	public void setup(EvolutionState state, Parameter base) {
		// do my own thing
		mySetup(state, base);

		// pass on the torch
		decoratedResultPrinter.setup(state, base);
	}

	/**
	 * Open the file to write the graph to (according to the parameter in the
	 * .params file) and write the header.
	 * 
	 * @param state
	 * @param base
	 */
	private void mySetup(EvolutionState state, Parameter base) {
		// create the graph file handler
		File graphFile = state.parameters.getFile(base.push(P_DECORATOR).push(
				P_GRAPHVIZ).push(P_FILE), null);

		if (graphFile != null)
			try {
				graphlog = state.output.addLog(graphFile,
						Output.V_NO_GENERAL - 1, false, !state.parameters
								.getBoolean(base
										.push(SimpleStatistics.P_COMPRESS),
										null, false), false);
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log "
								+ graphFile + ":\n" + i);
			}

		// write the graph file header
		writeGraphFileHeader(state);

	}

	/**
	 * Writes the header for the graph file, which is a constant, fixed value.
	 * 
	 * @param state
	 */
	private void writeGraphFileHeader(EvolutionState state) {
		state.output
				.println("digraph \"g\" {\n", Output.V_NO_GENERAL, graphlog);
	}

	/**
	 * Writes the footer for the graph file, which is a constant, fixed value.
	 * 
	 * @param state
	 */
	private void writeGraphFileFooter(EvolutionState state) {
		state.output.println("}\n", Output.V_NO_GENERAL, graphlog);
	}

}
