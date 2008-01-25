package ec.gep;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;

public class WeightedFitness extends SimpleFitness implements IAamasFitness {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String P_WEIGHTED = "weighted";
	private static final String P_ACC_WEIGHT = "accuracy_weight";
	//private static final String P_SIZE_WEIGHT = "size_weight";

	private float acc_weight = 1.0f;
	private float size_weight = 0.0f;

	private double accuracy = 0.0;

	float fitness = 0.0f;

	public void computeFitness(GEPIndividual individual, EvolutionState state) {
		SimpleFitness f = new SimpleFitness();

		double size_fit = GEPFitnessFunction2.TSfitness(individual);
		double acc_fit = GEPFitnessFunction2.BACCfitness(individual);

		fitness = (float) (acc_fit * acc_weight + size_fit * size_weight);
		accuracy = acc_fit;
		f.setFitness(state, fitness(), (size_fit == 1.0 && acc_fit == 1.0));

	}

	public String getDescription() {
		return "type=Weighted,accuracyWeight=" + acc_weight + ",sizeWeight="
				+ size_weight;
	}

	public double getAccuracy() {
		return accuracy;
	}

	@Override
	public float fitness() {

		return fitness;
	}

	@Override
	public boolean betterThan(Fitness _fitness) {

		return _fitness.fitness() < fitness();
	}

	@Override
	public Parameter defaultBase() {
		return super.defaultBase().push(P_WEIGHTED);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);

		Parameter def = defaultBase();
		acc_weight = state.parameters.getFloat(base.push(P_ACC_WEIGHT), def
				.push(P_ACC_WEIGHT), 0, 1);
		size_weight = 1 - acc_weight;
	}

}
