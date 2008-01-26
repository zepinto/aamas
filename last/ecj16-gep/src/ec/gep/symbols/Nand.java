package ec.gep.symbols;

import ec.EvolutionState;
import ec.gep.GEPFunctionSymbol;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.util.Parameter;

public class Nand extends GEPFunctionSymbol {

	public Nand() 
	{
		super("nand", 2);
	}

	public double eval(double params[]) 
	{
		//should check that there are 2 params
		boolean p1 = params[0] == 0.0 ? false : true;
		boolean p2 = params[1] == 0.0 ? false : true;
		return (((p1 && p2) == true) ? 0.0 : 1.0);
	}
	
	public boolean isLogicalFunction()
	{
		return true;
	}

	public String printMathExpression( String p[] )
	{
		return "(" + p[0] + " nand " + p[1] + ")";
	}
}
