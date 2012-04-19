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
import Models.RateCategory;
import Parameters.Parameters;
import Utils.ArrayMap;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Stores the results of a likelihood calculation.  As well as the overall
 * likelihood it stores the likelihood of each site and also of each missing
 * site.
 * @author Daniel Money
 * @version 1.2
 */

public class Likelihood implements Serializable
{
    Likelihood(double l, ArrayMap<Site,SiteLikelihood> siteLikelihoods,
            ArrayMap<Site,SiteLikelihood> missingLikelihoods,
            Parameters p)
    {
        this.l = l;
        this.siteLikelihoods = siteLikelihoods;
        this.missingLikelihoods = missingLikelihoods;
        this.p = p;
    }
    
    /**
     * Gets the total likelihood
     * @return The total likelihood
     */
    public double getLikelihood()
    {
        return l;
    }
    
    /**
     * Gets the likelihood result for a given site
     * @param s The site to return the likelihood results for
     * @return The likelihood results for the given site
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
     * has been calculated for the given site
     */
    public SiteLikelihood getSiteLikelihood(Site s) throws LikelihoodException
    {
        if (siteLikelihoods.containsKey(s))
        {
            return siteLikelihoods.get(s);
        }
        else
        {
            throw new LikelihoodException("No result for site: " + s);
        }
    }
    
    /**
     * Gets the likelihood result for a given missing site
     * @param s The missing site to return the likelihood results for
     * @return The likelihood results for the given missing site
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
     * has been calculated for the given site
     */
    public SiteLikelihood getMissingLikelihood(Site s) throws LikelihoodException
    {
        if (missingLikelihoods.containsKey(s))
        {
            return missingLikelihoods.get(s);
        }
        else
        {
            throw new LikelihoodException("No result for site: " + s);
        }
    }
    
    /**
     * Gets the parameters used to calculate this likelihood
     * @return The parameters
     */
    public Parameters getParameters()
    {
        return p;
    }
    
    public String toString()
    {
        return Double.toString(l);
    }
    
    private double l;
    private ArrayMap<Site,SiteLikelihood> siteLikelihoods;
    private ArrayMap<Site,SiteLikelihood> missingLikelihoods;
    private Parameters p;
    
    private static final long serialVersionUID = 1;
    
    /**
     * Stored the result of a likelihood calculation for a single site
     * @author Daniel Money
     * @version 1.0
     */
    public static class SiteLikelihood implements Serializable
    {
        SiteLikelihood(ArrayMap<RateCategory,RateLikelihood> rateLikelihoods, Probabilities P)
        {
            rateProbability = new HashMap<>();
            l = 0.0;
            for (RateCategory rc: P.getRateCategory())
            {
                l += rateLikelihoods.get(rc).getLikelihood() * P.getRateP(rc);
            }
            double maxP = 0.0;
            maxCat = null;
            for (RateCategory rc: P.getRateCategory())
            {
                double rp = rateLikelihoods.get(rc).getLikelihood() * P.getRateP(rc) / l;
                rateProbability.put(rc, rp);
                if (rp > maxP)
                {
                    maxP = rp;
                    maxCat = rc;
                }
            }
            this.rateLikelihoods = rateLikelihoods;
            //System.out.println(l);
        }

        /**
         * Get the likelihood
         * @return The likelihood
         */
        public double getLikelihood()
        {
            return l;
        }

        /**
         * Get the likelihood for a single rate class
         * @param rate Rate class to get the likelihood for
         * @return The likelihood results for a given rate
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
         * has been calculated for the given rate
         */
        public RateLikelihood getRateLikelihood(RateCategory rate) throws LikelihoodException
        {
            if (rateLikelihoods.containsKey(rate))
            {
                return rateLikelihoods.get(rate);
            }
            else
            {
                throw new LikelihoodException("No result for rate: " + rate);
            }
        }
        
        /**
         * Gets the probability of being in a rate category
         * @param rc The rate category
         * @return The probability of being in that rate category
         */
        public double getRateCategoryProbability(RateCategory rc)
        {
            return rateProbability.get(rc);
        }
        
        /**
         * Gets the most probable rate category
         * @return The most probable rate category
         */
        public RateCategory getMostProbableRateCategory()
        {
            return maxCat;
        }
        
        public String toString()
        {
            return Double.toString(l);
        }

        private ArrayMap<RateCategory,RateLikelihood> rateLikelihoods;
        private double l;        
        private Map<RateCategory,Double> rateProbability;
        private RateCategory maxCat;
        private static final long serialVersionUID = 1;
    }

    /**
     * Stores the result of the likelihood calculations for a single site <I> and </I>
     * a single rate class.
     * @author Daniel Money
     * @version 1.0
     */
    public static class RateLikelihood implements Serializable
    {
       RateLikelihood(double l, ArrayMap<String, NodeLikelihood> nodeLikelihoods)
        {
            this.l = l;
            this.nodeLikelihoods = nodeLikelihoods;
        }

        /**
         * Returns the likelihood.
         * @return The likelihood
         */
        public double getLikelihood()
        {
            return l;
        }

        /**
         * Returns the likelihood results for a given node on the tree
         * @param node The node to return the result for
         * @return The likelihood result for the given node
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
         * has been calculated for the given node 
         */
        public NodeLikelihood getNodeLikelihood(String node) throws LikelihoodException
        {
            if (nodeLikelihoods.containsKey(node))
            {
                return nodeLikelihoods.get(node);
            }
            else
            {
                throw new LikelihoodException("No result for node: " + node);
            }
        }
        
        public String toString()
        {
            return Double.toString(l);
        }

        private double l;
        //private Map<String, NodeLikelihood> nodeLikelihoods;
        private ArrayMap<String, NodeLikelihood> nodeLikelihoods;
        private static final long serialVersionUID = 1;
    }

    /**
     * Stores the results of a likelihood claculation for a single node in a tree.
     * That is the partial likelihoods for each possible state.
     * This is one of only two classes where backwards compitability with 1.1 is not possible.
     * The previous constructor only contained information on the states and not
     * what position they mapped too.  The new, more efficient data structures need
     * to knoiw the mapping so there's no way the old constructor is usable.
     * @author Daniel Money
     * @version 1.2
     */
    public static class NodeLikelihood implements Serializable
    {
        /**
         * Default constructor
         * @param states Map from a state to it's position in the array
         * @param allowedStates The allowed states at this state
         */
        public NodeLikelihood(ArrayMap<String,Integer> states, Set<String> allowedStates)
        {
            likelihoods = new double[states.size()];
            this.states = states;
            //for (Entry<String,Integer> s: states.entryList())
            for (int i = 0; i < states.size(); i ++)
            {
                Entry<String,Integer> s = states.getEntry(i);
                if (allowedStates.contains(s.getKey()))
                {
                    likelihoods[s.getValue()] = 1.0;
                }
                else
                {
                    likelihoods[s.getValue()] = 0.0;
                }
            }
        }
        
        private NodeLikelihood(double[] l, ArrayMap<String,Integer> states)
        {
            likelihoods = Arrays.copyOf(l, l.length);
            this.states = states;
        }
        
        public NodeLikelihood clone()
        {
            return new NodeLikelihood(likelihoods, states);
        }
        
        void multiply(String state, double by)
        {
            int i = states.get(state);
            likelihoods[i] = likelihoods[i] * by;
        }
        
        /**
         * Returns the partial likelihood for a given state
         * @param state The state to return the partial likelihood for
         * @return The partial likelihood for the given state
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
         * has been calculated for the given state 
         */
        public double getLikelihood(String state) throws LikelihoodException
        {
            if (states.containsKey(state))
            {
                return likelihoods[states.get(state)];
            }
            else
            {
                throw new LikelihoodException("No result for state: " + state);
            }
        }
        
        /**
         * Returns the partial likelihood for the state at position i.
         * Mainly intended to be used internally
         * @param i The position
         * @return The likelihood for the state associated with that position
         * (as defined by the map passed to the constructor)
         */
        public double getLikelihood(int i)
        {
            return likelihoods[i];
        }
        
        /**
         * Returns the partial likelihood for each state as an array.
         * Mainly intended to be used internally
         * @return The likelihood for each state.  Position is associated with state
         * based on the map passed to the constructor.
         */
        public double[] getLikelihoods()
        {
            return likelihoods;
        }
        
        public String toString()
        {
            return Arrays.toString(likelihoods);
        }
        
        private ArrayMap<String,Integer> states;
        private double[] likelihoods;
    }
    
    /**
     * Exception related to a likelihood calculation
     */
    public static class LikelihoodException extends GeneralException
    {
        /**
         * Constructor
         * @param msg Cause of the problem
         */
        public LikelihoodException(String msg)
        {
            super(msg,null);
        }
    }
}
