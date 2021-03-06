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
import Likelihood.Probabilities;
import Likelihood.Probabilities.RateProbabilities;
import Likelihood.SiteLikelihood;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Likelihood.SiteLikelihood.RateLikelihood;
import Maths.Real;
import Maths.SquareMatrix;
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
 * @version 2.0
 */
public class AncestralJointBB extends AncestralJoint
{   
    AncestralJointBB(Model m, Alignment a, Tree t)
    {
	this.a = a;
        this.m = new HashMap<>();
	this.m.put(null,m);
        this.t = new HashMap<>();
	this.t.put(null,t);
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
        this.t = new HashMap<>();
        for (String s: m.keySet())
        {
            this.t.put(s,t);
        }
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

    AncestralJointBB(Model m, Alignment a, Map<String,Tree> t) throws AlignmentException
    {
	this.a = a;
        this.t = t;
        this.m = new HashMap<>();
        for (String s: t.keySet())
        {
            this.m.put(s,m);
        }
        //Create a dynamic programming ancestral reconstructir for each rate
        //category.  Saves creating one for each site.  Used to choose a sensible
        //starting state for branch and bound search.
        dps = new HashMap<>();
        for (Model mo: this.m.values())
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
        if (!a.check(t))
        {
            throw new AlignmentException("Alignment contains classes for which no tree has been defined");
        }
    }
    
    AncestralJointBB(Map<String,Model> m, Alignment a, Map<String,Tree> t) throws AlignmentException
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
        if (!a.check(t))
        {
            throw new AlignmentException("Alignment contains classes for which no tree has been defined");
        }
    }
    
    public Alignment calculate(Parameters p) throws RateException, ModelException, AncestralException, TreeException, ParameterException, AlignmentException, LikelihoodException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Tree tt: t.values())
        {
            for (Branch b: tt)
            {
                if (!p.hasParam(b.getChild()))
                {
                    p.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));

                }
            }
        }
        
        //Calculate probabilities for this model, tree and set of parameters
        Map<String,Probabilities> P = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t.get(e.getKey()),p));
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

    Site calculateSite(Site ca, Probabilities P) throws TreeException, AncestralException, LikelihoodException, RateException
    {
        //Based on Pupko 2002 but without the second bound metioned.
        //Seems to be efficient without it.  The second bound could be
        //implememted if it's found to be needed although the dynamic programming
        //classes would need changes as well.  There's also no attempt to be clever
        //in the order we visit the internal nodes.  Again this seems to be reasonably
        //effecient without it.
        
        //Create a new, empty, assignemnt
        Assignment assignment = new Assignment();
        
        //Calculate the site likelihood
        //SiteLikelihood sl = (new SiteCalculator(t,P,assignment.getInitialNodeLikelihoods(t, ca, P.getMap()))).calculate();
        SiteLikelihood sl = calculateSite(t.get(ca.getSiteClass()),P,assignment.getInitialNodeLikelihoods(t.get(ca.getSiteClass()), ca, P.getMap()));
	RateCategory br = null;
        //And then use this to find the rate category that contributes the most likelihood
	Real brs = null;
	for (RateCategory r: m.get(ca.getSiteClass()))
	{
            try
            {
                Real rs = sl.getRateLikelihood(r).getLikelihood();
                if ((br == null) || rs.greaterThan(brs))
                {
                    br = r;
                    brs = rs;
                }
            }
            catch (LikelihoodException ex)
            {
                //Shouldn't reach here as we know what information should
                // have been claculated and only ask for that
                throw new UnexpectedError(ex);
            }
	}

        //Do an ancestral reconstruction using this single rate category using
        //the dynamic rogramming method.  This is used to "seed" the branch and
        //bound.  That is each time we are testing new states at a node we test
        //the state calculated here first before any others.
        Site ba = dps.get(br).calculateSite(ca, P);

        //Create a new empty assignemnt
        Assignment assign = new Assignment();
        
        //Initialise the structure that keeps track of the best reconstruction
        //found so far.  We don't have a best reconstruction yet, so pass
        //a dummy assignment with an infinitely small likelihood
	Best best = new Best(assign, null);
        //Do death first search (DFS) recursively to find the best reconstruction
        best = DFS(ca, assign, best, P, ba);

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
        for (String in: t.get(ca.getSiteClass()).getInternal())
        {
            ret.put(in, best.assign.getAssignment(in));
        }
        
        return new Site(ret);
    }

    private Best DFS(Site site, Assignment assign, Best best, Probabilities P, Site ba) throws LikelihoodException
    {
        //Recursive depth first search of possible reconstructions
     
        //If we've made an assignment to every node then...
	if (isFull(assign,t.get(site.getSiteClass())))
	{
            //Calculate the likelihood of that reconstruction
            //Real s = (new SiteCalculator(t,P,assign.getInitialNodeLikelihoods(t, site, P.getMap()))).calculate().getLikelihood();
            Real s = calculateSite(t.get(site.getSiteClass()),P,assign.getInitialNodeLikelihoods(t.get(site.getSiteClass()), site, P.getMap())).getLikelihood();
            //If it's better than the bext reconstruction we've encountered so far
            //update the best and return it
	    if ((best.score == null) || s.greaterThan(best.score))
	    {
		return new Best(assign, s);
	    }
            //Else return the previous best
	    else
	    {
		return best;
	    }
	}
        
        //Else we've not made a full assignment so...
        
        //Calculate the bound on the best likelihood that we could get with the
        //assignment we do already have.  This bound is calculated by summing accross
        //all possible states at unassigned nodes using the normal (quick) likelihood
        //calculation method.
        //Real bound = (new SiteCalculator(t,P,assign.getInitialNodeLikelihoods(t, site, P.getMap()))).calculate().getLikelihood();
        Real bound = calculateSite(t.get(site.getSiteClass()),P,assign.getInitialNodeLikelihoods(t.get(site.getSiteClass()), site, P.getMap())).getLikelihood();


        //If the bounded value is less the best econstruction we've aleady found
        //there's no point considering assigning more nodes so just return
        //the best reconstruction we've found.
        if ((best.score != null) && best.score.greaterThan(bound))
	{
	    return best;
	}
        
        //Get a currently unassigned node
	String b = getFirst(assign,t.get(site.getSiteClass()));
        
        //Clone the current assignment
	Assignment na = assign.clone();
        
        //First try the best single-rate assignment we calculated above
        try
        {
            na.addAssignment(b, ba.getRawCharacter(b));
        }
        catch (AlignmentException e)
        {
            //Should never reach here as we're looping over the known taxa hence...
            throw new UnexpectedError(e);
        }
        //and recurse...
	best = DFS(site,na,best,P,ba);

        //Next try all other assignments
        for (String state: P.getAllStates())
	{
            //Excpet the one we've already tried
            try
            {
                if (!state.equals(ba.getRawCharacter(b)))
                {
                    //Clone the assignment and add the current assignment
                    na = assign.clone();
                    na.addAssignment(b, state);
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
    


    private String getFirst(Assignment assign, Tree tt)
    {
        //Gets the first unassigned node.  No attempt to be clever on choosing
        //an effecient node to do next.
	for (String i: tt.getInternal())
	{
	    if (!assign.nodeIsAssigned(i))
	    {
		return i;
	    }
	}
	return null;
    }

    private boolean isFull(Assignment assign, Tree tt)
    {
	for (String i : tt.getInternal())
	{
	    if (!assign.nodeIsAssigned(i))
	    {
		return false;
	    }
	}
	return true;
    }
    
    SiteLikelihood calculateSite(Tree t, Probabilities tp, Map<String,NodeLikelihood> nl)
    {    
        List<Branch> branches = t.getBranches();
        Map<RateCategory,RateLikelihood> rateLikelihoods = new HashMap<>(tp.getRateCategory().size());

        //Calculate the likelihood for each RateCategory
        for (RateCategory rc: tp.getRateCategory())
        {
            RateProbabilities rp = tp.getP(rc);
            //Initalise the lieklihood values at each node.  first internal
            //using the alignemnt.
            Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(branches.size() + 1);
            for (String l: t.getLeaves())
            {
                nodeLikelihoods.put(l, nl.get(l).clone());
            }

            //And now internal nodes
            for (String i: t.getInternal())
            {
                nodeLikelihoods.put(i, nl.get(i).clone());
            }

            //for each branch.  The order these are returned in from tree means
            //we visit any branch with a node as it's parent before we visit
            //the branch with the node as the child.  Hence we treverse the
            //tree in the standard manner.  For each branch we will update
            //the likelihood at the parent node...
            for (Branch b: branches)
            {
                //BranchProbabilities bp = rp.getP(b);
                SquareMatrix bp = rp.getP(b);
                //So for each state at the parent node...
                //for (String endState: tp.getAllStates())
                for (String endState: tp.getAllStates())
                {
                    //l keeps track of the total likelihood from each possible
                    //state at the child
                    //For each possible child state
                    Real[] n = nodeLikelihoods.get(b.getChild()).getLikelihoods();
                    Real l = n[0].multiply(bp.getPosition(tp.getMap().get(endState), 0));
                    for (int j = 1; j < n.length; j++)
                    {
                        //Add the likelihood of going from start state to
                        //end state along that branch in that ratecategory
                        l = l.add(n[j].multiply(bp.getPosition(tp.getMap().get(endState), j)));
                    }
                    //Now multiply the likelihood of the parent by this total value.
                    //This will happen for each possible child as per standard techniques
                    nodeLikelihoods.get(b.getParent()).multiply(endState,l);
                }
            }

            //Rate total traxcks the total likelihood for this site and rate category
            Real ratetotal = null;//SiteLikelihood.getReal(0.0);//new Real(0.0);
            //Get the root likelihoods
            NodeLikelihood rootL = nodeLikelihoods.get(t.getRoot());

            ratetotal = tp.getRoot(rc).calculate(rootL);

            //Store the results for that rate
            rateLikelihoods.put(rc, new RateLikelihood(ratetotal,nodeLikelihoods));
            //Update the total site likelihood with the likelihood for the rate
            //category multiplied by the probility of being in that category
        }
        //return an object containg the results for that site
        return new SiteLikelihood(rateLikelihoods, tp);
    }

    private class Best
    {

	private Best(Assignment assign, Real score)
	{
	    this.assign = assign;
	    this.score = score;
	}

	private Assignment assign;

	private Real score;
    }

    private Alignment a;

    private Map<String,Model> m;

    private Map<String,Tree> t;
    
    private Map<RateCategory,AncestralJointDP> dps;
}
