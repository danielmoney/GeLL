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
import Exceptions.UnexpectedError;
import Likelihood.Calculator.CalculatorException;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Maths.Real;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
//AM import Utils.ArrayMap;
import Utils.DaemonThreadFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    
    /**
     * Calculates the likelihood for a given set of parameters
     * @param p The parameters to be used in the calculation
     * @return A Likelihood object which contains the likelihood as well as
     * likelihoods for each site.
     * @throws TreeException Thrown if there is a problem with the Tree (e.g. if
     * there is a branch with no length given in parameters)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive or NaN) log likelihood is calculated 
     */
    public StandardLikelihood calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.  If the branch has a length add
        //it as a fixed parameter, else as an estimated parameter.
        for (Branch b: t)
	{
            if (!p.hasParam(b.getChild()))
            {
                if (b.hasLength())
                {
                    p.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));
                }
                else
                {
                    p.addParameter(Parameter.newEstimatedPositiveParameter(b.getChild()));
                }
            }
	}
        
        //The total ikelihood
        double l = 0.0;
        //Stores the likelihood of sites in the alignment
        //AM ArrayMap<Site,SiteLikelihood> siteLikelihoods = new ArrayMap<>(Site.class,SiteLikelihood.class,a.getUniqueSites().size());
        Map<Site,SiteLikelihood> siteLikelihoods = new HashMap<>(a.getUniqueSites().size());
        //Stores the likelihood of unobserved states
        //AM ArrayMap<Site,SiteLikelihood> missingLikelihoods;
        Map<Site,SiteLikelihood> missingLikelihoods;
        if (missing != null)
        {
            //AM missingLikelihoods = new ArrayMap<>(Site.class,SiteLikelihood.class,missing.getUniqueSites().size());
            missingLikelihoods = new HashMap<>(missing.getUniqueSites().size());
        }
        else
        {
            missingLikelihoods = null;
        }
            
        //AM ArrayMap<Site,SiteLikelihood> sites = siteCalculate(p);
        Map<Site,SiteLikelihood> sites = siteCalculate(p);

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
    
    /**
     * Calculates the likelihood for each site
     * @param p The parameters to be used in the calcl
     * @return A Map from site to result
     * @throws TreeException Thrown if there is a problem with the Tree (e.g. if
     * there is a branch with no length given in parameters)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive
     * or NaN) log likelihood is calculated 
     */
    //AM protected ArrayMap<Site,SiteLikelihood> siteCalculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException
    protected Map<Site,SiteLikelihood> siteCalculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException
    {
        //Doing threaded calculation can be slower in small cases due to the
        //overhead in creating threads.  However haven't tested when this is the
        //case and is likely to depend on both tree and rate matrix size so for
        //now always doing it threaded.
        try
        {
            //Calculate all the probabilites associated with this model, tree and
            //set of parameters
            //AM ArrayMap<String,Probabilities> tp = new ArrayMap<>(String.class,Probabilities.class,m.size());
            Map<String,Probabilities> tp = new HashMap<>(m.size());
            for (Entry<String,Model> e: m.entrySet())
            {
                tp.put(e.getKey(), new Probabilities(e.getValue(),t,p));
            }
            
            //For each unique site in both the alignment and unobserved sites
            //create a callable object to calculate it and send it to
            // be executed.
            //AM ArrayMap<Site, SiteCalculator> sites = new ArrayMap<>(Site.class,SiteCalculator.class,snl.size());
            Map<Site, SiteCalculator> sites = new HashMap<>(snl.size());
            
            List<SiteCalculator> scs = new ArrayList<>();
            //AM for (Entry<Site,ArrayMap<String,NodeLikelihood>> e: snl.entrySet())
            for (Entry<Site,Map<String,NodeLikelihood>> e: snl.entrySet())
            {
                SiteCalculator temp = new SiteCalculator(t, 
                        tp.get(e.getKey().getSiteClass()),
                        e.getValue());
                scs.add(temp);
                sites.put(e.getKey(), temp);
            }
            
            es.invokeAll(scs);
                        
            //AM ArrayMap<Site, SiteLikelihood> ret = new ArrayMap<>(Site.class,SiteLikelihood.class,snl.size());
            Map<Site, SiteLikelihood> ret = new HashMap<>(snl.size());
            //AM for (int i = 0; i < ret.size(); i++)
            for (Entry<Site,SiteCalculator> e: sites.entrySet())
            {
                //AM Entry<Site,SiteCalculator> e = sites.getEntry(i);
                ret.put(e.getKey(),e.getValue().getResult());
            }
            
            return ret;
        }

        catch(InterruptedException | ResultNotComputed ex)
        {
            //Don't think this should happen but in case it does...
            throw new UnexpectedError(ex);
        }
    }
    
        /**
     * Set the number of threads to be used during the calculations
     * @param number Number of threads
     */
    public static void setNoThreads(int number)
    {
        es = Executors.newFixedThreadPool(number, new DaemonThreadFactory());
    }
    
    private static ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new DaemonThreadFactory());
    
    private Alignment a;
    private Tree t;
    private Alignment missing;
    
    
    //AM private static HashMap<Site,ArrayMap<String,NodeLikelihood>> getInitialNodeLikelihoods(Map<String,Model> m, Alignment a, Tree t, Alignment missing) 
    private static HashMap<Site,Map<String,NodeLikelihood>> getInitialNodeLikelihoods(Map<String,Model> m, Alignment a, Tree t, Alignment missing) 
            throws TreeException, LikelihoodException, AlignmentException
    {
        //AM HashMap<Site,ArrayMap<String,NodeLikelihood>> snl = new HashMap<>();
        HashMap<Site,Map<String,NodeLikelihood>> snl = new HashMap<>();
        for (UniqueSite s: a.getUniqueSites())
        {
            //AM ArrayMap<String, NodeLikelihood> nodeLikelihoods = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
            Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(t.getNumberBranches() + 1);
            for (String l: t.getLeaves())
            {
                nodeLikelihoods.put(l, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap(), s.getCharacter(l)));
            }

            //And now internal nodes using
            for (String i: t.getInternal())
            {
                nodeLikelihoods.put(i, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap()));
            }
            snl.put(s, nodeLikelihoods);
        }
        if (missing != null)
        {
            for (UniqueSite s: missing.getUniqueSites())
            {
                //AM ArrayMap<String, NodeLikelihood> nodeLikelihoods = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
                Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(t.getNumberBranches() + 1);
                for (String l: t.getLeaves())
                {
                    nodeLikelihoods.put(l, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap(), s.getCharacter(l)));
                }

                //And now internal nodes using
                for (String i: t.getInternal())
                {
                    nodeLikelihoods.put(i, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap()));
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
