package ModelTest;

import Exceptions.GeneralException;
import Likelihood.Likelihood;
import Optimizers.Optimizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BIC
{
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
    
    public Map<TestInstance,Likelihood> getLikelihoods()
    {
        return likelihoods;
    }
    
    public Map<TestInstance,Double> getBICs()
    {
        return bic;
    }
    
    public double getBestBIC()
    {
        return minbic;
    }
    
    public TestInstance getBestInstance()
    {
        return mininstance;
    }
    
    private final Map<TestInstance,Likelihood> likelihoods;
    private final Map<TestInstance,Double> bic;
    private double minbic;
    private TestInstance mininstance;
}
