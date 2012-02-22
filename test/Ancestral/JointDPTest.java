package Ancestral;

import Likelihood.Probabilities;
import Likelihood.Calculator;
import Alignments.Alignment;
import Alignments.SequenceAlignment;
import Alignments.Site;
import Ancestors.AncestralJoint;
import Maths.SquareMatrix;
import Maths.SquareMatrix.Calculation;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameters;
import Trees.Tree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the dynamic programming joint reconstruction method
 * @author Daniel Money
 */
public class JointDPTest
{
    /**
     * Tests by comparing GeLL to PAML output
     * @throws Exception Thrown if something went wrong!
     */
    @Test
    public void testReconstruction() throws Exception
    {
        SquareMatrix.setExpMethod(Calculation.EIGEN);
        
        Tree t = Tree.fromNewickString("(((Human: 0.041755, Chimpanzee: 0.053312)A: 0.016472, Gorilla: 0.058177)B: 0.052919, Orangutan: 0.100079, Gibbon: 0.139397)C;");
        Alignment a = new SequenceAlignment(new File("test\\PAML\\JointReconstruction\\brown.nuc"));

        String[][] ma = new String[4][4];

        ma[0][0] = "-"; ma[0][1] = "1.141084"; ma[0][2] = "0.094909"; ma[0][3] = "0.000804";
        ma[1][0] = "0.878293"; ma[1][1] = "-"; ma[1][2] = "0.170850"; ma[1][3] = "0.030534";
        ma[2][0] = "0.077029"; ma[2][1] = "0.180151"; ma[2][2] = "-"; ma[2][3] = "0.386656";
        ma[3][0] = "0.001923"; ma[3][1] = "0.094823"; ma[3][2] = "1.138759"; ma[3][3] = "-";
        
        String[] freq = {"0.25318", "0.32894", "0.31196", "0.10592"};
        
        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        Model m = new Model(new RateCategory(ma,freq,map));
        
        Calculator c = new Calculator(m,a,t);

        AncestralJoint aj = AncestralJoint.newInstance(m,a,t);

        Parameters p = t.getParameters();
        
        Alignment res = aj.calculate(p);

        String re = "([ACTG]{5}): ([ACTG]{3})";
        Pattern pattern = Pattern.compile(re);
        
        BufferedReader in = new BufferedReader(new FileReader("test\\PAML\\JointReconstruction\\rst"));
        
        String line;
        boolean good = true;
        boolean inJoint = false;
        while ((line = in.readLine()) != null)
        {
            if (line.contains("Joint reconstruction of ancestral sequences"))
            {
                inJoint = true;
            }
            if (inJoint)
            {
                Matcher match = pattern.matcher(line);
                if (match.find())
                {
                    String origSeq = match.group(1);

                    Site reconSite = null;
                    
                    for (Site os: res.getUniqueSites())
                    {
                        if (os.getRawCharacter("Human").equals(origSeq.substring(0, 1)) &&
                                os.getRawCharacter("Chimpanzee").equals(origSeq.substring(1, 2)) &&
                                os.getRawCharacter("Gorilla").equals(origSeq.substring(2, 3)) &&
                                os.getRawCharacter("Orangutan").equals(origSeq.substring(3, 4)) &&
                                os.getRawCharacter("Gibbon").equals(origSeq.substring(4, 5)))
                        {
                            reconSite = os;
                        }
                    }
                    
                    Probabilities P = new Probabilities(m,t,p);
                    
                    good = good && reconSite.getRawCharacter("A").equals(match.group(2).substring(2, 3));
                    good = good && reconSite.getRawCharacter("B").equals(match.group(2).substring(1, 2));
                    good = good && reconSite.getRawCharacter("C").equals(match.group(2).substring(0, 1));
                }
            }
        }
        in.close();
        
        assertTrue(good);
    }
}
