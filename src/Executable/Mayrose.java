package Executable;

import Alignments.Alignment;
import Alignments.DuplicationAlignment;
import Exceptions.GeneralException;
import Exceptions.UnexpectedError;
import Likelihood.Likelihood;
import Likelihood.StandardCalculator;
import Likelihood.StandardLikelihood;
import Models.Model;
import Models.RateCategory;
import Models.RateCategory.RateException;
import Optimizers.GoldenSection;
import Optimizers.Optimizable;
import Optimizers.Optimizer;
import Parameters.Parameter;
import Parameters.Parameters;
import Trees.Tree;
import java.io.File;
import java.util.HashMap;
    
/**
 * Another example executable.  This implements the model of Mayrose et al
 * (2010) and is an example of how to define a new model, something not done
 * in {@link Example}.
 * @author Daniel Money
 * @version 2.0
 */
public class Mayrose
{
    private Mayrose()
    {
        //No reason for anyone to be creating an instance of this class
    }
    
    /**
     * Main function
     * @param args Command line arguments
     * @throws GeneralException This is example code so to keep it simple we just
     * throw any exception we encounter.
     */
    public static void main(String[] args) throws GeneralException
    {
        //To define a new rate matrix we use a 2D array of Strings.
        String[][] matrix = new String[C+1][C+1];
        HashMap<String,Integer> map = new HashMap<>();
        
        //We then closely follow the defintion in the paper.
        for (int i = 0; i <= C; i++)
        {
            //We also need to define a map from characters in the alignment to
            //positions in the array, in this case it's simply "0" -> 0, "1" -> 1
            //etc.
            map.put(Integer.toString(i), i);
            for (int j = 0; i <=C; j++)
            {
                //By default set the rate to zero.
                matrix[i][j] = "0";

                //If we have a gain define the rate to be l (lambda)
                if (j == i + 1)
                {
                    matrix[i][j] = "l";
                }
                //If we have a loss define the rate to be d (delta)
                if (j == i - 1)
                {
                    matrix[i][j] = "d";
                }
                //If we have a polyploidization define the rate to be r (rho)
                if (j == 2 * i)
                {
                    matrix[i][j] = "r";
                }
                
                //We don't need to define the diagonals as any values in the
                //diagonals are ignored and automatically calculated to make
                //the matrix a valid rate matrix.
            }
        }
        
        //Start the definition of the parameters.  Add the three parameters in
        //the model as parameters that must be positive.
        Parameters p = new Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("l"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("d"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("r"));
        
        //Create the model using the FitzJohn method for root distribution
        Model m = null;
        try
        {
            m = new Model(new RateCategory(matrix,RateCategory.FrequencyType.FITZJOHN,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        Alignment a = DuplicationAlignment.fromFile(new File(alignment));
        
        Tree t = Tree.fromFile(new File(tree));
        p.addParameters(t.getParameters());
        
        Optimizable<StandardLikelihood> c = new StandardCalculator(m,a,t);
        
        Optimizer o = new GoldenSection();
        
        Likelihood l = o.maximise(c, p);
        
        System.out.println(l);
        System.out.println();
        System.err.println(p.toString(false));
    }
    
    private static final int C = 25;
    private static final String alignment = "";
    private static final String tree = "";
}
