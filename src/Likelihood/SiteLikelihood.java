package Likelihood;

import Exceptions.GeneralException;
import Maths.Real;
import Maths.RealFactory;
import Maths.RealFactory.RealType;
import Maths.SmallDouble;
import Maths.StandardDouble;
import Models.RateCategory;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Stored the result of a likelihood calculation for a single site
 * @author Daniel Money
 * @version 2.0
 */
public class SiteLikelihood implements Serializable
{
    /**
     * Default constructor
     * @param rateLikelihoods The likelihood of the individual rates at that site
     * @param P A Probabilities object from which the frequency of each rate will be fetched
     */
    public SiteLikelihood(Map<RateCategory,RateLikelihood> rateLikelihoods, Probabilities P)
    {
        rateProbability = new HashMap<>();
        l = RealFactory.getReal(type,0.0);
        for (RateCategory rc: P.getRateCategory())
        {
            l = l.add(rateLikelihoods.get(rc).getLikelihood().multiply(P.getRateP(rc)));
        }
        Real maxP = RealFactory.getReal(type,0.0);
        maxCat = null;
        for (RateCategory rc: P.getRateCategory())
        {
            Real rp = rateLikelihoods.get(rc).getLikelihood().multiply(P.getRateP(rc)).divide(l);
            rateProbability.put(rc, rp);
            if (rp.greaterThan(maxP))
            {
                maxP = rp;
                maxCat = rc;
            }
        }
        this.rateLikelihoods = rateLikelihoods;
    }

    /**
     * Get the likelihood
     * @return The likelihood
     */
    public Real getLikelihood()
    {
        return l;
    }

    /**
     * Get the likelihood for a single rate class
     * @param rate Rate class to get the likelihood for
     * @return The likelihood results for a given rate
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if no likelihood
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
    public Real getRateCategoryProbability(RateCategory rc)
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
        keepNL = true;
    }
    
    /**
     * Sets the real type to be used during calculations, either {@link SmallDouble}
     * or {@link StandardDouble}.
     * @param type The double type to use
     * @see Ancestors.AncestralJointDP#realType(Maths.RealFactory.RealType) 
     */
    public static void realType(RealType type)
    {
        SiteLikelihood.type = type;
    }

    private Map<RateCategory,RateLikelihood> rateLikelihoods;
    private Real l;        
    private Map<RateCategory,Real> rateProbability;
    private RateCategory maxCat;
    private static final long serialVersionUID = 1;
    
    private static boolean keepNL = true;
    private static boolean publicKeepNL = true;
    private static boolean optKeepNL = true;
        
    private static RealType type = RealType.STANDARD_DOUBLE;

    /**
     * Stores the result of the likelihood calculations for a single site <I> and </I>
     * a single rate class.
     * @author Daniel Money
     * @version 2.0
     */
    public static class RateLikelihood implements Serializable
    {
        /**
         * Default constructor
         * @param l The likelihood for this site and rate
         * @param nodeLikelihoods The invidual node likelihoods for this site and rate
         */
        public RateLikelihood(Real l, Map<String, NodeLikelihood> nodeLikelihoods)
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
        public Real getLikelihood()
        {
            return l;
        }

        /**
         * Returns the likelihood results for a given node on the tree
         * @param node The node to return the result for
         * @return The likelihood result for the given node
         * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if no likelihood
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
            return l.toString();
        }

        private Real l;
        private Map<String, NodeLikelihood> nodeLikelihoods;
        private static final long serialVersionUID = 1;
    }

    /**
     * Stores the results of a likelihood claculation for a single node in a tree.
     * That is the partial likelihoods for each possible state.
     * @author Daniel Money
     * @version 2.0
     */
    public static class NodeLikelihood implements Serializable
    {
        /**
         * Constructor where all sates are allowed at a node
         * @param states Map from a state to it's position in the array
         * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
         *      (most probably due to the state at the node not being in the model).  
         */
        public NodeLikelihood(Map<String,Integer> states) throws LikelihoodException
        {
            likelihoods = new Real[states.size()];
            this.states = states;
            for (int i = 0; i < states.size(); i ++)
            {
                likelihoods[i] = RealFactory.getReal(type,1.0);
            }            
        }
        
        /**
         * Constructor where only some states are allowed at a node
         * @param states Map from a state to it's position in the array
         * @param allowedStates The allowed states at this state
         * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
         *      (most probably due to the state at the node not being in the model).  
         */
        public NodeLikelihood(Map<String,Integer> states, Set<String> allowedStates) throws LikelihoodException
        {
            likelihoods = new Real[states.size()];
            this.states = states;
            boolean onz = false;
            for (Entry<String,Integer> s: states.entrySet())
            {
                if (allowedStates.contains(s.getKey()))
                {
                    likelihoods[s.getValue()] = RealFactory.getReal(type,1.0);
                    onz = true;
                }
                else
                {
                    likelihoods[s.getValue()] = RealFactory.getReal(type,0.0);
                }
            }
            //If an empty set is passed in assume it's deliberate and don't throw an error
            if (!allowedStates.isEmpty() && !onz)
            {
                throw new LikelihoodException("No non-zero probabilities at leaves - alignment state not in model?");
            }
        }

        /**
         * Constructor where only a single state is allowed at a node
         * @param states Map from a state to it's position in the array
         * @param allowedState The allowed state at this state
         * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
         *      (most probably due to the state at the node not being in the model).  
         */
        public NodeLikelihood(Map<String,Integer> states, String allowedState) throws LikelihoodException
        {
            likelihoods = new Real[states.size()];
            this.states = states;
            boolean onz = false;
            for (Entry<String,Integer> s: states.entrySet())
            {
                if ((allowedState != null) && allowedState.equals(s.getKey()))
                {
                    likelihoods[s.getValue()] = RealFactory.getReal(type,1.0);
                    onz = true;
                }
                else
                {
                    likelihoods[s.getValue()] = RealFactory.getReal(type,0.0);
                }
            }
            if (!onz)
            {
                throw new LikelihoodException("No non-zero probabilities at leaves - alignment state not in model?");
            }
        }
        
        private NodeLikelihood(Real[] l, Map<String,Integer> states)
        {
            likelihoods = Arrays.copyOf(l, l.length);
            this.states = states;
        }
        
        public NodeLikelihood clone()
        {
            return new NodeLikelihood(likelihoods, states);
        }
        
        /**
         * This is a fudge to stop to allow this code to be reused in some
         * of the ancestor classes.  Should be no reason to use.
         * @param state The state probility to multiply
         * @param by How much to mutliply the probabilty by
         */
        public void multiply(String state, Real by)
        {
            int i = states.get(state);
            likelihoods[i] = likelihoods[i].multiply(by);
        }
        
        /**
         * This is a fudge to stop to allow this code to be reused in some
         * of the ancestor classes.  Should be no reason to use.
         * @param state The state probility to multiply
         * @param by How much to mutliply the probabilty by
         */
        public void multiply(String state, double by)
        {
            int i = states.get(state);
            likelihoods[i] = likelihoods[i].multiply(by);
        }
        
        /**
         * Returns the partial likelihood for a given state
         * @param state The state to return the partial likelihood for
         * @return The partial likelihood for the given state
         * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if no likelihood
         * has been calculated for the given state 
         */
        public Real getLikelihood(String state) throws LikelihoodException
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
        public Real getLikelihood(int i)
        {
            return likelihoods[i];
        }
        
        /**
         * Returns the partial likelihood for each state as an array.
         * Mainly intended to be used internally
         * @return The likelihood for each state.  Position is associated with state
         * based on the map passed to the constructor.
         */
        public Real[] getLikelihoods()
        {
            return likelihoods;
        }
        
        public String toString()
        {
            return Arrays.toString(likelihoods);
        }
        
        private Map<String,Integer> states;
        private Real[] likelihoods;
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
