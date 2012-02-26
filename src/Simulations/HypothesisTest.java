package Simulations;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Constraints.Constrainer;
import Constraints.NoConstraints;
import Exceptions.OutputException;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Optimizers.Optimizer;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Simulations.Simulate.SimulationException;
import Trees.Tree;
import Trees.TreeException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to do hypothesis testing using simulation to generate the null distribution.
 * @author Daniel Money
 * @version 1.1
 */
public class HypothesisTest
{
    /**
     * Constructor for use when neither the null hypothesis and alternative 
     * hypothesis have constraints.  
     * @param nullModel The null model
     * @param altModel The alternative model
     * @param o The optimizer to be used
     * @param reps The number of samples of the null distribution to generate
     */
    public HypothesisTest(Model nullModel, Model altModel, Optimizer o, int reps)
    {
        this(nullModel, altModel, new NoConstraints(nullModel.getStates()),
                new NoConstraints(altModel.getStates()), o, reps);
    }
    
    /**
     * Constructor for use when one or both of the null hypothesis and alternative 
     * hypothesis have constraints.  If only one is constrained then {@link
     * NoConstraints} should be used as input for the hypothesis that is unconstrained.
     * @param nullModel The null model
     * @param altModel The alternative model
     * @param nullConstrainer The constrainer for the null hypothesis
     * @param altConstrainer The constrainer for the alternative hypothesis
     * @param o The optimizer to be used
     * @param reps The number of samples of the null distribution to generate
     */
    public HypothesisTest(Model nullModel, Model altModel, 
            Constrainer nullConstrainer, Constrainer altConstrainer,
            Optimizer o, int reps)
    {
        this.nullModel = nullModel;
        this.altModel = altModel;
        this.nullConstrainer = nullConstrainer;
        this.altConstrainer = altConstrainer;
        this.o = o;
        this.reps = reps;
    }
    
    /**
     * Does a hpyothesis test on the given data and gives a p-value
     * @param t The tree
     * @param a The alignment
     * @param unobserved Any unobserved states
     * @param nullParams The paramters of the null model
     * @param altParams The parameters of the alternative model
     * @return A p-value
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     * @throws OutputException Should not currently be thrown as would only be thrown
     * when an optimizer try to write a checkpoint file and that isn't currently supported
     * in this class.  Included as should be in a future version.
     */
    public double test(Tree t, Alignment a, Alignment unobserved, Parameters nullParams, Parameters altParams)
            throws RateException, ModelException, TreeException, ParameterException,
            OutputException, AlignmentException, SimulationException
    {
        return test(t,a,unobserved,nullParams,altParams,null);
    }
    
    /**
     * Does a hpyothesis test on the given data and gives a p-value
     * @param t The tree
     * @param a The alignment
     * @param unobserved Any unobserved states
     * @param nullParams The paramters of the null model
     * @param altParams The parameters of the alternative model
     * @param recode The recoding to be passed to the simulator.  See 
     * {@link Simulate#getAlignment(int, java.util.Map)} for while this is neccessary.
     * @return A p-value
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     * @throws OutputException Should not currently be thrown as would only be thrown
     * when an optimizer try to write a checkpoint file and that isn't currently supported
     * in this class.  Included as should be in a future version.
     */
    public double test(Tree t, Alignment a, Alignment unobserved, Parameters nullParams, Parameters altParams,
            Map<String,String> recode)
            throws RateException, ModelException, TreeException, ParameterException,
            OutputException, AlignmentException, SimulationException
    {
        //Calculate the difference in likelihood between the two models for the given alignment
        Calculator nullCalc = new Calculator(nullModel, a, t, unobserved, nullConstrainer);
        Calculator altCalc = new Calculator(altModel, a, t, unobserved, altConstrainer);
        Likelihood nullL = o.maximise(nullCalc, nullParams);
        Likelihood altL = o.maximise(altCalc, altParams);        
        double diff = altL.getLikelihood() - nullL.getLikelihood();
        
        //Get the null distribution
        double[] dist = getDistribution(t,a.getLength(),unobserved,nullL.getParameters(),
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
    
    private double[] getDistribution(Tree t, int alignLength, Alignment missing,
            Parameters simParams, Parameters nullParams, Parameters altParams,
            Map<String,String> rec) 
            throws RateException, ModelException, TreeException, ParameterException,
            OutputException, AlignmentException, SimulationException
    {
        //Stores the distribution
        double[] dist = new double[reps];
        
        //Create the simulator
        Simulate s = new Simulate(nullModel, t, simParams, missing);
        
        //For each sample
        for (int i=0; i < reps; i++)
        {
            //Simulate an alignment
            Alignment a = s.getAlignment(alignLength, rec);
            
            //Then calculate and store the likelihood difference between the two models
            Calculator nullCalc = new Calculator(nullModel, a, t, missing, nullConstrainer);
            Calculator altCalc = new Calculator(altModel, a, t, missing, altConstrainer);

            Likelihood nullL = o.maximise(nullCalc, nullParams);
            Likelihood altL = o.maximise(altCalc, altParams);

            dist[i] = altL.getLikelihood() - nullL.getLikelihood();
        }
        
        return dist;
    }
    
    private Model nullModel;
    private Model altModel;
    private Constrainer nullConstrainer;
    private Constrainer altConstrainer;
    private Optimizer o;
    private int reps;
}
