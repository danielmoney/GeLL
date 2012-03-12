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

import Constraints.SiteConstraints;
import Alignments.Alignment;
import Alignments.Site;
import Constraints.Constrainer;
import Constraints.NoConstraints;
import Exceptions.UnexpectedError;
import Likelihood.Likelihood.LikelihoodException;
import Likelihood.Likelihood.NodeLikelihood;
import Likelihood.Likelihood.RateLikelihood;
import Likelihood.Likelihood.SiteLikelihood;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Calculates the likelihood for different parameter values.  Succesive calls
 * to an instance of this class are used to maximise the parameters values for
 * one case.  Uses the pruning technique of Felenstein 1981 and can account for
 * unobserved states using Felsenstein 1992.
 * @author Daniel Money
 * @version 1.0
 */
public class Calculator
{
    /**
     * Creates an object to calculate the likelihood for a given model, alignment,
     * tree.  Has no unobserved data or constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     */
    public Calculator(Model m, Alignment a, Tree t)
    {
        this(m,a,t,null,new NoConstraints(m.getStates()));
    }

    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree, missing data and constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     */    
    public Calculator(Model m, Alignment a, Tree t, Alignment unobserved)
    {
        this(m,a,t,unobserved,new NoConstraints(m.getStates()));
    }
 
    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree and constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param con Any constraints
     */
    public Calculator(Model m, Alignment a, Tree t, Constrainer con)
    {
        this(m,a,t,null,con);
    }
    
   
    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree, unobserved data and constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @param con Any constraints
     */
    public Calculator(Model m, Alignment a, Tree t, Alignment unobserved, Constrainer con)
    {
        this.m = m;
        this.a = a;
        this.t = t;
        this.missing = unobserved;
        this.con = con;
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
     *  
     */
    public Likelihood calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException
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
                    p.addParameter(Parameter.newEstimatedPositiveParameter(b.getChild(),false));
                }
            }
	}
        
        //The total ikelihood
        double l = 0.0;
        //Stores the likelihood of sites in the alignment
        Map<Site,SiteLikelihood> siteLikelihoods = new HashMap<>();
        //Stores the likelihood of unobserved states
        Map<Site,SiteLikelihood> missingLikelihoods = new HashMap<>();
        
        
        //Doing threaded calculation can be slower in small cases due to the
        //overhead in creating threads.  However haven't tested when this is the
        //case and is likely to depend on both tree and rate matrix size so for
        //now always doing it threaded.
        try
        {
            //Calculate all the probabilites associated with this model, tree and
            //set of parameters
            Probabilities tp = new Probabilities(m,t,p);
            
            //For each unique site in both the alignment and unobserved sites
            //create a callable object to calculate it and send it to
            // be executed.
            ExecutorService es = Executors.newFixedThreadPool(noThreads);

            Map<Site, SiteCalculator> sites = new HashMap<>();
            Map<Site, SiteCalculator> miss = new HashMap<>();
            
            for (Site s: a.getUniqueSites())
            {
                SiteCalculator temp = new SiteCalculator(s, t, con.getConstraints(t, s), tp);
                sites.put(s, temp);
                es.submit(temp);
            }
            if (missing != null)
            {
                for (Site s: missing.getUniqueSites())
                {
                    SiteCalculator temp = new SiteCalculator(s, t, con.getConstraints(t, s), tp);
                    miss.put(s, temp);
                    es.submit(temp);
                }
            }

            //Wait for all the site calculations to complete
            es.shutdown();
            es.awaitTermination(7, TimeUnit.DAYS);

            //Get the result for each site and calculate the total likelihood (l)
            //of the alignemnt taking into account how often each unique site occurs
            for (Entry<Site, SiteCalculator> e: sites.entrySet())
            {
                try
                {
                    SiteLikelihood sl = e.getValue().getResult();
                    siteLikelihoods.put(e.getKey(),sl);
                    l += a.getCount(e.getKey()) * Math.log(sl.getLikelihood());
                }
                catch(ResultNotComputed ex)
                {
                    //Shouldn't get here as the call to shutdown should ensure
                    //everythings finished before we do.
                    throw new UnexpectedError(ex);
                }
            }
            
            //Get the result for each site and calculate the total likelihood (m)
            //of the unobserved date.  Follows Felsenstein 1992.
            double ml = 0.0;
            for (Entry<Site, SiteCalculator> e: miss.entrySet())
            {
                try
                {
                    SiteLikelihood sl = e.getValue().getResult();
                    missingLikelihoods.put(e.getKey(),sl);
                    ml += sl.getLikelihood();
                }
                 catch(ResultNotComputed ex)
                {
                    //Shouldn't get here as the call to shutdown should ensure
                    //everythings finished before we do.
                    throw new UnexpectedError(ex);
                }
            }
            //Now modify the alignment likelihood to account for unobserved data,
            //again per Felsenstein 1992
            l = l - (a.getLength() * Math.log(1 - ml));
        }
        catch(InterruptedException ex)
        {
            //Don't think this should happen but in case it does...
            throw new UnexpectedError(ex);
        }
        return new Likelihood(l,siteLikelihoods,missingLikelihoods,p);
    }
    
    /**
     * Set the number of threads to be used during the calculations
     * @param number Number of threads
     */
    public static void setNoThreads(int number)
    {
        noThreads = number;
    }
    
    private Model m;
    private Alignment a;
    private Tree t;
    private Alignment missing;
    private Constrainer con;
    private static int noThreads = Runtime.getRuntime().availableProcessors();
    
    /**
     * Calculates the likelihood of a single site.  Implemeted like this as it
     * allows parrallel calculation.
     * @author Daniel Money
     * @version 1.0
     */
    public static class SiteCalculator implements Runnable
    {
        /**
         * Standard constructor
         * @param s Site
         * @param t Tree
         * @param con Constraints on that site
         * @param tp Pre-computed datastructure containing probabilities
         */
        public SiteCalculator(Site s, Tree t, SiteConstraints con, Probabilities tp)
        {
            this.s = s;
            this.t = t;
            this.tp = tp;
            this.con = con;
            result = null;
        }

        public void run()
        {
            result = calculate();
        }
        
        /**
         * Calculates the likelihood
         * @return Object containing the likelihood and the results of intermediate
         * calculations.
         */
        public SiteLikelihood calculate()
        {
            List<Branch> branches = t.getBranches();
            Map<RateCategory,RateLikelihood> rateLikelihoods = new HashMap<>();

            //Calculate the likelihood for each RateCategory
            for (RateCategory rc: tp.getRateCategory())
            {
                //Initalise the lieklihood values at each node.  first internal
                //using the alignemnt.
                LinkedHashMap<String, NodeLikelihood> nodeLikelihoods = new LinkedHashMap<>();
                for (String l: t.getLeaves())
                {
                    nodeLikelihoods.put(l, new NodeLikelihood(tp.getAllStates(), s.getCharacter(l)));
                }

                //And now internal nodes using any constraints
                for (String i: t.getInternal())
                {
                    nodeLikelihoods.put(i, new NodeLikelihood(tp.getAllStates(), con.getConstraint(i)));
                }

                //for each branch.  The order these are returned in from tree means
                //we visit any branch with a node as it's parent before we visit
                //the branch with the node as the child.  Hence we treverse the
                //tree in the standard manner.  For each branch we will update
                //the likelihood at the parent node...
                for (Branch b: branches)
                {
                    //So for each state at the parent node...
                    for (String endState: tp.getAllStates())
                    {
                        //l keeps track of the total likelihood from each possible
                        //state at the child
                        double l = 0.0;
                        //For each possible child state
                        for (String startState: tp.getAllStates())
                        {
                            try
                            {
                                //Add the likelihood of going from start state to
                                //end state along that branch in that ratecategory
                                l += nodeLikelihoods.get(b.getChild()).getLikelihood(startState) * tp.getP(rc, b, startState, endState);
                            }
                            catch (LikelihoodException ex)
                            {
                                //Shouldn't reach here as we know what oinformation should
                                // have been claculated and only ask for that
                                throw new UnexpectedError(ex);
                            }
                        }
                        //Now multiply the likelihood of the parent by this total value.
                        //This will happen for each possible child as per standard techniques
                        //If there is a constraint that the end state is not valid at this node
                        //then the likelihood would have been initialised to zero so will
                        //stay at zero.
                        nodeLikelihoods.get(b.getParent()).multiply(endState,l);
                    }
                }

                //Rate total traxcks the total likelihood for this site and rate category
                double ratetotal = 0.0;
                //Get the root likelihoods
                NodeLikelihood rootL = nodeLikelihoods.get(t.getRoot());
                //For each possible state
                for (String state: this.tp.getAllStates())
                {
                    try
                    {
                        //Get the likelihood at the root, multiply by it's root frequency
                        //and add to the ratde total.
                        ratetotal += rootL.getLikelihood(state) * tp.getFreq(rc,state);
                    }
                    catch (LikelihoodException ex)
                    {
                        //Shouldn't reach here as we know what oinformation should
                        // have been claculated and only ask for that
                        throw new UnexpectedError(ex);
                    }
                }
                //Store the results for that rate
                rateLikelihoods.put(rc, new RateLikelihood(ratetotal,nodeLikelihoods));
                //Update the total site likelihood with the likelihood for the rate
                //category multiplied by the probility of being in that category
            }
            //return an object containg the results for that site
            return new SiteLikelihood(rateLikelihoods, tp);
        }
        
        /**
         * Gets the computed result
         * @return The computed result
         * @throws Likelihood.Calculator.ResultNotComputed If the result has not
         * been computed for any reason.
         */
        public SiteLikelihood getResult() throws ResultNotComputed
        {
            if (result != null)
            {
                return result;
            }
            else
            {
                throw new ResultNotComputed();
            }
        }


        private Site s;
        private Tree t;
        private Probabilities tp;
        private SiteConstraints con;
        private SiteLikelihood result;
    }
    
    private static class ResultNotComputed extends Exception
    {
        
    }
}
