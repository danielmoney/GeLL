package Simulations;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Site;
import Constraints.Constrainer;
import Exceptions.OutputException;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Likelihood.Likelihood.LikelihoodException;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Optimizers.Optimizer;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Tree;
import Trees.TreeException;
import java.util.HashMap;
import java.util.Map;

public class HypothesisTest
{
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
    
    public double test(Tree t, Alignment a, Alignment missing, Parameters nullParams, Parameters altParams)
            throws RateException, ModelException, TreeException, ParameterException,
            OutputException, AlignmentException
    {
        Calculator nullCalc = new Calculator(nullModel, a, t, missing, nullConstrainer);
        Calculator altCalc = new Calculator(altModel, a, t, missing, altConstrainer);
        
        Likelihood nullL = o.maximise(nullCalc, nullParams);
        Likelihood altL = o.maximise(altCalc, altParams);
        System.out.println(nullL.getParameters());
        
        double diff = altL.getLikelihood() - nullL.getLikelihood();
        System.out.println(diff);
        System.out.println();
        
        double[] dist = getDistribution(t,a.getLength(),missing,nullL.getParameters(),
                nullParams,altParams);
        
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
            Parameters simParams, Parameters nullParams, Parameters altParams) 
            throws RateException, ModelException, TreeException, ParameterException,
            OutputException, AlignmentException
    {
        double[] dist = new double[reps];
        Simulate s = new Simulate(nullModel, t, simParams, missing);
        
        Map<String,String> rec = new HashMap<>();
        rec.put("A","-");rec.put("B","-");
        
        double tot = 0.0;
        
        //alignLength = alignLength * 5;
        
        for (int i=0; i < reps; i++)
        {
            Alignment a = s.getAlignment(alignLength, rec);
            //a = a.recode(rec);
            
            Calculator nullCalc = new Calculator(nullModel, a, t, missing, nullConstrainer);
            Calculator altCalc = new Calculator(altModel, a, t, missing, altConstrainer);

            Likelihood nullL = o.maximise(nullCalc, nullParams);
            Likelihood altL = o.maximise(altCalc, altParams);

            dist[i] = altL.getLikelihood() - nullL.getLikelihood();
            System.out.println(dist[i]);
            tot += dist[i];
        }
        
        System.out.println(tot/reps);
        
        return dist;
    }
    
    private Model nullModel;
    private Model altModel;
    private Constrainer nullConstrainer;
    private Constrainer altConstrainer;
    private Optimizer o;
    private int reps;
}
