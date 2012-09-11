package Likelihood;

import Exceptions.GeneralException;
import Maths.SmallDouble;
import Models.RateCategory;
import Utils.ArrayMap;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Stored the result of a likelihood calculation for a single site
 * @author Daniel Money
 * @version 1.0
 */
public class SiteLikelihood implements Serializable
{
    SiteLikelihood(ArrayMap<RateCategory,RateLikelihood> rateLikelihoods, Probabilities P)
    {
        rateProbability = new HashMap<>();
        l = new SmallDouble(0.0);
        for (RateCategory rc: P.getRateCategory())
        {
            l = l.add(rateLikelihoods.get(rc).getLikelihood().multiply(P.getRateP(rc)));
        }
        SmallDouble maxP = new SmallDouble(0.0);
        maxCat = null;
        for (RateCategory rc: P.getRateCategory())
        {
            SmallDouble rp = rateLikelihoods.get(rc).getLikelihood().multiply(P.getRateP(rc)).divide(l);
            rateProbability.put(rc, rp);
            //if (rp > maxP)
            if (rp.graterThan(maxP))
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
    public SmallDouble getLikelihood()
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
    public SmallDouble getRateCategoryProbability(RateCategory rc)
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
        //return Double.toString(l);
        return l.toString();
    }
    
    /**
     * Sets whether to store the node likelihoods after they've been used.
     * Setting this to false can save significant amounts of memory
     * @param keep Whether to keep the node likelihoods
     */
    public static void keepNodeLikelihoods(boolean keep)
    {
        publicKeepNL = keep;
        keepNL = publicKeepNL && optKeepNL;
    }
    
    /**
     * Similar to {@link #keepNodeLikelihoods(boolean)} but if set to false
     * overrides the setting set by that function.  Intended to only be used
     * by optimizers so that the node likelihoods calculated while optimizing
     * aren't kept.  I.e. should be set to false before optimizing and back to
     * true for the final calculation.
     * @param keep Whether to keep the ndoe likelihoods
     */
    public static void optKeepNL(boolean keep)
    {
        optKeepNL = keep;
        keepNL = publicKeepNL && optKeepNL;
    }

    private ArrayMap<RateCategory,RateLikelihood> rateLikelihoods;
    private SmallDouble l;        
    private Map<RateCategory,SmallDouble> rateProbability;
    private RateCategory maxCat;
    private static final long serialVersionUID = 1;
    
    private static boolean keepNL = true;
    private static boolean publicKeepNL = true;
    private static boolean optKeepNL = true;

    /**
     * Stores the result of the likelihood calculations for a single site <I> and </I>
     * a single rate class.
     * @author Daniel Money
     * @version 1.0
     */
    public static class RateLikelihood implements Serializable
    {
        RateLikelihood(SmallDouble l, ArrayMap<String, NodeLikelihood> nodeLikelihoods)
        {
            this.l = l;
            if (keepNL)
            {
                this.nodeLikelihoods = nodeLikelihoods;
            }
            else
            {
                this.nodeLikelihoods = null;
            }
        }

        /**
         * Returns the likelihood.
         * @return The likelihood
         */
        public SmallDouble getLikelihood()
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
            if (nodeLikelihoods != null)
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
            else
            {
                throw new LikelihoodException("Node results not kept to safe memory");
            }
        }
        
        public String toString()
        {
            //return Double.toString(l);
            return l.toString();
        }

        private SmallDouble l;
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
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).  
         */
        public NodeLikelihood(ArrayMap<String,Integer> states, Set<String> allowedStates) throws LikelihoodException
        {
            likelihoods = new SmallDouble[states.size()];
            this.states = states;
            //for (Entry<String,Integer> s: states.entryList())
            boolean onz = false;
            for (int i = 0; i < states.size(); i ++)
            {
                Entry<String,Integer> s = states.getEntry(i);
                if (allowedStates.contains(s.getKey()))
                {
                    likelihoods[s.getValue()] = new SmallDouble(1.0);
                    onz = true;
                }
                else
                {
                    likelihoods[s.getValue()] = new SmallDouble(0.0);
                }
            }
            if (!onz)
            {
                throw new LikelihoodException("No non-zero probabilities at leaves - alignment state not in model?");
            }
        }
        
        private NodeLikelihood(SmallDouble[] l, ArrayMap<String,Integer> states)
        {
            likelihoods = Arrays.copyOf(l, l.length);
            this.states = states;
        }
        
        public NodeLikelihood clone()
        {
            return new NodeLikelihood(likelihoods, states);
        }
        
        void multiply(String state, SmallDouble by)
        {
            int i = states.get(state);
            likelihoods[i] = likelihoods[i].multiply(by);
        }
        
        /**
         * Returns the partial likelihood for a given state
         * @param state The state to return the partial likelihood for
         * @return The partial likelihood for the given state
         * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
         * has been calculated for the given state 
         */
        public SmallDouble getLikelihood(String state) throws LikelihoodException
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
        public SmallDouble getLikelihood(int i)
        {
            return likelihoods[i];
        }
        
        /**
         * Returns the partial likelihood for each state as an array.
         * Mainly intended to be used internally
         * @return The likelihood for each state.  Position is associated with state
         * based on the map passed to the constructor.
         */
        public SmallDouble[] getLikelihoods()
        {
            return likelihoods;
        }
        
        public String toString()
        {
            return Arrays.toString(likelihoods);
        }
        
        private ArrayMap<String,Integer> states;
        private SmallDouble[] likelihoods;
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
