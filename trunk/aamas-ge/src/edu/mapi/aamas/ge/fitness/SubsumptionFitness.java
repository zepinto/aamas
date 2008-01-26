package edu.mapi.aamas.ge.fitness;

import ec.EvolutionState;
import ec.gep.GEPIndividual;
import ec.multiobjective.SubsumptionMultiObjectiveFitness;

/**
 * A concrete instance of the subsumption fitness architecture, where we have
 * two objectives, accuracy and expression size. Accuracy subsumes expression
 * size until a configurable threshold is reached.
 * 
 * @author Rui Meireles
 * 
 */
public class SubsumptionFitness extends SubsumptionMultiObjectiveFitness
		implements IFitness {

	private static final long serialVersionUID = 1L;

	public void computeFitness(GEPIndividual individual, EvolutionState state) {

		// the most important objective is accuracy
		multifitness[0] = (float) FitnessPrimitives.BACCfitness(individual);
		// then comes the tree size
		multifitness[1] = (float) FitnessPrimitives.TSfitness(individual);
	}

	public String getDescription() {
		return String.format("type=Subsumption,minAccuracy=%s,maxLoss=%s",
				subsumptionThreshold[1], maxLoss[1]);
	}
	
	public double getAccuracy() {
		// the accuracy is stored on index zero of the multifitness array
		return multifitness[0];
	}
}
