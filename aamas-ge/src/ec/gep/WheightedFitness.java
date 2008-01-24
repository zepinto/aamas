package ec.gep;

import ec.EvolutionState;
import ec.Fitness;
import ec.simple.SimpleFitness;
import ec.util.Parameter;


public class WheightedFitness extends SimpleFitness implements IAamasFitness {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String P_WHEIGHTED = "wheighted";
	private static final String P_ACC_WEIGHT = "accuracy_wheight";
	private static final String P_SIZE_WEIGHT = "size_wheight";
	
	private double acc_wheight = 1.0;
	private double size_wheight = 0.0;
	
	private double accuracy = 0.0;
	
	float fitness = 0.0f;
	
	
	public void computeFitness(GEPIndividual individual, EvolutionState state) {
		SimpleFitness f = new SimpleFitness();
		
		
		
		double size_fit = GEPFitnessFunction2.TSfitness(individual);
		double acc_fit = GEPFitnessFunction2.BACCfitness(individual);
		
		fitness = (float)(acc_fit * acc_wheight + size_fit * size_wheight);
		accuracy = acc_fit;
		f.setFitness(state, fitness(), (size_fit == 1.0 && acc_fit == 1.0));
		
	}
	
	public String getDescription() {		
		return "type=Wheighted,accuracyWheight="+acc_wheight+",sizeWheight="+size_wheight;
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
		return super.defaultBase().push(P_WHEIGHTED);
	}
	
	@Override
	public void setup(EvolutionState state, Parameter base) {	
		super.setup(state, base);
		
		Parameter def = defaultBase();
		acc_wheight = state.parameters.getDouble(base.push(P_ACC_WEIGHT), def.push(P_ACC_WEIGHT), 0, 1);
		size_wheight = state.parameters.getDouble(base.push(P_SIZE_WEIGHT), def.push(P_SIZE_WEIGHT), 0, 1);
	}
	
	
	
}
