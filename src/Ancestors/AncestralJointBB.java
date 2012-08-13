/*
 * This file is part of GeLL.
 * 
 * GeLL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeLL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeLL.  If not, see <http://www.gnu.org/licenses/>.
 */

package Ancestors;

import Alignments.Site;
import Alignments.Alignment;
import Alignments.AlignmentException;
import Ancestors.AncestralJointDP.MultipleRatesException;
import Exceptions.UnexpectedError;
import Likelihood.Calculator.SiteCalculator;
import Likelihood.Likelihood.LikelihoodException;
import Likelihood.Likelihood.SiteLikelihood;
import Likelihood.Probabilities;
import Constraints.SiteConstraints;
import Parameters.Parameters;
import Models.RateCategory;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import Utils.SetUtils;
import Utils.SetUtils.SetHasMultipleElementsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to perform joint ancestral reconstrion using the method of Pupko 2002
 * slightly modified
 * @author Daniel Money
 * @version 1.2
 */
public class AncestralJointBB extends AncestralJoint
{   
    AncestralJointBB(Model m, Alignment a, Tree t)
    {
	this.a = a;
        this.m = new HashMap<>();
	this.m.put(null,m);
	this.t = t;
        //Create a dynamic programming ancestral reconstructir for each rate
        //category.  Saves creating one for each site.  Used to choose a sensible
        //starting state for branch and bound search.
        dps = new HashMap<>();
        for (RateCategory r: m)
        {
            try
            {
                dps.put(r,new AncestralJointDP(new Model(r),a,t));
            }
            catch (MultipleRatesException ex)
            {
                //Can't reach here as we know we only pass in models with a single
                //rate category but just in case...
                throw new UnexpectedError(ex);
            }
        }
    }
    
    AncestralJointBB(Map<String,Model> m, Alignment a, Tree t) throws AlignmentException
    {
	this.a = a;
        this.m = m;
	this.t = t;
        //Create a dynamic programming ancestral reconstructir for each rate
        //category.  Saves creating one for each site.  Used to choose a sensible
        //starting state for branch and bound search.
        dps = new HashMap<>();
        for (Model mo: m.values())
        {
            for (RateCategory r: mo)
            {
                try
                {
                    dps.put(r,new AncestralJointDP(new Model(r),a,t));
                }
                catch (MultipleRatesException ex)
                {
                    //Can't reach here as we know we only pass in models with a single
                    //rate category but just in case...
                    throw new UnexpectedError(ex);
                }
            }
        }
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
    }

    public Alignment calculate(Parameters p) throws RateException, ModelException, AncestralException, TreeException, ParameterException, AlignmentException, LikelihoodException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Branch b: t)
	{
            if (!p.hasParam(b.getChild()))
            {
                p.addParameter(Parameter.newFixedParameter(b.getChild(),
                   b.getLength()));

            }
	}
        
        //Calculate probabilities for this model, tree and set of parameters
        Map<String,Probabilities> P = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t,p));
        }

        //Get unqiue sites in the alignment and calculator a reconstuction for each
        Map<Site,Site> ret = new HashMap<>();
	for (Site s: a.getUniqueSites())
	{
	    try
	    {
		ret.put(s,calculateSite(s,P.get(s.getSiteClass())));
	    }
	    catch (AncestralException e)
	    {
		throw new AncestralException(e.getMessage() + " at site " + s, null);
	    }
	}

        //Make a new alignment using the result for each unique site
        List<Site> alignment = new ArrayList<>();
        for (Site s: a)
        {
            alignment.add(ret.get(s));
        }

	return new Alignment(alignment);
    }

    //private String[] calculateSite(int ch, String[][] ca, HashMap<RateClass,SquareMatrix[]> matrices, HashMap<RateClass,double[]> freq,
    Site calculateSite(Site ca, Probabilities P) throws TreeException, AncestralException, LikelihoodException
    {
        //Based on Pupko 2002 but without the second bound metioned.
        //Seems to be efficient without it.  The second bound could be
        //implememted if it's found to be needed although the dynamic programming
        //classes would need changes as well.  There's also no attempt to be clever
        //in the order we visit the internal nodes.  Again this seems to be reasonably
        //effecient without it.                
        SiteConstraints con = new SiteConstraints(P.getAllStatesAsList());
        
        //Calculate the site likelihood without any constraints
        SiteLikelihood sl = (new SiteCalculator(ca,t,con,P)).calculate();
	RateCategory br = null;
        //And then use this to find the rate category that contributes the most likelihood
	double brs = -Double.MAX_VALUE;
	for (RateCategory r: m.get(ca.getSiteClass()))
	{
            double rs = 0.0;
            try
            {
                rs = sl.getRateLikelihood(r).getLikelihood();
            }
            catch (LikelihoodException ex)
            {
                //Shouldn't reach here as we know what information should
                // have been claculated and only ask for that
                throw new UnexpectedError(ex);
            }
            if (rs > brs)
            {
                br = r;
            }
	}

        //Do an ancestral reconstruction using this single rate category using
        //the dynamic rogramming method.  This is used to "seed" the branch and
        //bound.  That is each time we are testing new states at a node we test
        //the state calculated here first before any others.
        Site ba = dps.get(br).calculateSite(ca, P);

        //If we want to do calculation with some nodes already fixed that's
        //the same as constraining the node to the fixedstate so we can use
        //the constraints mechanism to keep track of what internal nodes we've
        //fixed.  To begin with nbo nodes are fixed.
        SiteConstraints assign = new SiteConstraints(P.getAllStatesAsList());
        
        //Initialise the structure that keeps track of the best reconstruction
        //found so far.  We don't have a best reconstruction yet, so pass
        //a dummy assignment with an infinitely small likelihood
	Best best = new Best(assign, -Double.MAX_VALUE);
        //Do death first search (DFS) recursively to find the best reconstruction
        best = DFS(ca, assign, best, P, ba);

        //String[] ret = new String[order.size()];
        //Create the result
        LinkedHashMap<String,String> ret = new LinkedHashMap<>();
        //By copying the original site
        for (String s: ca.getTaxa())
        {
            try
            {
                ret.put(s, ca.getRawCharacter(s));
            }
            catch (AlignmentException e)
            {
                //Should never reach here as we're looping over the known taxa hence...
                throw new UnexpectedError(e);
            }
        }
        //And then adding the reconstruction
        for (String in: t.getInternal())
        {
            try
            {
                //ret[i] = SetUtils.getSingleElement(assign.getConstraint(in));
                ret.put(in, SetUtils.getSingleElement(best.assign.getConstraint(in)));
            }
            catch (SetHasMultipleElementsException e)
            {
                //Shouldn't reach here as we only ever constrain each node to
                //a single site during the reconstruction
                throw new UnexpectedError(e);
            }
        }
        
        return new Site(ret);
    }

    private Best DFS(Site site, SiteConstraints assign, Best best, Probabilities P, Site ba) throws LikelihoodException
    {
        //Recursive depth first search of possible reconstructions
     
        //If we've made an assignment to every node then...
	if (isFull(assign))
	{
            //Calculate the likelihood of that reconstruction
            double s = (new SiteCalculator(site,t,assign,P)).calculate().getLikelihood();
            //If it's better than the bext reconstruction we've encountered so far
            //update the best and return it
	    if (s > best.score)
	    {
		//System.out.println("Best:\t" + s + "\t" + Arrays.toString(assign));
		return new Best(assign, s);
	    }
            //Else return the previous best
	    else
	    {
		//System.out.println("Discard:\t" + s + "\t" + Arrays.toString(assign));
		return best;
	    }
	}
        
        //Else we've not made a full assignment so...
        
        //Calculate the bound on the best likelihood that we could get with the
        //assignment we do already have.  This bound is calculated by summing accross
        //all possible states at unassigned nodes using the normal (quick) likelihood
        //calculation method.
        double bound = (new SiteCalculator(site,t,assign,P)).calculate().getLikelihood();

	//System.out.println(best.score + "\t" + bound + "\t" + Arrays.toString(assign));

        //If the bounded value is less the best econstruction we've aleady found
        //there's no point considering assigning more nodes so just return
        //the best reconstruction we've found.
	if (bound <= best.score)
	{
	    //System.out.println("Pruned\t" + bound + "\t" + Arrays.toString(assign));
	    return best;
	}
        
        //Get a currently unassigned node
	String b = getFirst(assign);
        
        //Clone the current assignment
	SiteConstraints na = assign.clone();
        
        //First try the best single-rate assignment we calculated above
        try
        {
            na.addConstraint(b, ba.getRawCharacter(b));
        }
        catch (AlignmentException e)
        {
            //Should never reach here as we're looping over the known taxa hence...
            throw new UnexpectedError(e);
        }
        //and recurse...
	best = DFS(site,na,best,P,ba);

        //Next try all other assignments
	for (String state: P.getAllStatesAsList())
	{
            //Excpet the one we've already tried
            try
            {
                if (!state.equals(ba.getRawCharacter(b)))
                {
                    //Clone the assignment and add the current assignment
                    na = assign.clone();
                    na.addConstraint(b, state);
                    //and recurse...
                    best = DFS(site,na,best,P,ba);
                }
            }
            catch (AlignmentException e)
            {
                //Should never reach here as we're looping over the known taxa hence...
                throw new UnexpectedError(e);
            }
	}
        //Return the best we've found (used when coming back up the recursion)
	return best;
    }
    


    private String getFirst(SiteConstraints assign)
    {
        //Gets the first unassigned node.  No attempt to be clever on choosing
        //an effecient node to do next.
	for (String i: t.getInternal())
	{
	    if (!assign.nodeIsConstrained(i))
	    {
		return i;
	    }
	}
	return null;
    }

    private boolean isFull(SiteConstraints assign)
    {
	for (String i : t.getInternal())
	{
	    if (!assign.nodeIsConstrained(i))
	    {
		return false;
	    }
	}
	return true;
    }

    private class Best
    {

	private Best(SiteConstraints assign, double score)
	{
	    this.assign = assign;
	    this.score = score;
	}

	private SiteConstraints assign;

	private double score;
    }

    private Alignment a;

    private Map<String,Model> m;

    private Tree t;
    
    private Map<RateCategory,AncestralJointDP> dps;
}
