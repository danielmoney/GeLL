/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Likelihood;

import Alignments.Site;
import Constraints.SiteConstraints;
import Exceptions.GeneralException;
import Exceptions.UnexpectedError;
import Likelihood.Probabilities.RateProbabilities;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Likelihood.SiteLikelihood.RateLikelihood;
import Maths.SquareMatrix;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory;
import Models.RateCategory.RateException;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import Utils.ArrayMap;
import Utils.DaemonThreadFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Daniel Money
 */
public abstract class BasicCalculator<R extends BasicLikelihood>
{
    protected BasicCalculator(Map<String,Model> m, Tree t, HashMap<Site,ArrayMap<String,NodeLikelihood>> snl)
    {
        this.snl = snl;
        this.t = t;
        this.m = m;
    }
    
    public abstract R calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException;
    
    protected ArrayMap<Site,SiteLikelihood> siteCalculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException
    {
        //Doing threaded calculation can be slower in small cases due to the
        //overhead in creating threads.  However haven't tested when this is the
        //case and is likely to depend on both tree and rate matrix size so for
        //now always doing it threaded.
        try
        {
            //Calculate all the probabilites associated with this model, tree and
            //set of parameters
            ArrayMap<String,Probabilities> tp = new ArrayMap<>(String.class,Probabilities.class,m.size());
            for (Entry<String,Model> e: m.entrySet())
            {
                tp.put(e.getKey(), new Probabilities(e.getValue(),t,p));
            }
            
            //For each unique site in both the alignment and unobserved sites
            //create a callable object to calculate it and send it to
            // be executed.
            ArrayMap<Site, SiteCalculator> sites = new ArrayMap<>(Site.class,SiteCalculator.class,snl.size());
            
            List<SiteCalculator> scs = new ArrayList<>();
            for (Entry<Site,ArrayMap<String,NodeLikelihood>> e: snl.entrySet())
            {
                SiteCalculator temp = new SiteCalculator(t, 
                        tp.get(e.getKey().getSiteClass()),
                        e.getValue());
                scs.add(temp);
                sites.put(e.getKey(), temp);
            }
            
            es.invokeAll(scs);
                        
            ArrayMap<Site, SiteLikelihood> ret = new ArrayMap<>(Site.class,SiteLikelihood.class,snl.size());
            for (int i = 0; i < ret.size(); i++)
            {
                Entry<Site,SiteCalculator> e = sites.getEntry(i);
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
    
    private HashMap<Site,ArrayMap<String,NodeLikelihood>> snl;
    private Tree t;
    private Map<String,Model> m;
    
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
   
    /**
     * Calculates the likelihood of a single site.  Implemeted like this as it
     * allows parrallel calculation.
     * @author Daniel Money
     * @version 1.0
     */
    public static class SiteCalculator implements Callable<SiteLikelihood> //Runnable
    {
        /**
         * Standard constructor
         * @param t Tree
         * @param tp Pre-computed datastructure containing probabilities
         * @param nl Initalised node likelihoods based on the site and any constraints.
         * See {@link Site#getInitialNodeLikelihoods(Trees.Tree, Utils.ArrayMap, Constraints.SiteConstraints)}.
         */
        public SiteCalculator(Tree t, Probabilities tp, ArrayMap<String,NodeLikelihood> nl)
        {
            this.t = t;
            this.tp = tp;
            this.nl = nl;
            result = null;
        }
        
        /**
         * Constructor mainly for backwards compitability although also still used by
         * some of the ancestral calculations.  Calculates the initial node likelihoods
         * as part of the constructor
         * @param s The site
         * @param t The tree
         * @param con Constraints on the site
         * @param tp Pre-computed datastructure containing probabilities
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
         *      (most probably due to the state at the node not being in the model). 
         */
        public SiteCalculator(Site s, Tree t, SiteConstraints con, Probabilities tp) throws LikelihoodException
        {
            this.t = t;
            this.tp = tp;
            result = null;
            
            //Create the initial values for the node likelihoods based on the tip assignment
            nl = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
            for (String l: t.getLeaves())
            {
                nl.put(l, new NodeLikelihood(tp.getArrayMap(), s.getCharacter(l)));
            }

            //And now internal nodes using any constraints
            for (String i: t.getInternal())
            {
                nl.put(i, new NodeLikelihood(tp.getArrayMap(), con.getConstraint(i)));
            }
        }

        public SiteLikelihood call()//void run()
        {
            result = calculate();
            return result;            
        }
        
        /**
         * Calculates the likelihood
         * @return Object containing the likelihood and the results of intermediate
         * calculations.
         */
        public SiteLikelihood calculate()
        {
            List<Branch> branches = t.getBranches();
            ArrayMap<RateCategory,RateLikelihood> rateLikelihoods = new ArrayMap<>(RateCategory.class,RateLikelihood.class,tp.getRateCategory().size());

            //Calculate the likelihood for each RateCategory
            for (RateCategory rc: tp.getRateCategory())
            {
                RateProbabilities rp = tp.getP(rc);
                //Initalise the lieklihood values at each node.  first internal
                //using the alignemnt.
                ArrayMap<String, NodeLikelihood> nodeLikelihoods = new ArrayMap<>(String.class,NodeLikelihood.class,branches.size() + 1);
                for (String l: t.getLeaves())
                {
                    nodeLikelihoods.put(l, nl.get(l).clone());
                }

                //And now internal nodes using any constraints
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
                    for (int i = 0; i < tp.getAllStatesAsList().size(); i ++)
                    {
                        String endState = tp.getAllStatesAsList().get(i);
                        //l keeps track of the total likelihood from each possible
                        //state at the child
                        double l = 0.0;
                        //For each possible child state
                        //for (String startState: tp.getAllStates())
                        double[] nl = nodeLikelihoods.get(b.getChild()).getLikelihoods();
                        //for (int j = 0; j < tp.getAllStates().size(); j ++)
                        for (int j = 0; j < nl.length; j++)
                        {
                            //Add the likelihood of going from start state to
                            //end state along that branch in that ratecategory
                            l += nl[j] * bp.getPosition(i, j);
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
                for (String state: this.tp.getAllStatesAsList())
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
        private Tree t;
        private Probabilities tp;
        private SiteLikelihood result;
        private ArrayMap<String,NodeLikelihood> nl;
    }
    
    static class ResultNotComputed extends Exception
    {
        
    }
    
    /**
     * Exception thrown when there is a problem with the calculation
     */
    public static class CalculatorException extends GeneralException
    {
        /**
         * Constructor when there is no underlying Throwable that caused the problem.
         * Currnetly used when there is a problem constructing the model,
         * e.g. different number of states in the RateClasses.
         * @param reason The reason for the exception
         */
        public CalculatorException(String reason)
        {
            super("Rates Exception\n\tReason:\t" + reason,null);
        }
    }
}
