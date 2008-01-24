package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Ln extends GEPFunctionSymbol {

	public Ln() 
	{
		super("ln", 1);
	}

	public double eval(double params[]) 
	{
		//should check that there is only 1 paramater
		return (Math.log(params[0]));
	}
	
	public boolean isLogicalFunction()
	{
		return false;
	}

	public String printMathExpression( String p[] )
	{
		return "ln("+p[0]+")";
	}
}
