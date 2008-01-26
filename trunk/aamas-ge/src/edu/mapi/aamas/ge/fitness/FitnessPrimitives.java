package edu.mapi.aamas.ge.fitness;

import ec.gep.GEPDependentVariable;
import ec.gep.GEPExpressionTreeNode;
import ec.gep.GEPFunctionSymbol;
import ec.gep.GEPIndividual;
import ec.gep.GEPSymbolSet2;
import ec.gep.GEPTerminalSymbol;

public class FitnessPrimitives {

	// ************************* BACC (Binary Accuracy) *****************

	/**
	 * Calculates the 'raw' fitness for the ACC (Accuracy) type fitness (before
	 * the normalization from 0 to max value is done).
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * Expect all values to be 0 or 1; for classification threshold value should
	 * be set.
	 * 
	 */
	public static double BACCrawFitness(GEPIndividual ind) {
		double dv[] = GEPDependentVariable.getDependentVariableValues();
		int len = dv.length - 1;
		int confusionMatrix[] = getConfusionMatrixValues(ind);
		int truePositives = confusionMatrix[0];
		int trueNegatives = confusionMatrix[3];

		// the raw fitness ... ACC
		return ((truePositives + trueNegatives) / (double) len);
	}

	/**
	 * Calculates the fitness for the BACC (Binary Accuracy) type fitness. Gets
	 * the raw fitness and then normalized between 0 and max value.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * Expect all values to be 0 or 1; for classification threshold value should
	 * be set.
	 * 
	 */
	public static double BACCfitness(GEPIndividual ind) {
		double ACC = BACCrawFitness(ind);

		// no luck for cases with worse than random performance
		if (ACC <= 0.5)
			return 0;
		else
			// fitness is between 0 and 1
			return (ACC);
	}

	/**
	 * The max value for this type of fitness is always 1.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return value 1.0
	 */
	public static double BACCmaxFitness(GEPIndividual ind) {
		// maximum value is always 1
		return (1.0);
	}

	// ************************* SACC (Squared Accuracy) *****************

	/**
	 * Calculates the fitness for the SACC (Squared Accuracy) type fitness. Gets
	 * the raw fitness and then normalized between 0 and max value.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * Expect all values to be 0 or 1; for classification threshold value should
	 * be set.
	 * 
	 */
	public static double BSACCfitness(GEPIndividual ind) {
		double ACC = BACCrawFitness(ind);

		// no luck for cases with worse than random performance
		if (ACC <= 0.5)
			return 0;
		else
			// fitness is between 0 and 1
			return (ACC * ACC);
	}

	/**
	 * The max value for this type of fitness is always 1.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return value 1.0
	 */
	public static double BSACCmaxFitness(GEPIndividual ind) {
		// maximum value is always 1
		return (1.0);
	}

	// ************************* NTS (Number of Terminal Symbols)
	// *****************

	/**
	 * Calculates the 'raw' fitness for the NTS (Number of Terminal Symbols)
	 * type fitness (before the normalization from 0 to max value is done).
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * 
	 */
	public static double NTSrawFitness(GEPIndividual ind) {
		int tCounter = 0;
		int[] variableUsageCounts = ind.variableUseageCounts();

		// sum it up
		for (int i = 0; i < variableUsageCounts.length; i++)
			tCounter += variableUsageCounts[i];

		// the raw fitness ... NTS (bigger the less symbols we have)
		return (double) (1.0 / tCounter);
	}

	/**
	 * Calculates the fitness for the NTS (Number of Terminal Symbols) type
	 * fitness. Gets the raw fitness and then normalized between 0 and max
	 * value.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * 
	 */
	public static double NTSfitness(GEPIndividual ind) {
		return NTSrawFitness(ind);
	}

	/**
	 * The max value for this type of fitness is always 1.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return value 1.0
	 */
	public static double NTSmaxFitness(GEPIndividual ind) {
		// maximum value is always 1
		return (1.0);
	}

	// ************************* TS (Tree Size) *****************

	/**
	 * Calculates the 'raw' fitness for the TS (Tree Size) type fitness (before
	 * the normalization from 0 to max value is done).
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * 
	 */
	public static double TSrawFitness(GEPIndividual ind) {
		return (double) (1.0 / ind.size());
	}

	/**
	 * Calculates the fitness for the TS (Tree Size) type fitness. Gets the raw
	 * fitness and then normalized between 0 and max value.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * 
	 */
	public static double TSfitness(GEPIndividual ind) {
		return TSrawFitness(ind);
	}

	/**
	 * The max value for this type of fitness is always 1.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return value 1.0
	 */
	public static double TSmaxFitness(GEPIndividual ind) {
		// maximum value is always 1
		return (1.0);
	}

	// ************************* NT (Non-Triviality) *****************

	/**
	 * Calculates the 'raw' fitness for the NT (Non-Triviality) type fitness
	 * (before the normalization from 0 to max value is done).
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * 
	 */
	public static double NTrawFitness(GEPIndividual ind) {

		// make sure we have the parsed expression to work with
		if (ind.parsedGeneExpressions == null)
			ind.parseGenes();

		// compute dependencies for all gene expressions
		for (GEPExpressionTreeNode parsedGeneExpression : ind.parsedGeneExpressions)
			if (isNonTrivial(parsedGeneExpression))
				return 1.0;

		// must be trivial i.e. dependent only on itself
		return 0;
	}

	/**
	 * Returns true iff the variable depends on others and false if it depends
	 * only on itself.
	 * 
	 * @param exprNode
	 * @return
	 */
	private static boolean isNonTrivial(GEPExpressionTreeNode exprNode) {

		// is it a constant node
		if (exprNode.isConstantNode)
			return false;
		// a terminal symbol?
		if (exprNode.symbol instanceof GEPTerminalSymbol)
			if (exprNode.symbol.symbol.equals(GEPSymbolSet2.getDependentGene()))
				return false;
			else
				return true;

		// must be a function then...
		GEPFunctionSymbol fs = (GEPFunctionSymbol) (exprNode.symbol);
		if (fs.arity == 0)
			return false;

		// Recursive call for the function arguments/parameters
		for (GEPExpressionTreeNode arg : exprNode.parameters)
			if (isNonTrivial(arg))
				return true;

		return false;
	}

	/**
	 * Calculates the fitness for the NT (Non-Triviality) type fitness. Gets the
	 * raw fitness and then normalized between 0 and max value.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return the 'raw' fitness value before normalization from 0 to max value
	 * 
	 * 
	 */
	public static double NTfitness(GEPIndividual ind) {
		return NTrawFitness(ind);
	}

	/**
	 * The max value for this type of fitness is always 1.
	 * 
	 * @param ind
	 *            the GEP individual that needs its fitness calculated.
	 * @return value 1.0
	 */
	public static double NTmaxFitness(GEPIndividual ind) {
		// maximum value is always 1
		return (1.0);
	}

	// ******************** Auxiliary Methods ********************

	/**
	 * Given an individual calculate the true positive, false negative, false
	 * positive and true negative values of the model formed by the individual
	 * versus the expected values.
	 * 
	 * The way in that we calculate the matrix is fundamentally different from
	 * what is done in the original GEPFitnessFunction class. Here, we are
	 * trying to compute g(t) as a function of g(t-1). So, in each iteration, we
	 * compare the actual training value at index i with the predicted value
	 * computed as a function of index i - 1.
	 * 
	 * @param ind
	 *            the individual for which we want to calculate the confusion
	 *            matrix values
	 * @return double array with 4 values representing TP FN FP TN respectively
	 */
	public static int[] getConfusionMatrixValues(GEPIndividual ind) {
		double expectedResult;
		double predictedValue;
		double dv[] = GEPDependentVariable.getDependentVariableValues();
		int truePositives = 0;
		int trueNegatives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;
		int len = dv.length;

		// we are starting with i = 1: the first line is to be discarded for it
		// was randomly generated
		for (int i = 1 ; i < len; i++) {
			// the predicted value is calculated using the previous line, not
			// the current one
			predictedValue = ind.eval(i - 1);
			expectedResult = dv[i];
			if (predictedValue == 1.0)
				if (expectedResult == 1.0)
					truePositives++;
				else
					falsePositives += 1;
			else // predicted value is 0
			if (expectedResult == 0.0)
				trueNegatives++;
			else
				falseNegatives++;
		}
		int results[] = new int[] { truePositives, falseNegatives,
				falsePositives, trueNegatives };
		return results;
	}
	
	

}