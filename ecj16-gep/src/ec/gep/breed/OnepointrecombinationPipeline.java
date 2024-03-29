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

package ec.gep.breed;
import ec.*;
import ec.util.*;
import ec.vector.VectorIndividual;
import ec.gep.*;

import java.util.*;


/* 
 * OnepointrecombinationPipeline.java
 * 
 * Created: Nov 2006
 * By: Bob Orchard
 */

/**
 * OnepointrecombinationPipeline implements ...
 *
 

 * @author Bob Orchard
 * @version 1.0 
 */

public class OnepointrecombinationPipeline extends GEPBreedingPipeline
{
    public static final int NUM_SOURCES = 1;
    public static final String P_ONEPOINTRECOMBINATION_PIPE = "onepointrecombination";
    

    public Parameter defaultBase() { return GEPDefaults.base().push(P_ONEPOINTRECOMBINATION_PIPE);/* */ }

    public int numSources() { return NUM_SOURCES; }

    public Object clone()
    {
        OnepointrecombinationPipeline c = (OnepointrecombinationPipeline)(super.clone());
        
        // deep-cloned stuff
        return c;
    }

    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);

    }


    public int produce(final int min, 
                       final int max, 
                       final int start,
                       final int subpopulation,
                       final Individual[] inds,
                       final EvolutionState state,
                       final int thread) 
    {
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there -- for gep we force all of the population to
    	// be dealt with at once so min is set to max .. should be the entire population
    	// we use without the elite(s) individuals
    	int n = sources[0].produce(max,max,start,subpopulation,inds,state,thread);

        // clone the individuals if necessary
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // recomine 'em - randomly select from the entire population based on the
        // one point recombination probability (rate). So we will choose the numberToRecombine as:
        //
        //    numberToRecombine = onePointRecombinationProbability * (populationSize - numberOfElites)
        //
        // where n returned from (produced by) the sources breeder is the
        // (populationSize - numberOfElites)
        //
        // Note: we could walk through the entire population (in pairs) and use the probability to decide 
        //       if the individual should be transposed but this is slower and not it seems what Ferreira
        //       does in GeneXpro
        //
        // ... can't recombine a genome more than once I believe so we should be selecting the n
        // individuals to recombine using a 'without replacement method' -- Ferreira confirmed this
        
        GEPSpecies s = (GEPSpecies) inds[0].species;
        MersenneTwisterFast srt = state.random[thread];

        // note: must be an even number of genomes to recombine so make this so ...
        int numberToRecombine = (int)Math.round(s.onePointRecombinationProbability * (double)n);
        numberToRecombine = (numberToRecombine/2)*2;
        
        // select the genomes without replacement
        int chosenOnes[] = chooseWithoutReplacement(state, thread, numberToRecombine, n);
        
try {
	    for(int q=0; q<chosenOnes.length-1; q += 2)
        {
        	int selInd1 = chosenOnes[q];
        	int selInd2 = chosenOnes[q+1];
            onepointRecombine(state, thread, s, srt,
            		    (GEPIndividual)inds[selInd1],
						(GEPIndividual)inds[selInd2]);
            ((GEPIndividual)inds[selInd1]).evaluated = false;
            ((GEPIndividual)inds[selInd1]).parsedGeneExpressions = null;
            ((GEPIndividual)inds[selInd2]).evaluated = false;
            ((GEPIndividual)inds[selInd2]).parsedGeneExpressions = null;
        }
} catch (Exception e) { e.printStackTrace(); }

        return n;
     }
    /** One-point recombination will swap part of one chromosome
     *  with the same part of another chromosome. 
     *  <br>
     *  1. choose position in the chromosome (the 'one-point' position). <br>
     *  2. swap the part of the chromosomes from this point on with each other. Note
     *     that the point chosen is a 'bond' site so if 1 is chosen then the break is between 
     *     position 1 and 2 in the chromosome. In this case the stuff from 2 on is exchanged. 
     *     Actually the same thing is done by exchanging the stuff before position 2,
     *     so that's what we do.
     *   <br>
     * 
     */
    
    public void onepointRecombine( EvolutionState state, int thread, GEPSpecies s,
    		                       MersenneTwisterFast srt, GEPIndividual ind1, 
    		                       GEPIndividual ind2)
    {  
       int genome1[][] = ind1.genome;
       int genome2[][] = ind2.genome;
try {
       int crossoverPoint = srt.nextInt((s.geneSize*s.numberOfGenes)-1); // - 1 since we no bond at end of chromosome
       int pointsTomove = crossoverPoint+1; // num pts from beginning to the position of bond selected
       int temp1[] = new int[pointsTomove]; // we'll actually swap the first parts
       int temp2[] = new int[pointsTomove]; 
       // the swap will be the 'stuff' from gene 0, position 0, to gene (crossoverPoint mod geneSize),
       // position (crossoverpoint rem genesize);
       copyFromGenome(genome1, 0, 0, pointsTomove, temp1);
       copyFromGenome(genome2, 0, 0, pointsTomove, temp2);
       copyToGenome(genome1, 0, 0, pointsTomove, temp2);
       copyToGenome(genome2, 0, 0, pointsTomove, temp1);
} catch (Exception e) { e.printStackTrace(); }
    }
    
    private void copyFromGenome(int genome[][], int geneNum, int genePos, int lenToCopy, int placeToCopyTo[])
    {
    	int gene[] = genome[geneNum];
    	int geneLen = gene.length;
    	int j = genePos;
    	for (int i=0; i<lenToCopy; i++)
    	{
    		placeToCopyTo[i] = gene[j];
    		if (++j >= geneLen)
    		{	j = 0;
    		    gene = genome[++geneNum];
    		}
    	}
    }
    
    private void copyToGenome(int genome[][], int geneNum, int genePos, int lenToCopy, int placeToCopyFrom[])
    {
    	int gene[] = genome[geneNum];
    	int geneLen = gene.length;
    	int j = genePos;
    	for (int i=0; i<lenToCopy; i++)
    	{
    		gene[j] = placeToCopyFrom[i];
    		if (++j >= geneLen)
    		{	j = 0;
    		    gene = genome[++geneNum];
    		}
    	}
    }
}
