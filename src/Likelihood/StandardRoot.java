package Likelihood;

import Exceptions.UnexpectedError;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Maths.Real;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Root class for the standard root methods - e.g. root frequencies based on the
 * stationary distribution or given by the model.  Also used if the root distribution
 * is given by the quasi-stationary distribution as this uses the same machinery.
 * @author Daniel Money
 * @version 2.0
 */
public class StandardRoot implements Root
{
    /**
     * Default constructor
     * @param freq The frequency of the various states
     * @param map Map from state to position in the frequency array
     */
    public StandardRoot(double[] freq, Map<String,Integer> map)
    {
        this.freq = freq;
        this.map = map;
    }
    
    public Real calculate(NodeLikelihood root)
    {
        Real total = null;
        for (Entry<String,Integer> e: map.entrySet())
        {
            try
            {
                //Get the likelihood at the root, multiply by it's root frequency
                //and add to the ratde total.
                if (total == null)
                {
                    total = root.getLikelihood(e.getKey()).multiply(freq[e.getValue()]);
                }
                else
                {
                    total = total.add(root.getLikelihood(e.getKey()).multiply(freq[e.getValue()]));
                }
            }
            catch (LikelihoodException ex)
            {
                //Shouldn't reach here as we know what oinformation should
                // have been claculated and only ask for that
                throw new UnexpectedError(ex);
            }
        }
        return total;
    }
    
    public double getFreq(String state)
    {
        return freq[map.get(state)];
    }
    
    double[] freq;
    Map<String,Integer> map;
}
