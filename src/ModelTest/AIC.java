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

package ModelTest;

import Exceptions.GeneralException;
import Likelihood.Likelihood;
import Optimizers.Optimizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for computing the AIC of several test instances (where an instance
 * is a calculator and a set of parameters.
 * @author Daniel Money
 * @version 2.0
 */
public class AIC
{

    /**
     * Creates an object containing the AIC for several test instances
     * @param instances The test instances to calculate the AIC for
     * @param o The optimizer to used when maximizing the likelihood
     * @throws GeneralException If there is an error
     */
    public AIC(List<TestInstance> instances, Optimizer o) throws GeneralException
    {
        likelihoods = new HashMap<>();
        aic = new HashMap<>();
        
        minaic = Double.MAX_VALUE;
        mininstance = null;
        for (TestInstance instance: instances)
        {
            Likelihood l = o.maximise(instance.getCalculator(), 
                    instance.getParameters());
            likelihoods.put(instance, l);
            
            double al = l.getLikelihood() + instance.getAdapterL();
            double ap = instance.getParameters().numberEstimate() +
                    instance.getNumberAdapterParams();
            double a = 2*(ap - al);
            aic.put(instance, a);
            
            if (a < minaic)
            {
                minaic = a;
                mininstance = instance;
            }
        }        
    }
    
    /**
     * Returns the likelihood for each instance
     * @return Map from instance to likelihood
     */
    public Map<TestInstance,Likelihood> getLikelihoods()
    {
        return likelihoods;
    }

    /**
     * Returns the AIC for each instance
     * @return Map from instance to AIC
     */
    public Map<TestInstance,Double> getAICs()
    {
        return aic;
    }
    
    /**
     * Returns the best AIC
     * @return The best AIC
     */
    public double getBestAIC()
    {
        return minaic;
    }
    
    /**
     * Returns the test instance with the best AIC
     * @return The test instance with the best AIC
     */
    public TestInstance getBestInstance()
    {
        return mininstance;
    }
    
    private final Map<TestInstance,Likelihood> likelihoods;
    private final Map<TestInstance,Double> aic;
    private double minaic;
    private TestInstance mininstance;
}
