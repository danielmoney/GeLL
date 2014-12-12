package ModelTest;

import Exceptions.GeneralException;
import Likelihood.Likelihood;
import Optimizers.Optimizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIC
{
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
    
    public Map<TestInstance,Likelihood> getLikelihoods()
    {
        return likelihoods;
    }
    
    public Map<TestInstance,Double> getAICs()
    {
        return aic;
    }
    
    public double getBestAIC()
    {
        return minaic;
    }
    
    public TestInstance getBestInstance()
    {
        return mininstance;
    }
    
    private final Map<TestInstance,Likelihood> likelihoods;
    private final Map<TestInstance,Double> aic;
    private double minaic;
    private TestInstance mininstance;
}
