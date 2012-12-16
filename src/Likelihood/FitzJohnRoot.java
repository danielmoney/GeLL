package Likelihood;

import Exceptions.UnexpectedError;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Maths.Real;
import Models.RateCategory.RateException;
import java.util.Set;

/**
 * Root class for the FitzJohn et al 2009 method
 * @author Daniel Money
 * @version 2.0
 */
public class FitzJohnRoot implements Root
{
    /**
     * Default constructor
     * @param states The possible states of the model
     */
    public FitzJohnRoot(Set<String> states)
    {
        this.states = states;
    }
    
    public Real calculate(NodeLikelihood root)
    {
        Real top = null;
        Real bottom = null;
        
        for (String s: states)
        {
            try
            {
                if (top == null)
                {
                    top = root.getLikelihood(s).multiply(root.getLikelihood(s));
                    bottom = root.getLikelihood(s);
                }
                else
                {
                    top = top.add(root.getLikelihood(s).multiply(root.getLikelihood(s)));
                    bottom = bottom.add(root.getLikelihood(s));
                }
            }
            catch (LikelihoodException ex)
            {
                //Shouldn't reach here as we know what oinformation should
                // have been claculated and only ask for that
                throw new UnexpectedError(ex);
            }
        }
        
        return top.divide(bottom);
    }
    
    public double getFreq(String state) throws RateException
    {
        throw new RateException("Frequency not independent of likelihood");
    }
    
    private Set<String> states;
}
