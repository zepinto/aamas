package ec.gep.statsprinter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import ec.EvolutionState;
import ec.Individual;
import ec.gep.GEPExpressionTreeNode;
import ec.gep.GEPFunctionSymbol;
import ec.gep.GEPIndividual;
import ec.gep.GEPSymbolSet2;
import ec.gep.GEPTerminalSymbol;
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

	private static final String P_INPUT_FILENAME = "gep.species.symbolset.terminalfilename";
	/* Graphviz identifier parameter */
	public static final String P_GRAPHVIZ = "graphviz";
	/* graph filename parameter */
	public static final String P_FILE = "file";
	/* string used to denote dependency in the Graphviz format */
	private static final String GRAPHVIZ_SEPARATOR = "->";
	/* graph log handler */
	public static int graphlog;

	public GraphvizDecorator() {

	}

	public GraphvizDecorator(IStatsPrinter decoratedResultPrinter) {
		super(decoratedResultPrinter);
	}

	public void postEvaluationStatistics(EvolutionState state, Individual[] ind) {
		// pass on the torch
		decoratedResultPrinter.postEvaluationStatistics(state, ind);
	}

	public void finalStatistics(EvolutionState state, Individual[] ind) {
		// do my own thing
		myPrintStatistics(state, ind);

		// pass on the torch
		decoratedResultPrinter.finalStatistics(state, ind);
	}

	/**
	 * Print the run's best individuals to a file in the Graphviz format.
	 * 
	 * @param state
	 * @param ind
	 */
	public void myPrintStatistics(EvolutionState state, Individual[] ind) {

		// iterate through all sub-populations (there will be only one, but let
		// us think about the future here)
		for (int x = 0; x < state.population.subpops.length; x++) {
			if (ind[x] instanceof GEPIndividual)
				printIndividualForGraph(state, ((GEPIndividual) ind[x]),
						graphlog, Output.V_NO_GENERAL);
			else
				throw new IllegalArgumentException(
						String
								.format(
										"Evaluation function: Unexpected individual object %s (expecting %s).",
										ind.getClass().getName(),
										GEPIndividual.class.getName()));
		}

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
		state.output.println(String.format("digraph \"%s\" {\n",
				state.parameters.getString(new Parameter(P_INPUT_FILENAME),
						null)), Output.V_NO_GENERAL, graphlog);
	}

	/**
	 * Writes the footer for the graph file, which is a constant, fixed value.
	 * 
	 * @param state
	 */
	private void writeGraphFileFooter(EvolutionState state) {
		state.output.println("}\n", Output.V_NO_GENERAL, graphlog);
	}

	/**
	 * Print an individual in a way that is adequate for using with GraphViz: g1 =
	 * g2 and (g3 or not(g4))
	 * 
	 * @param state
	 * @param log
	 * @param verbosity
	 */
	public void printIndividualForGraph(EvolutionState state,
			GEPIndividual ind, int log, int verbosity) {

		/* store all the dependencies */
		HashSet<String> dependencies = new HashSet<String>();

		// make sure we have the parsed expression to work with
		if (ind.parsedGeneExpressions == null)
			ind.parseGenes();

		// compute dependencies for all genomes
		for (GEPExpressionTreeNode parsedGeneExpression : ind.parsedGeneExpressions)
			dependencies
					.addAll(computeDependenciesFromNode(parsedGeneExpression));

		// loop to print all the dependencies we found
		for (String dependency : dependencies)
			state.output.println(String.format("%s%s%s\n", GEPSymbolSet2
					.getDependentGene(), GRAPHVIZ_SEPARATOR, dependency),
					verbosity, log);
	}

	/**
	 * Return a Collection with all symbols on
	 * 
	 * @param exprNode
	 * @return
	 */
	private Collection<? extends String> computeDependenciesFromNode(
			GEPExpressionTreeNode exprNode) {

		HashSet<String> nodeDependencies = new HashSet<String>();

		// is it a constant node
		if (exprNode.isConstantNode)
			return nodeDependencies;
		// a terminal symbol?
		if (exprNode.symbol instanceof GEPTerminalSymbol) {
			nodeDependencies.add(((GEPTerminalSymbol) exprNode.symbol).symbol);
			return nodeDependencies;
		}
		// must be a function then...
		GEPFunctionSymbol fs = (GEPFunctionSymbol) (exprNode.symbol);
		if (fs.arity == 0)
			return nodeDependencies;

		// Recursive call for the function arguments/parameters
		for (GEPExpressionTreeNode arg : exprNode.parameters)
			nodeDependencies.addAll(computeDependenciesFromNode(arg));

		return nodeDependencies;
	}

}
