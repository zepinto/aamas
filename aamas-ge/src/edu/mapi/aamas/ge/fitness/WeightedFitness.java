package edu.mapi.aamas.ge.fitness;

import ec.EvolutionState;
import ec.gep.GEPIndividual;
import ec.simple.SimpleFitness;
import ec.util.Parameter;

public class WeightedFitness extends SimpleFitness implements IFitness {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String P_WEIGHTED = "weighted";
	private static final String P_ACC_WEIGHT = "accuracy_weight";

	private float acc_weight = 1.0f;
	private float size_weight = 0.0f;

	private double accuracy = 0.0;

	public void computeFitness(GEPIndividual individual, EvolutionState state) {

		double size_fit = FitnessPrimitives.TSfitness(individual);
		double acc_fit = FitnessPrimitives.BACCfitness(individual);

		fitness = (float) (acc_fit * acc_weight + size_fit * size_weight);
		accuracy = acc_fit;
	}

	public String getDescription() {
		return String.format("type=Weighted,accuracyWeight=%s,sizeWeight=%s",
				acc_weight, size_weight);
	}

	public double getAccuracy() {
		return accuracy;
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
