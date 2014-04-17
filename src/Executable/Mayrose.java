package Executable;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Site;
import Exceptions.GeneralException;
import Exceptions.InputException;
import Exceptions.UnexpectedError;
import Likelihood.Likelihood;
import Likelihood.StandardCalculator;
import Likelihood.StandardLikelihood;
import Maths.SquareMatrix;
import Maths.SquareMatrix;
import Models.Model;
import Models.RateCategory;
import Models.RateCategory.RateException;
import Optimizers.GoldenSection;
import Optimizers.Optimizable;
import Optimizers.Optimizer;
import Parameters.Parameter;
import Parameters.Parameters;
import Trees.Tree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
    
/**
 * Another example executable.  This implements the model of Mayrose et al
 * (2010) and is an example of how to define a new model, something not done
 * in {@link Example}.  This does not give the same results as using the Mayrose
 * et al program and this has not been investigated fully as it is intended as
 * an example of how to code a model and not as an exact duplicate.  Part of the
 * difference is that this code does not scale the tree length which the Mayrose
 * code does (for the example data given here it scales it to 6) but this does
 * not explain all of the difference.
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
        //Read in the alignment using a custom function for the custom format
        Alignment a = readAlignment(new File(alignment));
        
        //Read in the tree
        Tree t = Tree.fromFile(new File(tree));
        
        //Calculate the value of C - 10 more than the highest chromosome count
        int C = 0;
        for (Site s: a)
        {
            for (String taxa: s.getTaxa())
            {
                C = Math.max(C, Integer.parseInt(s.getRawCharacter(taxa)) + 10);
            }
        }
        
        //To define a new rate matrix we use a 2D array of Strings.
        String[][] matrix = new String[C][C];
        HashMap<String,Integer> map = new HashMap<>();
        
        //We then closely follow the defintion in the paper.
        for (int i = 0; i < C; i++)
        {
            //We also need to define a map from characters in the alignment to
            //positions in the array, in this case it's simply "1" -> 0, "2" -> 1
            //etc.
            map.put(Integer.toString(i+1), i);
            for (int j = 0; j < C; j++)
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
            m.setRescale(false);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        //Add the parameters from the tree to the parameters structure
        p.addParameters(t.getParameters());
        
        //Create the likelihood calculator
        Optimizable<StandardLikelihood> c = new StandardCalculator(m,a,t);
        
        //Create the optimiser
        Optimizer o = new GoldenSection();
        
        //Calculate an optimal likelihood
        Likelihood l = o.maximise(c, p);
        
        //Print out results
        System.out.println(l);
        System.out.println();
        System.out.print(l.getParameters().toString(false));
    }
    
    private static Alignment readAlignment(File f) throws InputException, AlignmentException
    {
        BufferedReader ain;
	try
	{
	    ain = new BufferedReader(new FileReader(f));
	}
	catch (FileNotFoundException e)
 	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","File does not exist",e);
	}
        try
        {
            LinkedHashMap<String,String> data = new LinkedHashMap<>();
            String name = null;
            String line;
            while ((line = ain.readLine()) != null)
	    {
                if (line.startsWith(">"))
                {
                    name = line.substring(1);
                }
                else
                {
                    data.put(name,line);
                }
            }
            List<Site> a = new ArrayList<>();
            a.add(new Site(data));
            return new Alignment(a);
	}
	catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
    }
    
    private static final String alignment = "src\\Executable\\example\\counts";
    private static final String tree = "src\\Executable\\example\\tree";
}
