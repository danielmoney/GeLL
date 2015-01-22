package ModelTest;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Exceptions.GeneralException;
import Likelihood.StandardCalculator;
import Likelihood.StandardLikelihood;
import Models.Model;
import Optimizers.Optimizer;
import Parameters.Parameters;
import Simulations.Simulate;
import Trees.Tree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to do hypothesis testing using simulation to generate the null distribution.
 * @author Daniel Money
 * @version 2.0
 */
public class HypothesisTest
{
    /**
     * Constructor
     * @param nullModel The null model
     * @param altModel The alternative model
     * @param o The optimiser to be used
     * @param reps The number of samples of the null distribution to generate
     */
    public HypothesisTest(Model nullModel, Model altModel, 
            Optimizer o, int reps)
    {
        this.nullModel = new HashMap<>();
        this.nullModel.put(null,nullModel);
        this.altModel = new HashMap<>(); 
        this.altModel.put(null,altModel);
        this.o = o;
        this.reps = reps;
    }
    
    public HypothesisTest(Map<String,Model> nullModel, Map<String,Model> altModel, 
            Optimizer o, int reps)
    {
        this.nullModel = nullModel;
        this.altModel = altModel;
        this.o = o;
        this.reps = reps;
    }

    public double test(Tree t, Alignment a, Alignment unobserved, Parameters nullParams, Parameters altParams)
            throws GeneralException
    {
        return test(t,a,unobserved,nullParams,altParams,null);
    }

    
    public double test(Tree t, Alignment a, Alignment unobserved, Parameters nullParams, Parameters altParams,
            Map<String,String> recode)
            throws GeneralException
    {
        Map<String,Tree> tm = new HashMap<>();
        for (String s: nullModel.keySet())
        {
            tm.put(s,t);
        }
        return test(tm,a,unobserved,nullParams,altParams,null);
    }
    
    /**
     * Does a hypothesis test on the given data and gives a p-value
     * @param t The tree
     * @param a The alignment
     * @param unobserved Any unobserved states
     * @param nullParams The parameters of the null model
     * @param altParams The parameters of the alternative model
     * @return A p-value
     * @throws GeneralException When there is a problem performing the hypothesis test
     */
    public double test(Map<String,Tree> t, Alignment a, Alignment unobserved, Parameters nullParams, Parameters altParams)
            throws GeneralException
    {
        return test(t,a,unobserved,nullParams,altParams,null);
    }
    
    /**
     * Does a hypothesis test on the given data and gives a p-value
     * @param a The alignment
     * @param unobserved Any unobserved states
     * @param nullParams The parameters of the null model
     * @param altParams The parameters of the alternative model
     * @param recode The recoding to be passed to the simulator.  See 
     * {@link Simulate#getAlignment(int, java.util.Map)} for while this is necessary.
     * @return A p-value
     * @throws GeneralException When there is a problem performing the hypothesis test
     */
    public double test(Map<String,Tree> t, Alignment a, Alignment unobserved, Parameters nullParams, Parameters altParams,
            Map<String,String> recode)
            throws GeneralException
    {
        for (String c: t.keySet())
        {
            if (!altModel.containsKey(c))
            {
                throw new AlignmentException("Alignment contains class for which no tree is provided");
            }    
        }
        //Calculate the difference in likelihood between the two models for the given alignment
        StandardCalculator nullCalc = new StandardCalculator(nullModel, a, t, unobserved);
        StandardCalculator altCalc = new StandardCalculator(altModel, a, t, unobserved);
        StandardLikelihood nullL = o.maximise(nullCalc, nullParams);
        StandardLikelihood altL = o.maximise(altCalc, altParams);        
        double diff = altL.getLikelihood() - nullL.getLikelihood();
        
        //Get the null distribution
        double[] dist = getDistribution(t,a.getSiteClasses(),unobserved,nullL.getParameters(),
                nullParams,altParams,recode);
        
        //Calculate and return the percentage point for our data
        int c = 0;
        for (double d: dist)
        {
            if (diff > d)
            {
                c++;
            }
        }
        
        return (double) c / (double) reps;
    }
    
    private double[] getDistribution(Map<String,Tree> t, List<String> siteClasses, Alignment missing,
            Parameters simParams, Parameters nullParams, Parameters altParams,
            Map<String,String> rec) 
            throws GeneralException
    {
        //Stores the distribution
        double[] dist = new double[reps];
        
        //Create the simulator
        Simulate s = new Simulate(nullModel, t, simParams, missing);
        
        //For each sample
        for (int i=0; i < reps; i++)
        {
            //Simulate an alignment
            Alignment a = s.getAlignment(siteClasses, rec);
            
            //Then calculate and store the likelihood difference between the two models
            StandardCalculator nullCalc = new StandardCalculator(nullModel, a, t, missing);
            StandardCalculator altCalc = new StandardCalculator(altModel, a, t, missing);

            StandardLikelihood nullL = o.maximise(nullCalc, nullParams);
            StandardLikelihood altL = o.maximise(altCalc, altParams);

            dist[i] = altL.getLikelihood() - nullL.getLikelihood();
        }
        
        return dist;
    }
    
    private Map<String,Model> nullModel;
    private Map<String,Model> altModel;
    private Optimizer o;
    private int reps;
    //private boolean singleClass;
}
