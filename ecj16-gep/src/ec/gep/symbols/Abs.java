package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Abs extends GEPFunctionSymbol {

	public Abs() 
	{
		super("abs", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.abs(params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}
	
	public String printMathExpression( String p[] )
	{
		return "abs("+p[0]+")";
	}

}
