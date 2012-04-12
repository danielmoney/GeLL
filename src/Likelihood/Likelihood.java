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
 * @version 1.0
 */

public class Likelihood implements Serializable
{
    //Likelihood(double l, Map<Site,SiteLikelihood> siteLikelihoods,
    //        Map<Site,SiteLikelihood> missingLikelihoods,
    //        Parameters p)
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
        //SiteLikelihood(Map<RateCategory,RateLikelihood> rateLikelihoods, Probabilities P)
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
        //RateLikelihood(double l, Map<String, NodeLikelihood> nodeLikelihoods)
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
     * @author Daniel Money
     * @version 1.0
     */
    public static class NodeLikelihood implements Serializable
    {
        public NodeLikelihood(ArrayMap<String,Integer> states, Set<String> setStates)
        {
            likelihoods = new double[states.size()];
            this.states = states;
            //for (Entry<String,Integer> s: states.entryList())
            for (int i = 0; i < states.size(); i ++)
            {
                Entry<String,Integer> s = states.getEntry(i);
                if (setStates.contains(s.getKey()))
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
        
        public double getLikelihood(int i)
        {
            /*for (int j = 0; j < likelihoods.length; j++)
            {
                if (i == j)
                {
                    return likelihoods[j];
                }
            }
            return Double.NEGATIVE_INFINITY;*/
            return likelihoods[i];
        }
        
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
    /*{
        //This constructor creates the initial sate of a node.
        //Takes the set of all states and the set of setStates. For leaf nodes
        //that is the possible values given by the alignment.  for internal nodes
        //thats the set of states we allow at that node which, in the abscence of
        //any constraints, will be all states.  As per the standard likelihood
        //calculation set states are given a "likelihood" of 1, all other states
        //zero.
        NodeLikelihood(Set<String> states, Set<String> setStates)
        {
            likelihoods = new ToDoubleHashMap<>();
            for (String s: states)
            {
                if (setStates.contains(s))
                {
                    likelihoods.put(s,1.0);
                }
                else
                {
                    likelihoods.put(s,0.0);
                }
            }
        }

        //Multiplies the "likelihood" of a state by the appropiate value.  Used 
        //in the likelihood calculation for internal nodes.  See uses in 
        //Calculator for a little more on this.
        void multiply(String state, double by)
        {
            likelihoods.multiply(state, by);
        }*/

        /**
         * Returns the partial likelihood for a given state
         * @param state The state to return the partial likelihood for
         * @return The partial likelihood for the given state
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
         * has been calculated for the given state 
         */
        /*public Double getLikelihood(String state) throws LikelihoodException
        {
            if (likelihoods.containsKey(state))
            {
                return likelihoods.get(state);
            }
            else
            {
                throw new LikelihoodException("No result for state: " + state);
            }
        }
        
        public String toString()
        {
            return likelihoods.toString();
        }

        private ToDoubleHashMap<String> likelihoods;
        private static final long serialVersionUID = 1;
    }*/
    
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
