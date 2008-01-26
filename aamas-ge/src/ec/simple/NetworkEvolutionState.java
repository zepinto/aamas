package ec.simple;

import ec.Population;
import ec.gep.GEPSymbolSet2;

/**
 * Class that extends the normal evolution state to get multiple solutions,
 * which is useful for problems such as boolean networks.
 * 
 * @author Rui Meireles
 * 
 */
public class NetworkEvolutionState extends SimpleEvolutionState {

	private static final long serialVersionUID = 1L;

	// initial population cache so we don't have to recompute everything all the
	// time
	Population protoPopulation;

	@Override
	public void run(int condition) {

		if (condition == C_STARTED_FRESH) {
			startFresh();
		} else // condition == C_STARTED_FROM_CHECKPOINT
		{
			startFromCheckpoint();
		}

		/* solve for all terminal symbols */
		for (int i = 0; !GEPSymbolSet2.isComplete(); i++) {

			prepIteration(i);

			/* evolution loop */
			int result = R_NOTDONE;
			while (result == R_NOTDONE)
				result = evolve();

			finish(result);
		}
	}

	@Override
	public void startFresh() {
		output.message("Setting up");
		setup(this, null); // a garbage Parameter
	}

	/**
	 * Prepares the execution environment for each consecutive problem instance.
	 * 
	 * @param i
	 */
	public void prepIteration(int i) {

		// start iterating all over again
		generation = 0;

		// POPULATION INITIALIZATION
		statistics.preInitializationStatistics(this);
		output.message(String.format("Solving for index %s", i));
		population = initializer.initialPopulation(this, 0); // single-threaded
		statistics.postInitializationStatistics(this);

		// INITIALIZE CONTACTS -- done after initialization to allow
		// a hook for the user to do things in Initializer before
		// an attempt is made to connect to island models etc.
		exchanger.initializeContacts(this);
		evaluator.initializeContacts(this);
	}

	@Override
	public void finish(int result) {
		// Output.message("Finishing");
		/* finish up -- we completed. */
		statistics.finalStatistics(this, result);
		finisher.finishPopulation(this, result);
		exchanger.closeContacts(this, result);
		evaluator.closeContacts(this, result);
	}

}
