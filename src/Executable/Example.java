package Executable;

import Alignments.Alignment;
import Alignments.SequenceAlignment;
import Ancestors.AncestralJoint;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Models.DNAModelFactory;
import Models.Model;
import Optimizers.GoldenSection;
import Optimizers.Optimizer;
import Parameters.Parameters;
import Simulations.Simulate;
import Trees.Tree;
import java.io.File;

/**
 * An example executable.  Does not try to be a general driver so a better
 * learning example than {@link GeLL}.
 * @author Daniel Money
 * @version 1.0
 */
public class Example
{    
    private Example()
    {
        //No reason for anyone to be creating an instance of this class
    }
    
    /**
     * Main function
     * @param args Command line arguments
     * @throws Exception This is example code so to keep it simple we just
     * throw any exception we encounter.
     */
    public static void main(String[] args) throws Exception
    {
        //Create a tree object.  Here we are going to estimate branch lengths so
        //no point including them in the tree
        Tree t = Tree.fromNewickString("(((Human, Chimpanzee)A, Gorilla)B, Orangutan, Gibbon)C;");
        //Load an alignment
        Alignment a = new SequenceAlignment(new File("src\\Executable\\example\\brown.nuc"));
        //Get the parameters from the tree.  This step is recommended although due
        //to it's wierdness will be done automatically if missed out.
        Parameters p = t.getParametersForEstimation();
        //Get the model, passing the parameters structure to it so the neccessary
        //parameters are added.  See the documentation of Model for how to create
        //your own models
        Model m = DNAModelFactory.GTR_Gamma(p, 4);
        //Create a calculator which is used to calculate likelihoods
        Calculator c = new Calculator(m,a,t);
        //Create an optimizer to do the the optimization
        Optimizer o = new GoldenSection();
        
        //Actually do the optimization and get the result.  The parameters passed
        //will be updated to their estimated values.  The result will contain the
        //lieklihood and intermediate results in calculating it.
        Likelihood l = o.maximise(c, p);
        
        //Update paameters to the optimized values
        p = l.getParameters();
        
        //Print out the likelihood and the estimated parameters
        System.out.println("Likelihood: " + l.getLikelihood());
        System.out.println();
        System.out.println("Parameters:");
        System.out.println(p);
        
        //Create an ancestral reconstruction object
        AncestralJoint aj = AncestralJoint.newInstance(m, a, t);
        
        //And do the reconstruction using the parameters we've just edstimated
        Alignment anc = aj.calculate(p);
        
        //Write out the reconstructed alignment
        SequenceAlignment.writeFile(anc, new File("src\\Executable\\example\\ancestor.dat"));
        
        //Create a simulation object, using the parameters we've just estimated
        Simulate s = new Simulate(m,t,p);
        
        //Create a simulated alignment
        Alignment sim = s.getAlignment(500);
        
        //Write out the simulated alignment
        SequenceAlignment.writeFile(sim, new File("src\\Executable\\example\\simulated.dat"));
    }
}
