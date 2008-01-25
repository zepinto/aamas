package ec.gep;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.csvreader.CsvReader;

import ec.EvolutionState;
import ec.gep.GEPConstantTerminalSymbol;
import ec.gep.GEPDependentVariable;
import ec.gep.GEPFunctionSymbol;
import ec.gep.GEPProblem;
import ec.gep.GEPSpecies;
import ec.gep.GEPSymbol;
import ec.gep.GEPSymbolSet;
import ec.gep.GEPTerminalSymbol;
import ec.util.Parameter;
import ec.util.RandomChoice;

public class GEPSymbolSet2 extends GEPSymbolSet {

	private static final long serialVersionUID = 1L;
	/* indicates if all options for dependent variables have been explored */
	private static boolean isComplete = false;
	/* saves the current index value for the dependent value */
	private static int dependentVarIdx = -1;

	public static void reset() {
		isComplete = false;
		dependentVarIdx = -1;
	}

	/**
	 * Returns true if all options for dependent variables have been explored
	 * and false otherwise.
	 * 
	 * @return isComplete
	 */
	public static boolean isComplete() {
		return isComplete;
	}
	
	public static String getDependentGene() {
		return "G"+dependentVarIdx;
	}

	/**
	 * Computes the index for the next dependent variable.
	 * 
	 * @param maxIndex
	 * @return
	 */
	private int getDependentVarIndex(int maxIndex) {

		dependentVarIdx++;

		// meaning this is the last iteration
		if (maxIndex - dependentVarIdx < 1)
			isComplete = true;

		return dependentVarIdx < maxIndex ? dependentVarIdx : maxIndex;
	}

	/**
	 * Sets up all the GEPSymbolSet symbols, loading them from the parameter
	 * file. Enhanced: sequential calls will result in different dependent
	 * variables being chosen.
	 */
	@SuppressWarnings("unchecked")
	public void setup(final EvolutionState state, final Parameter base,
			final Parameter def, GEPSpecies species) {

		// Rui
		int depVarIdx = 0;

		// Name of file with the terminal (variable) definitions and training
		// values
		String terminalFilename;
		// Name of file with the test data values if specified
		String testingTerminalFilename;

		// keep track of the maximum arity of any function
		maxArity = 0;

		// What's my name? Don't really use this at this time ...
		name = state.parameters.getString(base.push(P_NAME), def.push(P_NAME));
		if (name == null || name.equals(""))
			state.output
					.warning(
							"No name was given for this GEP symbol set...not required at this time.",
							base.push(P_NAME), def.push(P_NAME));

		// How many functions do I have?
		numberOfFunctions = state.parameters.getInt(base.push(P_FUNCTIONSIZE),
				def.push(P_FUNCTIONSIZE), 1);
		numberOfSymbols = numberOfFunctions;

		// How many terminals do I have? Check for a data file first ...
		// if time series problem type and using raw time series data then
		// number of terminals will be specified in the embedding dimension
		// value
		// provided in the parameter file
		// else if a file specified
		// get the 1st line of the file and count the fields in it
		// else
		// use the number of terminals specified in the parameter file

		terminalFilename = state.parameters.getStringWithDefault(base
				.push(P_TERMINALFILENAME), def.push(P_TERMINALFILENAME), "");
		testingTerminalFilename = state.parameters.getStringWithDefault(base
				.push(P_TESTINGTERMINALFILENAME), def
				.push(P_TESTINGTERMINALFILENAME), "");
		String terminalSymbolsfromFile[] = null;
		CsvReader terminalFileCSV = null;
		CsvReader testingTerminalFileCSV = null;
		// Are we processing raw time series data?
		boolean timeseriesWithRawDataValues = species.problemType == GEPSpecies.PT_TIMESERIES
				&& species.timeseriesEmbeddingDimension > 0;
		if (!terminalFilename.equals("")) {
			try {
				terminalFileCSV = new CsvReader(terminalFilename);
			} catch (FileNotFoundException e) {
				state.output.fatal(
						"The file with terminal definitions and/or values ("
								+ terminalFilename + ") could not be found",
						base.push(P_TERMINALFILENAME), def
								.push(P_TERMINALFILENAME));
			}
			// if using a file for the terminals and their values then check for
			// a non-default separator
			String terminalFileSeparator = state.parameters
					.getStringWithDefault(base.push(P_TERMINALFILESEPARATOR),
							def.push(P_TERMINALFILESEPARATOR), ",");
			if (terminalFileSeparator.toLowerCase().equals("comma"))
				terminalFileSeparator = ",";
			else if (terminalFileSeparator == "\\t"
					|| terminalFileSeparator.toLowerCase().equals("tab"))
				terminalFileSeparator = "\t";
			terminalFileCSV.setDelimiter(terminalFileSeparator.charAt(0));
			// let's check for a testing dat file at this time as well .. if no
			// file for
			// names and training data no need to worry about this one.
			if (!testingTerminalFilename.equals("")) {
				try {
					testingTerminalFileCSV = new CsvReader(
							testingTerminalFilename);
					testingTerminalFileCSV.setDelimiter(terminalFileSeparator
							.charAt(0));
				} catch (FileNotFoundException e) {
					state.output.fatal("The file with testing data values ("
							+ testingTerminalFilename + ") could not be found",
							base.push(P_TERMINALFILENAME), def
									.push(P_TERMINALFILENAME));
				}
			}
		}

		if (timeseriesWithRawDataValues)
			numberOfTerminals = species.timeseriesEmbeddingDimension;
		else if (terminalFileCSV != null) {
			// get the terminal symbols for the independent and dependent
			// variables
			try {
				terminalFileCSV.readHeaders();
				terminalSymbolsfromFile = terminalFileCSV.getHeaders();
				// Rui: set dependent variable index
				depVarIdx = getDependentVarIndex(terminalSymbolsfromFile.length - 1);
			} catch (IOException e) {
				state.output.fatal(
						"The file with variable (terminal) definitions and values ("
								+ terminalFilename
								+ ") failed to read the headers" + e, base
								.push(P_TERMINALFILENAME), def
								.push(P_TERMINALFILENAME));
			}
			// Rui: was length - 1 but now we want to be able to make a gene
			// depend on its own value
			numberOfTerminals = terminalSymbolsfromFile.length;
			if (numberOfTerminals < 1)
				state.output
						.fatal(
								"The file with terminal definitions and data values ("
										+ terminalFilename
										+ ") has no independent variables specified in record 1",
								base.push(P_TERMINALFILENAME), def
										.push(P_TERMINALFILENAME));
			// if using a file for the terminals and their values then check for
			// a non-default separator
		} else {
			numberOfTerminals = state.parameters.getInt(base
					.push(P_TERMINALSIZE), def.push(P_TERMINALSIZE), 1);
		}
		numberOfSymbols += numberOfTerminals;

		if (numberOfSymbols < 1)
			state.output.error("The GEPSymbolSet \"" + name
					+ "\" have at least 1 terminal symbol defined.", base
					.push(P_TERMINALSIZE), def.push(P_TERMINALSIZE));

		// add a special Symbol for constants if we are using them ... it will
		// be added to the
		// end of the array of symbols!
		if (species.useConstants) {
			numberOfTerminals++; // special constant terminal
			numberOfSymbols++;
		}

		symbols = new GEPSymbol[numberOfSymbols];

		int numberOfSymbolsWithoutConstantSymbol = numberOfSymbols;
		if (species.useConstants) // add the constant terminal symbol to the
		// end
		{
			symbols[numberOfSymbols - 1] = (GEPSymbol) (new GEPConstantTerminalSymbol());
			symbols[numberOfSymbols - 1].id = numberOfSymbols - 1;
			numberOfSymbolsWithoutConstantSymbol--;
		}

		Parameter pTerminal = base.push(P_TERMINAL);
		Parameter pdefTerminal = def.push(P_TERMINAL);
		Parameter pFunction = base.push(P_FUNCTION);
		Parameter pdefFunction = def.push(P_FUNCTION);

		// create hashtable of names of terminals and hash table with names of
		// functions
		// so we can easily check that they are not duplicates
		Hashtable<String, String> functionHT = new Hashtable<String, String>();
		Hashtable<String, String> terminalHT = new Hashtable<String, String>();

		// process the functions
		for (int x = 0; x < numberOfFunctions; x++) {
			Parameter pp = pFunction.push("" + x);
			Parameter ppdef = pdefFunction.push("" + x);
			String function = state.parameters.getStringWithDefault(pp, ppdef,
					"");
			if (function.equals("")) // no name for the function
				state.output.fatal("Invalid function specifier: '" + function
						+ "'", pp, ppdef);
			// make sure not specifying the same function more than once
			if (functionHT.get(function) != null)
				state.output
						.fatal("Function '"
								+ function
								+ "' was specified more than once in list of function symbols");
			else
				functionHT.put(function, function);
			GEPFunctionSymbol fs = null;
			try {
				Class classDefinition = Class
						.forName(LOCATION_OF_FUNCTION_CLASSES + "." + function);
				fs = (GEPFunctionSymbol) classDefinition.newInstance();
			} catch (InstantiationException e) {
				state.output
						.fatal("Unable to create GEPFunctionSymbol class for function '"
								+ function + "'. " + e);
			} catch (IllegalAccessException e) {
				state.output
						.fatal("Unable to create GEPFunctionSymbol class for function '"
								+ function + "' " + e);
			} catch (ClassNotFoundException e) {
				state.output
						.fatal("Unable to create GEPFunctionSymbol class for function '"
								+ function + "' " + e);
			}

			// if using a logical function must be a logical problem
			if (fs.isLogicalFunction()
					&& (species.problemType != GEPSpecies.PT_LOGICAL))
				state.output.fatal(
						"Can only use logical functions with a logical problem type. Function "
								+ function + " is  a logical function.", pp,
						ppdef);
			// if using a numerical function must be an non logical problem
			if (!fs.isLogicalFunction()
					&& (species.problemType == GEPSpecies.PT_LOGICAL))
				state.output.fatal(
						"Can only use logical functions with a non logical problem type. Function "
								+ function + " is a numerical function.", pp,
						ppdef);

			symbols[x] = (GEPSymbol) fs;
			// symbols[x].setup(state, base);
			if (fs.arity < 1)
				state.output
						.fatal("Arity must be > 0 for a GEPTerminalSymbol)",
								pp, ppdef);
			symbols[x].id = x;
			int weight = state.parameters.getInt(pp.push(P_FUNCTIONWEIGHT),
					ppdef.push(P_FUNCTIONWEIGHT), 1);
			if (weight < 1) {
				state.output
						.warning(
								"Weight for GEP Function must be > 0; defaulting to 1)",
								pp.push(P_FUNCTIONWEIGHT), ppdef
										.push(P_FUNCTIONWEIGHT));
				weight = 1;
			}
			symbols[x].weight = weight;
			if (symbols[x].arity > maxArity)
				maxArity = symbols[x].arity;
		}

		// process the terminals ... defined by default for timeseries data, in
		// the
		// CSV file if specified and not timeseries, or in the params file if
		// neither of those.
		for (int x = numberOfFunctions; x < numberOfSymbolsWithoutConstantSymbol; x++) { // load
			// the
			// terminal
			// symbols

			int index = x - numberOfFunctions;

			String terminal = "";
			if (timeseriesWithRawDataValues) {
				// terminals get default names v0, v1, v2, v3, ... vn-1
				terminal = "v" + index;
			} else if (terminalFileCSV == null)// terminals defined in param
			// file
			{
				Parameter pp = pTerminal.push("" + index);
				Parameter ppdef = pdefTerminal.push("" + index);
				terminal = state.parameters.getStringWithDefault(pp, ppdef, "");
			} else { // terminals defined in CSV file
				terminal = terminalSymbolsfromFile[index];
			}
			if (terminal.equals("")) // no name for the terminal
				state.output.fatal("Invalid terminal specifier: '" + terminal
						+ "' for terminal # " + index);
			// make sure not specifying the same function more than once
			if (terminalHT.get(terminal) != null)
				state.output
						.fatal("Terminal symbol (indep var) '"
								+ terminal
								+ "' was specified more than once in list of terminal symbols (independent variables)");
			else
				terminalHT.put(terminal, terminal);
			GEPTerminalSymbol ts = new GEPTerminalSymbol(terminal, this);
			symbols[x] = (GEPSymbol) ts;
			// symbols[x].setup(state, base);
			if (ts.arity != 0) // cannot happen
				state.output
						.fatal("Arity must be exactly 0 for a GEPTerminalSymbol)");
			symbols[x].id = x;
			symbols[x].weight = 1; // all Terminal symbols have weight of 1
		}

		// must be at least 1 Terminal symbol in the SymbolSet.
		if (numberOfTerminals < 1)
			state.output
					.fatal("Must be at least one Terminal Symbol in the set of GEPSymbols");

		// collect the id's (indices) of the terminal and function symbols that
		// are in the set of symbols
		terminals = new int[numberOfTerminals];
		int terminalNum = 0;
		functions = new int[numberOfFunctions];
		int functionNum = 0;
		for (int x = 0; x < numberOfSymbols; x++) {
			if (symbols[x] instanceof GEPConstantTerminalSymbol)
				terminals[terminalNum++] = x;
			else if (symbols[x] instanceof GEPTerminalSymbol)
				terminals[terminalNum++] = x;
			else if (symbols[x] instanceof GEPFunctionSymbol)
				functions[functionNum++] = x;
		}

		// collect the weights for symbols and terminals and normalize and
		// cumulate them.
		// Then we can use these arrays to pick appropriate symbols or terminals
		// according to
		// their weights ... using the RandomChooser.PickFromDistribution
		cumulativeNormalizedSymbolWeights = new float[numberOfSymbols];
		cumulativeNormalizedTerminalWeights = new float[numberOfTerminals];
		cumulativeNormalizedFunctionWeights = new float[numberOfFunctions];
		int j = 0, k = 0;
		for (int i = 0; i < numberOfSymbols; i++) {
			float weight = (float) (symbols[i].weight);
			cumulativeNormalizedSymbolWeights[i] = weight;
			if (symbols[i] instanceof GEPTerminalSymbol
					|| symbols[i] instanceof GEPConstantTerminalSymbol)
				cumulativeNormalizedTerminalWeights[j++] = weight;
			if (symbols[i] instanceof GEPFunctionSymbol)
				cumulativeNormalizedFunctionWeights[k++] = weight;
		}
		RandomChoice.organizeDistribution(cumulativeNormalizedSymbolWeights);
		RandomChoice.organizeDistribution(cumulativeNormalizedTerminalWeights);
		RandomChoice.organizeDistribution(cumulativeNormalizedFunctionWeights);

		// use the 2/3 rule if fewer functions else the 1/2 rule (don't count
		// the constant
		// terminal here)
		if (numberOfFunctions < (numberOfTerminals - (species.useConstants ? 1
				: 0)))
			probabilityOfChoosingFunction = 2.0 / 3.0;
		else
			probabilityOfChoosingFunction = 0.5;

		// ... and finally get the training and testing data values for the
		// terminals and dependent variable
		// and put them into the Terminal instances (creating a 'special'
		// Terminal Symbol to
		// hold the dependent variable training and testing values)

		// If this is a time series problem AND we are using the raw time series
		// data then
		// we named the terminals v1, v2, ..., nn where n is the number of
		// independent
		// variables as specified in the embedding dimension (which) was used to
		// determine the number of terminals. But we have to process the time
		// series data
		// to get the values for each terminal ... get the raw data from the CSV
		// file
		// if specified or from the user program ... then process it into rows
		// of data
		// representing the independent variables and the dependent variable.
		//
		// timeseries-delay -- if 1 uses each time series value, if 2 uses every
		// other one, etc.
		// timeseries-embeddingdimension -- determines the number of timeseries
		// points to use
		// as independent variables when transforming the set of time series
		// data. Another
		// data point is used as the dependent variable value. So the time
		// series 'raw' data
		// consisting of a list of single values is processed by splitting the
		// data into
		// groups (rows) of size embeddingdimension+1. From the end of the time
		// series data
		// embeddingdimension+1 values are chosen (if delay is 1 all values are
		// chosen, if
		// 2 every other one is chosen). The last value is the independent
		// variable value.
		// Then the next row is selected by moving 'delay'
		// values from the end and chosing embeddingdimension+1 values. This is
		// repeated
		// until no more sets of size embeddingdimension+1 can be chosen. If
		// this produces
		// n sets of data then testingprediction of them are used for testing
		// and
		// (n - testingpredictions) are used for training.
		//
		// So if we had the data:
		// 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21
		// and delay was 1 and embeddingdimension was 4 then we'd process the
		// set into
		// the following 17 data sets. If testingpredictions was 6 then the 1st
		// 11
		// would be used for training and the last 6 for testing
		// iv1 iv2 iv3 iv4 dv
		// 1 2 3 4 5
		// 2 3 4 5 6
		// 3 4 5 6 7
		// . . .
		// 14 15 16 17 18
		// 15 16 17 18 19
		// 16 17 18 19 20
		// 17 18 19 20 21
		// If delay was 2 then 7 sets would be formed as:
		// iv1 iv2 iv3 iv4 dv
		// 1 3 5 7 9
		// 3 5 7 9 11
		// . . .
		// 9 11 13 15 17
		// 11 13 15 17 19
		// 13 15 17 19 21
		// timeseries-testingpredictions -- specifies the number of sets of data
		// to devote to testing
		if (timeseriesWithRawDataValues) {
			GEPDependentVariable.symbol = "dependentVariable";
			double rawTimeSeriesValues[] = null;
			if (terminalFileCSV == null)
				rawTimeSeriesValues = ((GEPProblem) state.evaluator.p_problem)
						.getTimeSeriesDataValues();
			else
				rawTimeSeriesValues = getRawTimeSeriesValuesFromCSVfile(state,
						terminalFileCSV);
			if (rawTimeSeriesValues == null)
				state.output
						.fatal("Unable to get time series data values from User Program or CSV file");
			Vector values[] = processRawTimeSeriesValues(state, species,
					rawTimeSeriesValues);
			// have an array of vectors; 1 vector for each indep variable and
			// the dep variable
			for (int i = 0; i < values.length; i++) {
				// get the values for training ... and testing (specified by
				// timeseriesTestingPredictions)
				int sizeOfTrainingData = values[i].size()
						- species.timeseriesTestingPredictions;
				double v[] = new double[sizeOfTrainingData];
				double testingV[] = new double[species.timeseriesTestingPredictions];
				for (int m = 0; m < v.length; m++)
					v[m] = ((Double) values[i].elementAt(m)).doubleValue();
				for (int n = 0; n < testingV.length; n++)
					testingV[n] = ((Double) values[i].elementAt(n
							+ sizeOfTrainingData)).doubleValue();
				if (i == values.length - 1) // last column in file is the
				// dependent var
				{
					GEPDependentVariable.setValues(v);
					GEPDependentVariable.setTestingValues(testingV);
				} else {
					((GEPTerminalSymbol) symbols[numberOfFunctions + i])
							.setValues(v);
					((GEPTerminalSymbol) symbols[numberOfFunctions + i])
							.setTestingValues(testingV);
				}
			}
		}
		// else If there is a file with the terminals and dep variable use this
		// else ask for
		// the values from the User Program (problem).
		else if (terminalFileCSV != null)// terminals defined in CSV file
		{
			// Rui: selecting the correct symbol for the dependent variable
			GEPDependentVariable.symbol = terminalSymbolsfromFile[depVarIdx];
			// get all the values into an array of vectors (each vector holds
			// the values for a
			// single terminal (dep or indep variable)
			Vector values[] = new Vector[terminalSymbolsfromFile.length];
			for (int i = 0; i < terminalSymbolsfromFile.length; i++)
				values[i] = new Vector();
			try {
				while (terminalFileCSV.readRecord()) {
					for (int i = 0; i < terminalSymbolsfromFile.length; i++)
						values[i].add(terminalFileCSV.get(i));
				}
			} catch (IOException e) {
				state.output
						.fatal("The file with terminal definitions/values failed when reading records. "
								+ e);
			}

			for (int i = 0; i < terminalSymbolsfromFile.length; i++) {
				double v[] = new double[values[i].size()];
				for (int m = 0; m < v.length; m++)
					try {
						v[m] = Double.parseDouble((String) values[i]
								.elementAt(m));
					} catch (Exception e) {
						state.output
								.fatal("Failed trying to read a training data set value. The field is supposed to be a number but was the string '"
										+ (String) values[i].elementAt(m)
										+ "'.\n" + e);
					}
				// Rui
				if (i == depVarIdx) { // dependent variable
					GEPDependentVariable.setValues(v);
					((GEPTerminalSymbol) symbols[numberOfFunctions + i])
							.setValues(v);
				} else
					((GEPTerminalSymbol) symbols[numberOfFunctions + i])
							.setValues(v);
			}
			// get the testing data as well if a file was specified
			if (testingTerminalFileCSV != null)// testing data defined in CSV
			// file
			{
				// get all the values into an array of vectors (each vector
				// holds the values for a
				// single terminal (dep or indep variable)
				Vector testingValues[] = new Vector[terminalSymbolsfromFile.length];
				for (int i = 0; i < terminalSymbolsfromFile.length; i++)
					testingValues[i] = new Vector();
				try {
					while (testingTerminalFileCSV.readRecord()) {
						for (int i = 0; i < terminalSymbolsfromFile.length; i++)
							testingValues[i].add(testingTerminalFileCSV.get(i));
					}
				} catch (IOException e) {
					state.output
							.fatal("The file with testing data values failed when reading records. "
									+ "\nMake sure the file has the same column separators as the testing data file."
									+ "\nAlso check that it has the same as the number of columns as the testing file"
									+ e);
				}

				for (int i = 0; i < terminalSymbolsfromFile.length; i++) {
					double v[] = new double[testingValues[i].size()];
					for (int m = 0; m < v.length; m++)
						try {
							v[m] = Double.parseDouble((String) testingValues[i]
									.elementAt(m));
						} catch (Exception e) {
							state.output
									.fatal("Failed trying to read a testing data set value. The field is supposed to be a number but was the string '"
											+ (String) testingValues[i]
													.elementAt(m) + "'.\n" + e);
						}
					// Rui
					if (i == depVarIdx){ // choose the correct index to set
											// the
										// testing values for the dependent
										// variable
						GEPDependentVariable.setTestingValues(v);
						((GEPTerminalSymbol) symbols[numberOfFunctions + i])
						.setTestingValues(v);
					}
					else
						((GEPTerminalSymbol) symbols[numberOfFunctions + i])
								.setTestingValues(v);
				}
			}
		}
		// else terminals were defined in the param file and no CSV file
		// defined so .... ask User Problem for the values, training and testing
		// (if there are any)
		else {
			GEPDependentVariable.symbol = "dependentVariable";
			GEPProblem prob = (GEPProblem) state.evaluator.p_problem;
			double vals[] = null;
			for (int i = numberOfFunctions; i < numberOfSymbolsWithoutConstantSymbol; i++) {
				GEPTerminalSymbol ts = (GEPTerminalSymbol) symbols[i];
				vals = prob.getDataValues(ts.symbol);
				if (vals == null)
					state.output
							.fatal("Expecting user problem (GEPProblem/ProblemForm) to supply training data values for terminal symbol '"
									+ ts + "'.");
				ts.setValues(vals);
				vals = prob.getTestingDataValues(ts.symbol);
				if (vals != null) // don't have to supply testing data
					ts.setTestingValues(vals);
			}
			vals = prob.getDataValues(GEPDependentVariable.symbol);
			if (vals == null)
				state.output
						.fatal("Expecting user problem (GEPProblem/ProblemForm) to supply training data values for dependent variable.");
			GEPDependentVariable.setValues(vals);
			vals = prob.getTestingDataValues(GEPDependentVariable.symbol);
			if (vals != null) // don't have to supply testing data
				GEPDependentVariable.setTestingValues(vals);
		}

		// Some checking of data values to ensure they meet the requirements for
		// the various problem types.
		// For all problem types need to make sure all indep vars and the dep
		// var have the same number of values!
		int numValues = GEPDependentVariable.values.length;
		for (int i = numberOfFunctions; i < numberOfSymbolsWithoutConstantSymbol; i++)
			if (((GEPTerminalSymbol) symbols[i]).values.length != numValues)
				state.output
						.fatal("Must have same number of values for all independent variables and the dependent variable."
								+ "/nNumber of values for Dependent Variable is: "
								+ numValues
								+ "/nNumber of values for Independent Variable '"
								+ symbols[i].symbol
								+ "' is: "
								+ ((GEPTerminalSymbol) symbols[i]).values.length);

		// For Classification and logical problems all dependent variable values
		// must be either 0 or 1
		if (species.problemType == GEPSpecies.PT_CLASSIFICATION
				|| species.problemType == GEPSpecies.PT_LOGICAL) {
			double dvVals[] = GEPDependentVariable.values;
			for (int i = 0; i < numValues; i++)
				if (dvVals[i] != 0.0 & dvVals[i] != 1.0)
					state.output
							.fatal("For classification/logical problems all dependent variable values must be either 1 or 0.\nFound value "
									+ dvVals[i]
									+ " at index "
									+ i
									+ "in the values.");
		}
		// For Logical problems all independent variable values must be 0 or 1
		if (species.problemType == GEPSpecies.PT_LOGICAL) { // for each
															// independent
			// variable symbol
			for (int i = numberOfFunctions; i < numberOfSymbolsWithoutConstantSymbol; i++) {
				double ivVals[] = ((GEPTerminalSymbol) symbols[i]).values;
				for (int m = 0; m < numValues; m++)
					if (ivVals[m] != 0.0 & ivVals[m] != 1.0)
						state.output
								.fatal("For logical problems all independent variable values must be either 1 or 0.\nFound value "
										+ ivVals[m]
										+ " at index '"
										+ m
										+ "' in the variable '"
										+ ((GEPTerminalSymbol) symbols[i]).symbol
										+ "'.");
			}
		}
		GEPDependentVariable.useTrainingData = true;
		state.output.exitIfErrors();
	}
}
