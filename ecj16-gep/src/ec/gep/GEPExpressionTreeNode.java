/*
 * Copyright (c) 2006 by National Research Council of Canada.
 *
 * This software is the confidential and proprietary information of
 * the National Research Council of Canada ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into
 * with the National Research Council of Canada.
 *
 * THE NATIONAL RESEARCH COUNCIL OF CANADA MAKES NO REPRESENTATIONS OR
 * WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * THE NATIONAL RESEARCH COUNCIL OF CANADA SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 */

package ec.gep;

/**
 * GEPExpressionTreeNode.java
 * 
 * Created: Nov. 2006
 * By: Bob Orchard
 * 
 * GEPExpressionTreeNodes are the constructs used to hold a 'parsed' gene (Karva) expression
 * so that it can be evaluated. It holds constant values, terminals or functions. For a simple 
 * Karva expression like:
 * <br>
 * <code>
 * +.*.C0.v1.v2
 * 
 * We would have the following parsed tree:
 * 
 *                               Node0
 *                               - Function node for +
 *                               - param1 is Node1
 *                               - param2 is node2
 *                           
 *                  Node1                        Node2
 *                  - Function node for *        - Constant Node for C0
 *                  - param1 is node3            - constant value stored here
 *                  - param2 is node4
 *              
 *       Node3                      Node4
 *       - terminal node for v1     - terminal node for v2
 *       
 * </code>
 * 
 */

import java.io.Serializable;

public class GEPExpressionTreeNode implements Serializable, Cloneable  
{ 
	/** If this is a function node then there is a link to each parameter for the function */
	public GEPExpressionTreeNode parameters[];
	/** When the function's parameters are evaluated they are stored in this array */
	double evaluatedParameters[]; 
	/** This is the Terminal or Function Symbol for the node (empty if a constant node) */
	public GEPSymbol symbol;
	/** Number of paramters if this is a function node */
	public int numParameters = 0;
	/** true if this is a constant node */
	public boolean isConstantNode = false;
	/** the constant value if this is a constant node */
	public double constantValue;
	
	/** Constructor for nodes that hold Terminals or Functions */
    public GEPExpressionTreeNode(GEPSymbol s)
    {
    	symbol = s;
    	if (s.arity == 0)  // terminal symbols have arity 0, no parameters required
    		return;
    	
        parameters = new GEPExpressionTreeNode[s.arity];
    	evaluatedParameters = new double[s.arity];
    }
    
    
	/** Constructor for nodes that hold constant values */
    public GEPExpressionTreeNode(GEPSymbol s, double constant)
    {
    	this(s);
    	isConstantNode = true;
    	constantValue = constant;
    }
    
    /**
     * Add a parameter node for a function.
     * 
     * @param node the parameter node
     */
    public void addParameter(GEPExpressionTreeNode node)
    {
    	parameters[numParameters++] = node;
    }
    
    /**
     * Evaluate the expression encoded in this node and its
     * descendents (encoded in the parameters if any). Each
     * problem being investigated has a set of data associated with 
     * each variable. The expression is asked for an evaluation for
     * a particular index into this data set. The values for each 
     * terminal (variable) are stored in the terminal symbol object.
     * 
     * @param valuesIndex For which set of data should we evaluate the expression.
     * @return the evaluated expression
     */
    public double eval(int valuesIndex)
    {
    	if (isConstantNode)
    		return constantValue;
    	if (symbol instanceof GEPTerminalSymbol)
    	   return ((GEPTerminalSymbol)symbol).eval(valuesIndex);
    	// for function symbols we have to calc each parameter and then call the
    	// evaluator for the function with these paramaters.
    	for (int i=0; i<numParameters; i++)
    		evaluatedParameters[i] =  parameters[i].eval(valuesIndex);

    	return ((GEPFunctionSymbol)symbol).eval(evaluatedParameters);
    }
    
    public Object clone()
    {
    	if (isConstantNode)
    		return new GEPExpressionTreeNode(symbol, constantValue);

    	GEPExpressionTreeNode myobj = new GEPExpressionTreeNode(symbol);
    	myobj.numParameters = numParameters;
    	for (int i=0; i<numParameters; i++)
   		    myobj.parameters[i] = (GEPExpressionTreeNode)parameters[i].clone();
    	
    	return myobj;
    }
    
    /** Calculates the number of nodes in the expression tree from this node 
     *  down (all it subnodes/parameters);
     *  
     * @return the number of nodes in the expression with this node as the root
     */
    public int numberOfNodes()
    {   
    	int numNodesInSubNodes = 0;
    	for (int i=0; i<numParameters; i++)
    		numNodesInSubNodes += parameters[i].numberOfNodes();
    	return 1 + numNodesInSubNodes;
    }
    
    /** Calculates the number of times each independent variable is used in
     *  the expression tree from this node down (all it subnodes/parameters).
     *  So if this is the root of the expression it is how many times the variable
     *  is used in the expression. The indices of the array returned are relative
     *  to the beginning of the first integer id assigned for the terminals. that
     *  is index 0 in the array refers to the count of the 1st variable in the list of
     *  terminals.
     *  
     *  @param ss needs the GEPSymbolSet used by this problem
     *  @return an array with the count of variable useage for each variable
     */
    public void variableUseageCounts( int counts[] )
    {
		if (symbol instanceof GEPTerminalSymbol && !(symbol instanceof GEPConstantTerminalSymbol))
		{
			GEPTerminalSymbol s = (GEPTerminalSymbol)symbol;
			counts[s.id-s.symbolSet.getBaseTerminalSymbolId()]++;  
		}
    	for (int i=0; i<numParameters; i++)
    		parameters[i].variableUseageCounts( counts );
    
    }
}
