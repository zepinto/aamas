package ec.multiobjective;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;

/**
 * SubsumptionMultiObjectiveFitness is a subclass of MultiObjectiveFitness which
 * adds the concept of subsumption to the basic multi-objective fitness
 * mechanism.
 * 
 * Here, the various fitness measures in the are order
 * 
 * 
 * @author Rui Meireles
 * 
 */
public class SubsumptionMultiObjectiveFitness extends MultiObjectiveFitness {

	private static final long serialVersionUID = 1L;

	// for reading parameters
	private static final String P_SUBSUMPTION = "subsumption";
	private static final String P_SUBTHRESHOLD = "subthreshold";
	private static final String P_MAXLOSS = "maxloss";

	protected double[] subsumptionThreshold;
	protected double[] maxLoss;

	@Override
	public Parameter defaultBase() {
		return super.defaultBase().push(P_SUBSUMPTION);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {

		super.setup(state, base);

		// Load the subsumption deactivation and acceptable loss thresholds

		// the size is equal to the number of parameters minus one
		subsumptionThreshold = new double[multifitness.length];
		maxLoss = subsumptionThreshold.clone();

		// fill the threshold arrays. Basically the defaults mean no subsumption
		// and no loss admissible
		Parameter def = defaultBase();
		for (int i = 0; i < subsumptionThreshold.length - 1; i++) {
			String iString = String.format("%d", i);
			subsumptionThreshold[i + 1] = state.parameters.getDouble(base.push(
					P_SUBTHRESHOLD).push(iString), def.push(P_SUBTHRESHOLD)
					.push(iString), 0, 1);
			if ((maxLoss[i + 1] = state.parameters.getDouble(base.push(
					P_MAXLOSS).push(iString),
					def.push(P_MAXLOSS).push(iString), 0, 1)) < 0)
				maxLoss[i + 1] = 0; // insure we do not get a negative maxLoss
		}
	}

	/**
	 * Returns true if I'm equivalent in fitness (neither better nor worse) to
	 * _fitness. The rule I'm using is this: If we are equal in all criteria,
	 * then equivalentTo is true, otherwise it is false.
	 */

	@Override
	public boolean equivalentTo(Fitness _fitness) {

		MultiObjectiveFitness moFitness = toMultiObjectiveFitness(_fitness);

		for (int i = 0; i <
		// just to be safe...
		Math.min(multifitness.length, moFitness.multifitness.length); i++) {
			if (multifitness[i] != moFitness.multifitness[i])
				return false;
			// we only care about lower priority measures when the higher ones
			// are properly satisfied
			if (multifitness[i] < subsumptionThreshold[i])
				break;
		}
		return true;
	}

	/**
	 * Returns true if I'm better than _fitness based on the subsumption chain.
	 * Basically we only look at lower priority fitness measures if the higher
	 * priority ones are good enough (> subsumptionThreshold) and don't make
	 * higher priority measures go bad more than the value indicated by maxLoss.
	 */
	@Override
	public boolean betterThan(Fitness _fitness) {

		// we are beaten by default and will have to prove our superiority if we
		// want to stay alive
		boolean abeatsb = false;
		MultiObjectiveFitness moFitness = toMultiObjectiveFitness(_fitness);

		// just to be safe...
		int fitnessLenght = Math.min(multifitness.length,
				moFitness.multifitness.length);

		for (int i = 0; i < fitnessLenght; i++) {

			// am I any better?
			if (multifitness[i] > moFitness.multifitness[i])
				abeatsb = true;

			// we only care about lower priority measures when the higher ones
			// are properly satisfied
			if (multifitness[i] < subsumptionThreshold[i])
				break;

			// two ways to be beaten...
			// either it is better than me on index i...
			if (multifitness[i] < moFitness.multifitness[i]
			// or we are both good enough on index i and he has better fitness
					// for index i+1 while loosing less than maxLoss on index i
					|| (i + 1 != fitnessLenght
							&& multifitness[i] > subsumptionThreshold[i]
							&& moFitness.multifitness[i] > subsumptionThreshold[i]
							&& moFitness.multifitness[i + 1] > multifitness[i + 1] && Math.abs(multifitness[i]
							- moFitness.multifitness[i]) <= maxLoss[i]))
				return false;
		}

		return abeatsb;
	}

	private MultiObjectiveFitness toMultiObjectiveFitness(Fitness _fitness) {
		if (!(_fitness instanceof MultiObjectiveFitness))
			throw new IllegalArgumentException(
					String
							.format(
									"betterThan function: Unexpected fitness object of type %s (expecting %s).",
									_fitness.getClass().getName(),
									MultiObjectiveFitness.class.getName()));
		MultiObjectiveFitness moFitness = (MultiObjectiveFitness) _fitness;
		return moFitness;
	}
}
