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

import Alignments.Site;
import Exceptions.GeneralException;
import Exceptions.UnexpectedError;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Optimizers.Optimizable;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
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
 * Abstract class for calculating a likelihood.  Likelihood calculators should
 * extend this class as then this class will deal with multi-threading.
 * @author Daniel Money
 * @version 2.0
 * @param <R> The return type from the calculation
 */
public abstract class Calculator<R extends Likelihood> implements Optimizable<R>
{
    /**
     * Default constructor.  The Site-Node-Likelihoods (snl) are passed here
     * as it is much quicker to clone them for each calculation than re-create them
     * @param m The model
     * @param t The tree
     * @param snl The site node likelihoods.  That is the initial likelihood values
     * for each state at each node for each site.  Will usually be 1.0 if that state at that
     * node of that site is possible, else 0.0.
     */
    protected Calculator(Map<String,Model> m, Map<String,Tree> t, HashMap<Site,Map<String,NodeLikelihood>> snl)
    {
        this.snl = snl;
        this.t = t;
        this.m = m;
    }

    /**
     * Abstract method for actually calculating the likelihood
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
     * with the parameters (e.g. a required parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive
     * or NaN) log likelihood is calculated 
     */
    public R calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.  If the branch has a length add
        //it as a fixed parameter, else as an estimated parameter.
        for (Tree tt: t.values())
        {
            for (Branch b: tt)
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
        }
        
        Map<Site,SiteLikelihood> sites = siteCalculate(p);
        
        return combineSites(sites, p);
    }
    
    /**
     * Combines the likelihood from each site into a alignment likelihood
     * @param sites A map from sites to the likelihood of those sites.
     * @param p The parameters to use in the calculation
     * @return The likelihood of the alignment
     * @throws TreeException Thrown if there is a problem with the Tree (e.g. if
     * there is a branch with no length given in parameters)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a required parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive
     * or NaN) log likelihood is calculated 
     */
    public abstract R combineSites(Map<Site,SiteLikelihood> sites, Parameters p) throws CalculatorException, TreeException, ParameterException, RateException, ModelException;
    
    /**
     * Calculate the  likelihood for a single site.
     * @param s The site to calculate the likelihood for
     * @param t The tree to use in calculating the likelihood
     * @param p The parameters to use in calculating the likelihood
     * @param tp Pre-calculated transition probabilities to be used in the
     * calculation (passed in as they don't need to be calculated separately for
     * each site).  See {@link Probabilities} for more information
     * @param nl The starting node-likelihoods are passed here as it is much 
     * quicker to clone them for each calculation than re-create them
     * @return An instance of {@link SiteLikelihood} given the likelihood and
     * any other information
     */
    public abstract SiteLikelihood calculateSite(Site s, Tree t, Parameters p, Probabilities tp, Map<String,NodeLikelihood> nl) throws ParameterException;
    
    /**
     * Calculates the likelihood for each site
     * @param p The parameters to be used in the calculation
     * @return A Map from site to result
     * @throws TreeException Thrown if there is a problem with the Tree (e.g. if
     * there is a branch with no length given in parameters)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a required parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive
     * or NaN) log likelihood is calculated 
     */
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
            Map<String,Probabilities> tp = new HashMap<>(m.size());
            for (Entry<String,Model> e: m.entrySet())
            {
                tp.put(e.getKey(), new Probabilities(e.getValue(),t.get(e.getKey()),p));
            }
            
            if (thread)
            {
                //For each unique site in both the alignment and unobserved sites
                //create a callable object to calculate it and send it to
                // be executed.
                Map<Site, SiteCalculator> sites = new HashMap<>(snl.size());

                List<SiteCalculator> scs = new ArrayList<>();
                for (Entry<Site,Map<String,NodeLikelihood>> e: snl.entrySet())
                {
                    SiteCalculator temp = new SiteCalculator(e.getKey(),t.get(e.getKey().getSiteClass()),p,
                            tp.get(e.getKey().getSiteClass()),
                            e.getValue());
                    scs.add(temp);
                    sites.put(e.getKey(), temp);
                }

                es.invokeAll(scs);

                Map<Site, SiteLikelihood> ret = new HashMap<>(snl.size());
                for (Entry<Site,SiteCalculator> e: sites.entrySet())
                {
                    ret.put(e.getKey(),e.getValue().getResult());
                }

                return ret;
            }
            else
            {
                Map<Site, SiteLikelihood> ret = new HashMap<>(snl.size());
                for (Entry<Site,Map<String,NodeLikelihood>> e: snl.entrySet())
                {
                    ret.put(e.getKey(), calculateSite(e.getKey(),t.get(e.getKey().getSiteClass()),p,tp.get(e.getKey().getSiteClass()),e.getValue()));
                }
                return ret;
            }
        }

        catch(InterruptedException | ResultNotComputed ex)
        {
            //Don't think this should happen but in case it does...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Set whether threaded calculations should be performed
     * @param thread Whether to perform threaded calculations
     */
    public void setThread(boolean thread)
    {
        this.thread = thread;
    }
    
    public abstract int getAlignmentLength();

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
    
    private boolean thread = true;
    
    /**
     * The site node likelihoods.  That is the initial likelihood values
     * for each state at each node for each site.  Will usually be 1.0 if that state at that
     * node of that site is possible, else 0.0.  Should be used by implementing classes.
     */
    protected HashMap<Site,Map<String,NodeLikelihood>> snl;
    /**
     * The tree to do the calculation on.  Should be used by implementing classes.
     */
    protected Map<String,Tree> t;
    /**
     * The model to do the calculation on.  Should be used by implementing classes.
     */
    protected Map<String,Model> m;
    
    /**
     * Calculates the likelihood of a single site.  Implemeted like this as it
     * allows parrallel calculation.
     * @author Daniel Money
     * @version 2.0
     */
    public class SiteCalculator implements Callable<SiteLikelihood> //Runnable
    {
        /**
         * Standard constructor
         * @param s The site to calculate the likelihood for
         * @param t The tree to be used in the calculation
         * @param p The parameters to be used in the calculation
         * @param tp Pre-computed datastructure containing probabilities
         * @param nl Initialised node likelihoods based on the site.
         * See {@link Site#getInitialNodeLikelihoods(Trees.Tree, java.util.Map)}.
         */
        public SiteCalculator(Site s, Tree t, Parameters p, Probabilities tp, Map<String,NodeLikelihood> nl)
        {
            this.s = s;
            this.t = t;
            this.p = p;
            this.tp = tp;
            this.nl = nl;
            result = null;
        }
        
        public SiteLikelihood call() throws ParameterException
        {
            result = calculateSite(s,t,p,tp,nl);
            return result;
            //result = calculate();
            //return result;            
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
        private Parameters p;
        private Probabilities tp;
        private SiteLikelihood result;
        private Map<String,NodeLikelihood> nl;
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
         * Currently used when there is a problem constructing the model,
         * e.g. different number of states in the RateClasses.
         * @param reason The reason for the exception
         */
        public CalculatorException(String reason)
        {
            super("Rates Exception\n\tReason:\t" + reason,null);
        }
        
        /**
         * Constructor when there is an underlying Throwable that caused the problem.
         * @param reason The reason for the exception
         * @param cause The underlying throwable
         */
        public CalculatorException(String reason, Exception cause)
        {
            super("Rates Exception\n\tReason:\t" + reason,cause);
        }
    }
}
