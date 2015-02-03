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
 * Class for computing the BIC of several test instances (where an instance
 * is a calculator and a set of parameters.
 * @author Daniel Money
 * @version 2.0
 */
public class BIC
{
    
    /**
     * Creates an object containing the BIC for several test instances
     * @param instances The test instances to calculate the AIC for
     * @param o The optimizer to used when maximizing the likelihood
     * @throws GeneralException If there is an error
     */
    public BIC(List<TestInstance> instances, Optimizer o) throws GeneralException
    {
        likelihoods = new HashMap<>();
        bic = new HashMap<>();
        
        minbic = Double.MAX_VALUE;
        mininstance = null;
        for (TestInstance instance: instances)
        {
            Likelihood l = o.maximise(instance.getCalculator(), 
                    instance.getParameters());
            likelihoods.put(instance, l);
            
            double al = l.getLikelihood() + instance.getAdapterL();
            double ap = instance.getParameters().numberEstimate() +
                    instance.getNumberAdapterParams();
            int n = instance.getCalculator().getAlignmentLength();
            double a = -2 * al + ap * Math.log(n);
            bic.put(instance, a);
            
            if (a < minbic)
            {
                minbic = a;
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
     * Returns the BIC for each instance
     * @return Map from instance to AIC
     */
    public Map<TestInstance,Double> getBICs()
    {
        return bic;
    }

    /**
     * Returns the best BIC
     * @return The best BIC
     */
    public double getBestBIC()
    {
        return minbic;
    }
    
    /**
     * Returns the test instance with the best BIC
     * @return The test instance with the best BIC
     */
    public TestInstance getBestInstance()
    {
        return mininstance;
    }
    
    private final Map<TestInstance,Likelihood> likelihoods;
    private final Map<TestInstance,Double> bic;
    private double minbic;
    private TestInstance mininstance;
}
