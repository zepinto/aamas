/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;
import ec.steadystate.*;
import ec.util.*;

/* 
 * Statistics.java
 * 
 * Created: Tue Aug 10 21:10:48 1999
 * By: Sean Luke
 */

/**
 * Statistics and its subclasses are Cliques which generate statistics
 * during the run.  Statistics are arranged in a tree hierarchy -- 
 * The root statistics object may have "children", and when the root is
 * called, it calls its children with the same message.  You can override
 * this behavior however you see fit.
  
 * <p>There are lots of places where statistics might be nice to print out.
 * These places are implemented as hooks in the Statistics object which you
 * can override if you like; otherwise they call the default behavior.  If
 * you plan on allowing your Statistics subclass to contain children, you
 * should remember to call the appropriate super.foo() if you 
 * override any foo() method.
 *
 * <p>While there are lots of hooks, various EvolutionState objects only
 * implement a subset of them.   You'll need to look at the EvolutionState
 * code to see which ones are implemented to make sure that your statistics
 * hooks are called appropriately!
 *
 * <p>Statistics objects should set up their statistics logs etc. during 
 * <tt>setup(...)</tt>.  Remember to make the log restartable in
 * case of restarting from a checkpoint.
 *
 * <p><b>Steady-State Statistics</b>.  For convenience, Statistics contains
 * default implementations of the SteadyStateStatisticsForm methods but
 * does not implement SteadyStateStatisticsForm.  This mostly is intended
 * to keep SteadyStateStatistcsForm implementations from having to call
 * functions on all their children: instead they can just call foo.super();
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>num-children</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(number of child statistics objects)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>child</tt>.<i>n</i><br>
 <font size=-1>classname, inherits or equals ec.Statistics</font></td>
 <td valign=top>(the class of child statistics object number <i>n</i>)</td></tr>
 </table>
 
 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>child</tt>.<i>n</i></td>
 <td>species (child statistics object number <i>n</i>)</td></tr>
 </table>

 * @author Sean Luke
 * @version 2.0 
 */

public class Statistics implements Singleton
    {
    public static final String P_NUMCHILDREN = "num-children";
    public static final String P_CHILD = "child"; 
    public Statistics[] children;
    
    public void setup(final EvolutionState state, final Parameter base)
        {
        int t = state.parameters.getIntWithDefault(base.push(P_NUMCHILDREN),null,0);
        if (t < 0) 
            state.output.fatal("A Statistics object cannot have negative number of children",
                               base.push(P_NUMCHILDREN));

        // load the trees
        children = new Statistics[t];

        for (int x=0;x<t;x++)
            {
            Parameter p = base.push(P_CHILD).push(""+x);
            children[x] = (Statistics)(state.parameters.getInstanceForParameterEq(p,null,Statistics.class));
            ((Statistics)children[x]).setup(state,p);
            }
        }
    
    /** Called immediately before population initialization occurs. */
    public void preInitializationStatistics(final EvolutionState state) 
        { 
        for(int x=0;x<children.length;x++)
            children[x].preInitializationStatistics(state);
        }
    /** Called immediately after population initialization occurs. */
    public void postInitializationStatistics(final EvolutionState state) 
        {
        for(int x=0;x<children.length;x++)
            children[x].postInitializationStatistics(state);
        }

    /** Called immediately before checkpointing occurs. */
    public void preCheckpointStatistics(final EvolutionState state) 
        {
        for(int x=0;x<children.length;x++)
            children[x].preCheckpointStatistics(state);
        }

    /** Called immediately after checkpointing occurs. */
    public void postCheckpointStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].postCheckpointStatistics(state);
        }

    /** Called immediately before evaluation occurs. */
    public void preEvaluationStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].preEvaluationStatistics(state);
        }

    /** Called immediately after evaluation occurs. */
    public void postEvaluationStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].postEvaluationStatistics(state);
        }

    /** Called immediately before the pre-breeding exchange occurs. */
    public void prePreBreedingExchangeStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].prePreBreedingExchangeStatistics(state);
        }
        
    /** Called immediately after the pre-breeding exchange occurs. */
    public void postPreBreedingExchangeStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].postPreBreedingExchangeStatistics(state);
        }

    /** Called immediately before breeding occurs. */
    public void preBreedingStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].preBreedingStatistics(state);
        }

    /** Called immediately after breeding occurs. */
    public void postBreedingStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].postBreedingStatistics(state);
        }

    /** Called immediately before the post-breeding exchange occurs. */
    public void prePostBreedingExchangeStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].prePostBreedingExchangeStatistics(state);
        }

    /** Called immediately after the post-breeding exchange occurs. */
    public void postPostBreedingExchangeStatistics(final EvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            children[x].postPostBreedingExchangeStatistics(state);
        }

    /** Called immediately after the run has completed.  <i>result</i>
        is either <tt>state.R_FAILURE</tt>, indicating that an ideal individual
        was not found, or <tt>state.R_SUCCESS</tt>, indicating that an ideal
        individual <i>was</i> found. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        for(int x=0;x<children.length;x++)
            children[x].finalStatistics(state, result);
        }
    
    /** Steady-state EC only: called immediately before the initial generation is evaluated. */
    public void preInitialEvaluationStatistics(final SteadyStateEvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            if (children[x] instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)children[x]).preInitialEvaluationStatistics(state);
        }
        
    /** Steady-state EC only: called immediately after the initial generation is evaluated. */
    public void postInitialEvaluationStatistics(final SteadyStateEvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            if (children[x] instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)children[x]).postInitialEvaluationStatistics(state);
        }
        
    /** Steady-state EC only: called each time new individuals are bred during the steady-state
        process.  You can look up the individuals in state.newIndividuals[] */
    public void individualsBredStatistics(SteadyStateEvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            if (children[x] instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)children[x]).individualsBredStatistics(state);
        }
    
    /** Steady-state EC only: called each time new individuals are evaluated during the steady-state
        process.  You can look up the individuals in state.newIndividuals[] */
    public void individualsEvaluatedStatistics(SteadyStateEvolutionState state)
        {
        for(int x=0;x<children.length;x++)
            if (children[x] instanceof SteadyStateStatisticsForm)
                ((SteadyStateStatisticsForm)children[x]).individualsEvaluatedStatistics(state);
        }
    }
