package Likelihood;

import Likelihood.SiteLikelihood.NodeLikelihood;
import Maths.Real;
import Models.RateCategory.RateException;

/**
 * Provides information on what to do once the root of the tree is reached.
 * Can be used to calculate the total likelihood from the root node likelihoods 
 * or provide the frequencies of the various states at the root
 * @author Daniel Money
 * @version 2.0
 */
public interface Root
{
    /**
     * Calculate the total likelihood from the root node likelihood
     * @param root The root node likelihood
     * @return The total likelihood
     */
    public Real calculate(NodeLikelihood root);
    /**
     * Get the frequency of the given state at the root
     * @param state The state to get the frequency of
     * @return The frequency
     * @throws Models.RateCategory.RateException If the frequency is not independent
     * of the root likelihoods (as in the case of FitzJohn et al 2009).
     */
    public double getFreq(String state) throws RateException;
}
