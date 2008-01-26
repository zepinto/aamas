/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.parity.func;
import ec.*;
import ec.app.parity.*;
import ec.gp.*;
import ec.util.*;

/* 
 * Nand.java
 * 
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0 
 */

public class Nand extends GPNode
    {
    public String toString() { return "nand"; }

    public void checkConstraints(final EvolutionState state,
                                 final int tree,
                                 final GPIndividual typicalIndividual,
                                 final Parameter individualBase)
        {
        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=2)
            state.output.error("Incorrect number of children for node " + 
                               toStringForError() + " at " +
                               individualBase);
        }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
        {
        ParityData d = ((ParityData)input);
        // shortcutting NAND
        children[0].eval(state,thread,input,stack,individual,problem);

        if (d.x == 1)  // return the second item
            children[1].eval(state,thread,input,stack,individual,problem);

        // invert
        d.x ^= 1;
        }
    }



