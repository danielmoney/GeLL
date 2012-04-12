package Executable;

import Optimizers.*;
import Alignments.Alignment;
import Alignments.SequenceAlignment;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Maths.SquareMatrix;
import Maths.SquareMatrix.Calculation;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameter;
import Parameters.Parameters;
import Trees.Tree;
import java.io.File;
import java.util.HashMap;

/**
 * Tests the GoldenSection optimizer is working right
 * @author Daniel Money
 * @version 1.0
 */
public class Grrr
{

    /**
     * Tests by comaping the optimised likelihood to PAML
     * @throws Exception Thrown if something went wrong!
     */
    public static void main(String[] args)
    {
        try
        {
            //SquareMatrix.setExpMethod(Calculation.EIGEN);
            Tree t = Tree.fromNewickString("(((Human, Chimpanzee)A, Gorilla)B, Orangutan, Gibbon)C;");
            Alignment a = new SequenceAlignment(new File("test\\PAML\\Likelihood\\brown.nuc"));

            String[][] ma = new String[4][4];

            ma[0][0] = "-"; ma[0][1] = "a*pC"; ma[0][2] = "b*pA"; ma[0][3] = "c*pG";
            ma[1][0] = "a*pT"; ma[1][1] = "-"; ma[1][2] = "d*pA"; ma[1][3] = "e*pG";
            ma[2][0] = "b*pT"; ma[2][1] = "d*pC"; ma[2][2] = "-"; ma[2][3] = "f*pG";
            ma[3][0] = "c*pT"; ma[3][1] = "e*pC"; ma[3][2] = "f*pA"; ma[3][3] = "-";

            String[] freq = {"pT", "pC", "pA", "pG"};

            HashMap<String,Integer> map = new HashMap<>();
            map.put("T",0);
            map.put("C",1);
            map.put("A",2);
            map.put("G",3);

            Model m = Model.gammaRates(new RateCategory(ma,freq,map),"g",4);

            Calculator c = new Calculator(m,a,t);

            Parameters p = t.getParametersForEstimation();

            p.addParameter(Parameter.newEstimatedPositiveParameter("a"));
            p.addParameter(Parameter.newEstimatedPositiveParameter("b"));
            p.addParameter(Parameter.newEstimatedPositiveParameter("c"));
            p.addParameter(Parameter.newEstimatedPositiveParameter("d"));
            p.addParameter(Parameter.newEstimatedPositiveParameter("e"));
            p.addParameter(Parameter.newFixedParameter("f",1.0));

            p.addParameter(Parameter.newFixedParameter("pT",1.0));
            p.addParameter(Parameter.newEstimatedPositiveParameter("pC"));
            p.addParameter(Parameter.newEstimatedPositiveParameter("pA"));
            p.addParameter(Parameter.newEstimatedPositiveParameter("pG"));

            p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
            Optimizer o = new GoldenSection();

            Likelihood l = o.maximise(c, p);

            System.out.println("Done!");
            //System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
