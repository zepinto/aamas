package ec.gep;

import java.util.Collection;
import java.util.HashSet;

import ec.EvolutionState;

public class GEPIndividual2 extends GEPIndividual {

	private static final long serialVersionUID = 1L;
	private static final String GRAPHVIZ_SEPARATOR = "->";

	/**
	 * Print an individual in a way that is adequate for using with GraphViz: g1 =
	 * g2 and (g3 or not(g4))
	 * 
	 * @param state
	 * @param log
	 * @param verbosity
	 */
	public void printIndividualForGraph(EvolutionState state, int log,
			int verbosity) {

		/* store all the dependencies */
		HashSet<String> dependencies = new HashSet<String>();

		// make sure we have the parsed expression to work with
		if (parsedGeneExpressions == null)
			parseGenes();

		// compute dependencies for all genomes
		for (GEPExpressionTreeNode parsedGeneExpression : parsedGeneExpressions)
			dependencies
					.addAll(computeDependenciesFromNode(parsedGeneExpression));

		// loop to print all the dependencies we found
		for (String dependency : dependencies)
			state.output.println(String.format("%s%s%s\n",
					GEPDependentVariable.symbol, GRAPHVIZ_SEPARATOR, dependency), verbosity, log);
	}

	/**
	 * Return a Collection with all symbols on
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
    	if (exprNode.symbol instanceof GEPTerminalSymbol)
    	{
    		nodeDependencies.add(((GEPTerminalSymbol)exprNode.symbol).symbol);
        	return nodeDependencies;
    	}
    	// must be a function then...
    	GEPFunctionSymbol fs = (GEPFunctionSymbol)(exprNode.symbol);
    	if (fs.arity == 0)
    		return nodeDependencies;
    	
    	// Recursive call for the function arguments/parameters
    	for (GEPExpressionTreeNode arg : exprNode.parameters)
        	nodeDependencies.addAll(computeDependenciesFromNode(arg));		

		return nodeDependencies;
	}

}
