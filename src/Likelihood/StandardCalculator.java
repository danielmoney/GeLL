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

package Likelihood;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Site;
import Alignments.UniqueSite;
import Likelihood.Calculator.CalculatorException;
import Likelihood.Probabilities.RateProbabilities;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Likelihood.SiteLikelihood.RateLikelihood;
import Maths.Real;
import Maths.SquareMatrix;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameters;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the likelihood for different parameter values.  Succesive calls
 * to an instance of this class are used to maximise the parameters values for
 * one case.  Uses the pruning technique of Felenstein 1981 and can account for
 * unobserved states using Felsenstein 1992.
 * @author Daniel Money
 * @version 2.0
 */
public class StandardCalculator extends Calculator<StandardLikelihood>
{  
    /**
     * Creates an object to calculate the likelihood for a given model, alignment and
     * tree.  Has no unobserved data
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */
    public StandardCalculator(Model m, Alignment a, Tree t) throws TreeException, LikelihoodException, AlignmentException
    {
        this(m,a,t,null);
    }

    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree and missing data
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */    
    public StandardCalculator(Model m, Alignment a, Tree t, Alignment unobserved) throws TreeException, LikelihoodException, AlignmentException
    {
        this(makeModelMap(m),a,t,unobserved);
    }
 
    /**
     * Creates a class to calculate the likelihood for a given set of models, an alignment,
     * and a tree.  There should be one model per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @throws AlignmentException Thrown if a model isn't given for each site class
     * in the alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model). 
     */
    public StandardCalculator(Map<String,Model> m, Alignment a, Tree t) throws AlignmentException, TreeException, LikelihoodException
    {
        this(m,a,t,null);
    }

    /**
     * Creates a class to calculate the likelihood for a given set of models, an alignment,
     * a tree and unobserved data.  There should be one model and one
     * constrainer per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @throws AlignmentException Thrown if a model and constrainer isn't given
     * for each site class in the alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model). 
     */
    public StandardCalculator(Map<String,Model> m, Alignment a, Tree t, Alignment unobserved) throws AlignmentException, TreeException, LikelihoodException
    {
        super(m,t,getInitialNodeLikelihoods(m,a,t,unobserved));
        this.a = a;
        this.t = t;
        this.missing = unobserved;
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
    }
    

    public StandardLikelihood combineSites(Map<Site,SiteLikelihood> sites, Parameters p) throws CalculatorException
    {        
        //The total ikelihood
        double l = 0.0;
        //Stores the likelihood of sites in the alignment
        Map<Site,SiteLikelihood> siteLikelihoods = new HashMap<>(a.getUniqueSites().size());
        //Stores the likelihood of unobserved states
        Map<Site,SiteLikelihood> missingLikelihoods;
        if (missing != null)
        {
            missingLikelihoods = new HashMap<>(missing.getUniqueSites().size());
        }
        else
        {
            missingLikelihoods = null;
        }        

        //Get the result for each site and calculate the total likelihood (l)
        //of the alignemnt taking into account how often each unique site occurs
        //for (Entry<Site, SiteCalculator> e: sites.entrySet())
        for (UniqueSite us: a.getUniqueSites())
        {
            SiteLikelihood sl = sites.get(us);
            siteLikelihoods.put(us,sl);
            l += us.getCount() * sl.getLikelihood().ln();
        }
            
        //Get the result for each site and calculate the total likelihood (m)
        //of the unobserved data.  Follows Felsenstein 1992.
        HashMap<String, Real> ml = new HashMap<>();
        if (missing != null)
        {
            for (UniqueSite us: missing.getUniqueSites())
            {
                SiteLikelihood sl = sites.get(us);
                missingLikelihoods.put(us,sl);
                String sc = us.getSiteClass();
                if (ml.containsKey(sc))
                {
                    ml.put(sc, ml.get(sc).add(sl.getLikelihood()));
                }
                else
                {
                    ml.put(sc, sl.getLikelihood());
                }
            }
            //Now modify the alignment likelihood to account for unobserved data,
            //again per Felsenstein 1992
            for (String sc: ml.keySet())
            {
                l = l - (a.getClassSize(sc) * ml.get(sc).ln1m());
            }
        }
        if (l > 0)
        {
            throw new CalculatorException("Positive Log Likelihood");
        }
        if (Double.isNaN(l))
        {
            throw new CalculatorException("NaN Log Likelihood");
        }
        return new StandardLikelihood(l,siteLikelihoods,missingLikelihoods,p);
    }
    
    public SiteLikelihood calculateSite(Site s, Tree t, Parameters p, Probabilities tp, Map<String,NodeLikelihood> nl)
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
                        //Real l = SiteLikelihood.getReal(0.0);//new Real(0.0);
                        //For each possible child state
                        //for (String startState: tp.getAllStates())
                        Real[] n = nodeLikelihoods.get(b.getChild()).getLikelihoods();
                        //for (int j = 0; j < tp.getAllStates().size(); j ++)
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
                
                //For each possible state
                /*for (String state: this.tp.getAllStates())
                {
                    try
                    {
                        //Get the likelihood at the root, multiply by it's root frequency
                        //and add to the ratde total.
                        if (ratetotal == null)
                        {
                            ratetotal = rootL.getLikelihood(state).multiply(tp.getFreq(rc,state));
                        }
                        else
                        {
                            ratetotal = ratetotal.add(rootL.getLikelihood(state).multiply(tp.getFreq(rc,state)));
                        }
                    }
                    catch (LikelihoodException ex)
                    {
                        //Shouldn't reach here as we know what oinformation should
                        // have been claculated and only ask for that
                        throw new UnexpectedError(ex);
                    }
                }*/
                //Store the results for that rate
                rateLikelihoods.put(rc, new RateLikelihood(ratetotal,nodeLikelihoods));
                //Update the total site likelihood with the likelihood for the rate
                //category multiplied by the probility of being in that category
            }
            //return an object containg the results for that site
            return new SiteLikelihood(rateLikelihoods, tp);
    }
    
    private Alignment a;
    private Alignment missing;
    
    
    private static HashMap<Site,Map<String,NodeLikelihood>> getInitialNodeLikelihoods(Map<String,Model> m, Alignment a, Tree t, Alignment missing) 
            throws TreeException, LikelihoodException, AlignmentException
    {
        HashMap<Site,Map<String,NodeLikelihood>> snl = new HashMap<>();
        for (UniqueSite s: a.getUniqueSites())
        {
            Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(t.getNumberBranches() + 1);
            for (String l: t.getLeaves())
            {
                nodeLikelihoods.put(l, new NodeLikelihood(m.get(s.getSiteClass()).getMap(), s.getCharacter(l)));
            }

            //And now internal nodes using
            for (String i: t.getInternal())
            {
                nodeLikelihoods.put(i, new NodeLikelihood(m.get(s.getSiteClass()).getMap()));
            }
            snl.put(s, nodeLikelihoods);
        }
        if (missing != null)
        {
            for (UniqueSite s: missing.getUniqueSites())
            {
                Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(t.getNumberBranches() + 1);
                for (String l: t.getLeaves())
                {
                    nodeLikelihoods.put(l, new NodeLikelihood(m.get(s.getSiteClass()).getMap(), s.getCharacter(l)));
                }

                //And now internal nodes using
                for (String i: t.getInternal())
                {
                    nodeLikelihoods.put(i, new NodeLikelihood(m.get(s.getSiteClass()).getMap()));
                }
                snl.put(s, nodeLikelihoods);
            }
        }
        return snl;
    }
    
    private static Map<String,Model> makeModelMap(Model m)
    {
        HashMap<String, Model> mm = new HashMap<>();
        mm.put(null,m);
        return mm;
    }
}
