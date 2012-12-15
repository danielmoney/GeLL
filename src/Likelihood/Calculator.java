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
import Likelihood.Probabilities.RateProbabilities;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Likelihood.SiteLikelihood.RateLikelihood;
import Maths.Real;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Abstract class for calculating a likelihood
 * @author Daniel Money
 * @version 2.0
 * @param <R> The return type from the calculation
 */
public abstract class Calculator<R extends Likelihood>
{
    /**
     * Default constructor.  The Site-Node-Likelihoods (snl) are passed here
     * as it is much quicker to clone them for each calcualtion than re-create them
     * @param m The model
     * @param t The tree
     * @param snl The site node likelihoods.  That is the initial likelihood values
     * for each state at each node for each site.  Will usually be 1.0 if that state at that
     * node of that site is possible, else 0.0.
     */
    protected Calculator(Map<String,Model> m, Tree t, HashMap<Site,ArrayMap<String,NodeLikelihood>> snl)
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
     * with the parameters (e.g. a requied parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive
     * or NaN) log likelihood is calculated 
     */
    public abstract R calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException;
    
    
    /**
     * The site node likelihoods.  That is the initial likelihood values
     * for each state at each node for each site.  Will usually be 1.0 if that state at that
     * node of that site is possible, else 0.0.  Should be used by implementing classes.
     */
    protected HashMap<Site,ArrayMap<String,NodeLikelihood>> snl;
    /**
     * The tree to do the calculation on.  Should be used by implementing classes.
     */
    protected Tree t;
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
    public static class SiteCalculator implements Callable<SiteLikelihood> //Runnable
    {
        /**
         * Standard constructor
         * @param t Tree
         * @param tp Pre-computed datastructure containing probabilities
         * @param nl Initalised node likelihoods based on the site.
         * See {@link Site#getInitialNodeLikelihoods(Trees.Tree, Utils.ArrayMap)}.
         */
        public SiteCalculator(Tree t, Probabilities tp, ArrayMap<String,NodeLikelihood> nl)
        {
            this.t = t;
            this.tp = tp;
            this.nl = nl;
            result = null;
        }
        
        public SiteLikelihood call()
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
                    for (int i = 0; i < tp.getAllStatesAsList().size(); i ++)
                    {
                        String endState = tp.getAllStatesAsList().get(i);
                        //l keeps track of the total likelihood from each possible
                        //state at the child
                        //Real l = SiteLikelihood.getReal(0.0);//new Real(0.0);
                        //For each possible child state
                        //for (String startState: tp.getAllStates())
                        Real[] nl = nodeLikelihoods.get(b.getChild()).getLikelihoods();
                        //for (int j = 0; j < tp.getAllStates().size(); j ++)
                        Real l = nl[0].multiply(bp.getPosition(i, 0));
                        for (int j = 1; j < nl.length; j++)
                        {
                            //Add the likelihood of going from start state to
                            //end state along that branch in that ratecategory
                            l = l.add(nl[j].multiply(bp.getPosition(i, j)));
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
                //For each possible state
                for (String state: this.tp.getAllStatesAsList())
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
