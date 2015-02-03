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

import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Site;
import Likelihood.Probabilities;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Maths.Real;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to perform marginal reconstruction of internal nodes.  Uses the principles of
 * Yang, Kurma and Nei 1995.
 * @author Daniel Money
 * @version 2.0
 */
public class AncestralMarginal
{
    /**
     * Creates an object to calculate an ancestral reconstruction for a given
     * model, alignment and tree
     * @param m The model
     * @param a The alignment
     * @param t The tree
     */
    public AncestralMarginal(Model m, Alignment a, Tree t)
    {
	this.a = a;
        this.m = new HashMap<>();
	this.m.put(null, m); 
        this.t = new HashMap<>();
        this.t.put(null,t);
    }

    /**
     * Creates an object to calculate an ancestral reconstruction for a given
     * model, alignment and tree
     * @param m The model
     * @param a The alignment
     * @param t Map from site class to tree
     * @throws AlignmentException Thrown if a tree isn't given
     * for each site class in the alignment  
     */
    public AncestralMarginal(Model m, Alignment a, Map<String,Tree> t) throws AlignmentException
    {
	this.a = a;
        this.m = new HashMap<>();
        for (String s: t.keySet())
        {
            this.m.put(s, m); 
        }
        this.t = t;
        if (!a.check(t))
        {
            throw new AlignmentException("Alignment contains classes for which no tree has been defined");
        }
    }

    /**
     * Creates an object to calculate an ancestral reconstruction for a given
     * set of models, an alignment and a tree.  There should be one model
     * per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @throws AlignmentException Thrown if a model isn't given
     * for each site class in the alignment  
     */
    public AncestralMarginal(Map<String,Model> m, Alignment a, Tree t) throws AlignmentException
    {
        this.a = a;
        this.m = m;
        this.t = new HashMap<>();
        for (String s: m.keySet())
        {
            this.t.put(s,t);
        }
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
    }

    /**
     * Creates an object to calculate an ancestral reconstruction for a given
     * set of models, an alignment and a tree.  There should be one model
     * per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t Map from site class to tree
     * @throws AlignmentException Thrown if a model or tree isn't given
     * for each site class in the alignment  
     */
    public AncestralMarginal(Map<String,Model> m, Alignment a, Map<String,Tree> t) throws AlignmentException
    {
        this.a = a;
        this.m = m;
        this.t = t;
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
        if (!a.check(t))
        {
            throw new AlignmentException("Alignment contains classes for which no tree has been defined");
        }
    }
      
    /**
     * Calculates the reconstruction
     * @param params The parameters to be used in the reconstruction
     * @return An object containing the reconstruction and the probability of 
     * each state at each node for each site
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if there is a
     * problem with calculating likelihoods
     */    
    public Result calculate(Parameters params) throws RateException, ModelException, TreeException, ParameterException, AlignmentException, LikelihoodException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Tree tt: t.values())
        {
            for (Branch b: tt)
            {
                if (!params.hasParam(b.getChild()))
                {
                    params.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));

                }
            }
        }
        
        //Calculate probabilities for this model, tree and set of parameters
        Map<String,Probabilities> P = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(), new Probabilities(e.getValue(),t.get(e.getKey()),params));
        }
	
        //Get unqiue sites in the alignment and calculator a reconstuction for each
        Map<Site,SiteResult> ret = new HashMap<>();
	for (Site s: a.getUniqueSites())
	{
            ret.put(s,calculateSite(s,P.get(s.getSiteClass())));
	}

        //Create and return the result - making the new alignment etc is taken
        //care of in the Result constructor
	return new Result(ret,a);
    }

    /**
     * Calculates a reconstruction for a single site.  Is more low level than
     * the {@link #calculate(Parameters.Parameters)} as it ignores the alignment
     * and model passed when creating the object.  Is intended mainly to be used
     * internally.
     * @param s The site to do the reconstruction for
     * @param P A probability structure
     * @return An object containing the reconstruction and the probability of 
     * each state at each node
     * @throws TreeException Thrown if there is a problem with the tree. 
     */
    SiteResult calculateSite(Site s, Probabilities P) throws TreeException, AlignmentException, LikelihoodException, RateException
    {
        //Calculalate the probability of each state for each node...
        Map<String,HashMap<String,Real>> nr = new HashMap<>();
        for (String node: t.get(s.getSiteClass()).getInternal())
        {
            nr.put(node,calculateNode(s,P,node));
        }
        
        //And combine the node results (done in SiteResult constructor) and return
        return new SiteResult(nr,s);
    }
    
    private HashMap<String,Real> calculateNode(Site s, Probabilities P, String node) throws TreeException, AlignmentException, LikelihoodException, RateException
    {
        //As we want to be able to use non-time reversible models we can't use
        //the normal re-root the tree at this node trick.  Instead we do a variation
        //on it where we effecitively treat this node as the root but calculate which
        //branches will be going "backwards" in time and account for this.  We also
        //still aply the root frequencies at the original root
        
        //Keep track of what branches are normal and which are going "backwards"
        List<Branch> reverse = new ArrayList<>();
        List<Branch> normal = new ArrayList<>(t.get(s.getSiteClass()).getBranches());

        //Branches on the path between the current node and the root will be
        //going backwards
        String cur = node;
        while (!(cur.equals(t.get(s.getSiteClass()).getRoot())))
        {
            Branch b = t.get(s.getSiteClass()).getBranchByChild(cur);
            reverse.add(b);
            normal.remove(b);
            cur = b.getParent();            
        }
        Collections.reverse(reverse);
        
        //For each rate categoy...
        Map<RateCategory, NodeLikelihood> rr = new HashMap<>();
        for (RateCategory r: m.get(s.getSiteClass()))
        {
            //Initalise the nodes in the same manner as for a normal likelihood
            //caluclation
            Map<String,NodeLikelihood> l = new HashMap<>();
            for (String n: t.get(s.getSiteClass()).getLeaves())
            {
                l.put(n, new NodeLikelihood(P.getMap(), s.getCharacter(n)));
            }

            for (String n: t.get(s.getSiteClass()).getInternal())
            {
                l.put(n, new NodeLikelihood(P.getMap()));
            }

            //Traverse the normal branches in the same manner as for a normal
            //likelihood calculation
            for (Branch b: normal)
            {
                for (String endState: P.getAllStates())
                {
                    Real li = null;
                    for (String startState: P.getAllStates())
                    {
                        //This will be a little slow but given that we don't optimise
                        //using this it will never get called that often so speeding
                        //it up is not a priority
                        if (li == null)
                        {
                            li = l.get(b.getChild()).getLikelihood(startState).multiply(P.getP(r).getP(b, startState, endState));
                        }
                        else
                        {
                            li = li.add(l.get(b.getChild()).getLikelihood(startState).multiply(P.getP(r).getP(b, startState, endState)));
                        }
                    }
                    l.get(b.getParent()).multiply(endState,li);
                }
            }

            //Apply the root frequencies to the original root
            for (String st: P.getAllStates())
            {
                //l.get(t.getRoot()).multiply(st, P.getFreq(r, st));
                l.get(t.get(s.getSiteClass()).getRoot()).multiply(st, P.getRoot(r).getFreq(st));
            }
            
            //Now traverse the "backwards" branches in a similar manner to normal
            //excpet the start and end states are swapper
            for (Branch b: reverse)
            {
                for (String endState: P.getAllStates())
                {
                    //Real li = 0.0;
                    Real li = null;
                    for (String startState: P.getAllStates())
                    {
                        //This will be a little slow but given that we don't optimise
                        //using this it will never get called that often so speeding
                        //it up is not a priority
                        if (li == null)
                        {
                            li = l.get(b.getParent()).getLikelihood(startState).multiply(P.getP(r).getP(b, endState, startState));
                        }
                        else
                        {
                            li = li.add(l.get(b.getParent()).getLikelihood(startState).multiply(P.getP(r).getP(b, endState, startState)));
                        }
                    }
                    l.get(b.getChild()).multiply(endState,li);
                }
            }
            //Store the result for this rate
            rr.put(r,l.get(node));
        }
        
        //Calculate the likelihood for each state by summing accross Rate
        //Categories (accoutning for different frequency of Rate Category).
        //Also calculate the total likelihood.
        Real total = null;
        HashMap<String,Real> sl = new HashMap<>();
        for (String st : P.getAllStates())
        {
            for (RateCategory r: m.get(s.getSiteClass()))
            {
                if (sl.get(st) == null)
                {
                    sl.put(st, rr.get(r).getLikelihood(st).multiply(m.get(s.getSiteClass()).getFreq(r)));
                }
                else
                {
                    sl.put(st, sl.get(st).add(rr.get(r).getLikelihood(st).multiply(m.get(s.getSiteClass()).getFreq(r))));
                }
            }
            if (total == null)
            {
                total = sl.get(st);
            }
            else
            {
                total = total.add(sl.get(st));
            }
        }
        
        //Divide each state probability by the total probability to get the
        //likelihood of a state
        HashMap<String,Real> sP = new HashMap<>();
        for (String st: P.getAllStates())
        {
            sP.put(st, sl.get(st).divide(total));
        }
        
        return sP;
    }
    
    private Alignment a;
    private Map<String,Model> m;
    private Map<String,Tree> t;

    /**
     * Returns an object of this class that can be used for marginal 
     * reconstruction.  Included for consistency with {@link AncestralJoint}
     * which requires a static method as there is two different classes that
     * are used based on the situation.
     * @param a The alignment
     * @param m The model
     * @param t The tree
     * @return An object that can be used for reconstruction
     */
    public static AncestralMarginal newInstance(Model m, Alignment a, Tree t)
    {
        return new AncestralMarginal(m,a,t);
    }

    /**
     * Returns an object of this class that can be used for marginal 
     * reconstruction.  Sites can belong to different classes and so use 
     * different models. Included for consistency with {@link AncestralJoint}
     * which requires a static method as there is two different classes that
     * are used based on the situation.
     * @param a The alignment
     * @param m Map from site class to model
     * @param t The tree
     * @return An object that can be used for reconstruction
     * @throws AlignmentException Thrown if a model isn't given
     * for each site class in the alignment  
     */
    public static AncestralMarginal newInstance(Map<String,Model> m, Alignment a, Tree t) throws AlignmentException
    {
        return new AncestralMarginal(m,a,t);
    }
    
    /**
     * Class to store the results of a marginal ancestral reconstruction
     */
    public class Result
    {
        private Result(Map<Site,SiteResult> sr, Alignment oa) throws AlignmentException
        {
            //Create the alignment from tjhe unique site results
            List<Site> na = new ArrayList<>();
            for (Site s: oa)
            {
                na.add(sr.get(s).getSite());
            }
            a = new Alignment(na);
            this.sr = sr;
        }
        
        /**
         * Gets the reconstructed alignment
         * @return The reconstructed alignment
         */
        public Alignment getAlignment()
        {
            return a;
        }
        
        /**
         * Gets the site results for a given site
         * @param s The site
         * @return The results for that site
         * @throws AncestralException If there is no result for that site
         */
        public SiteResult getSiteResult(Site s) throws AncestralException
        {
            if (sr.containsKey(s))
            {
                return sr.get(s);
            }
            else
            {
                throw new AncestralException("No result for site " + s, null);
            }
        }
        
        private Map<Site,SiteResult> sr;
        private Alignment a;
    }
    
    /**
     * Class to store the results of a marginal ancestral reconstruction for a single
     * site
     */
    public class SiteResult
    {
        private SiteResult(Map<String, HashMap<String,Real>> nr, Site os) throws AlignmentException
        {
            this.nr = nr;
            
            //Create the site by
            LinkedHashMap<String,String> s = new LinkedHashMap<>();
            //First copying the leave states from the original site
            for (String l: t.get(os.getSiteClass()).getLeaves())
            {
                s.put(l, os.getRawCharacter(l));
            }
            //And then adding the reconstrcuted results
            for (String i: t.get(os.getSiteClass()).getInternal())
            {
                s.put(i, getMaxKey(nr.get(i)));
            }
            site = new Site(s);
        }
        
        /**
         * Returns the reconstructed site
         * @return The reconstructed site
         */
        public Site getSite()
        {
            return site;
        }
        
        /**
         * Gets thr probability of a given state at a given node
         * @param node The node
         * @param state The state
         * @return The probability of the state at the node
         * @throws AncestralException If there is no result for that node/state 
         * combination
         */
        public Real getProbability(String node, String state) throws AncestralException
        {
            if (nr.containsKey(node))
            {
                if (nr.get(node).containsKey(state))
                {
                    return nr.get(node).get(state);
                }
            }
            throw new AncestralException("No result for node " + node +
                    " and state " + state, null);
        }
        
        private String getMaxKey(HashMap<String,Real> map)
        {
            Real mv = null;
            String mk = null;
            for (Entry<String,Real> e: map.entrySet())
            {
                if ((mv == null) || (e.getValue().greaterThan(mv)))
                {
                    mk = e.getKey();
                    mv = e.getValue();
                }
            }

            return mk;
        }
        
        private Site site;
        private Map<String, HashMap<String, Real>> nr;
    }
}
