/*
 * This file is part of GeLL.
 * 
 * GeLL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeLL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeLL.  If not, see <http://www.gnu.org/licenses/>.
 */

package Ancestral;
import Likelihood.Likelihood.NodeLikelihood;
import Utils.ArrayMap;
import java.util.ArrayList;
import java.util.List;
import Alignments.Alignment;
import Alignments.PhylipAlignment;
import Alignments.Site;
import Ancestors.AncestralJoint;
import Likelihood.Calculator.SiteCalculator;
import Likelihood.Probabilities;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameter;
import Parameters.Parameters;
import Constraints.SiteConstraints;
import Trees.Tree;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the branch-and-bound joint reconstruction method
 * @author Daniel Money
 * @version 1.2
 */
public class JointBBTest
{
    /**
     * Compares the reconstructed alignment at each site tone computed by
     * exhaustively calculating the likelihood of all possible reconstructions
     * @throws Exception Thrown if something went wrong!
     */
    @Test
    public void testReconstruction() throws Exception
    {
        Tree t = Tree.fromNewickString("(((Human: 0.057987, Chimpanzee: 0.074612)A: 0.035490, Gorilla: 0.074352)B: 0.131394, Orangutan: 0.350156, Gibbon: 0.544601)C;");
        Alignment a = new PhylipAlignment(new File("test\\PAML\\Likelihood\\brown.nuc"));

        String[][] ma = new String[4][4];

        ma[0][0] = "-"; ma[0][1] = "1.370596"; ma[0][2] = "0.039081"; ma[0][3] = "0.000004";
        ma[1][0] = "0.931256"; ma[1][1] = "-"; ma[1][2] = "0.072745"; ma[1][3] = "0.004875";
        ma[2][0] = "0.028434"; ma[2][1] = "0.077896"; ma[2][2] = "-"; ma[2][3] = "0.439244";
        ma[3][0] = "0.000011"; ma[3][1] = "0.017541"; ma[3][2] = "1.475874"; ma[3][3] = "-";

        String[] freq = {"0.23500", "0.34587", "0.32300", "0.09613"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        Model m = Model.gammaRates(new RateCategory(ma,freq,map),"g",4);

        Parameters p = t.getParameters();
        p.addParameter(Parameter.newFixedParameter("g", 0.19249));
        
        Probabilities P = new Probabilities(m,t,p);

        AncestralJoint aj = AncestralJoint.newInstance(m, a, t);
        
        Alignment rec = aj.calculate(p);
        
        boolean good = true;
        
        for (Site s: a.getUniqueSites())
        {
            Site reconSite = null;
            for (Site os: rec.getUniqueSites())
            {
                if (os.getRawCharacter("Human").equals(s.getRawCharacter("Human")) &&
                        os.getRawCharacter("Chimpanzee").equals(s.getRawCharacter("Chimpanzee")) &&
                        os.getRawCharacter("Gorilla").equals(s.getRawCharacter("Gorilla")) &&
                        os.getRawCharacter("Orangutan").equals(s.getRawCharacter("Orangutan")) &&
                        os.getRawCharacter("Gibbon").equals(s.getRawCharacter("Gibbon")))
                {
                    reconSite = os;
                }
            }
            String[] exhaust = getBest(t,P,s);
            good = good && exhaust[0].equals(reconSite.getRawCharacter("A"));
            good = good && exhaust[1].equals(reconSite.getRawCharacter("B"));
            good = good && exhaust[2].equals(reconSite.getRawCharacter("C"));
        }
        
        assertTrue(good);
    }
    
    private String[] getBest(Tree t, Probabilities P,
        Site s)
    {
        List<String> bases = new ArrayList<>();
        bases.add("A"); bases.add("C"); bases.add("G"); bases.add("T");
        Map<String,Map<String,Map<String,Double>>> hash = new HashMap<>();
        double maxL = -Double.MAX_VALUE;
        String[] maxA = new String[3];
        for (String a: bases)
        {
            hash.put(a, new HashMap<String,Map<String,Double>>());
            for (String b: bases)
            {
                hash.get(a).put(b, new HashMap<String,Double>());
                for (String c: bases)
                {
                    SiteConstraints sc = new SiteConstraints(bases);
                    sc.addConstraint("A", a);
                    sc.addConstraint("B", b);
                    sc.addConstraint("C", c);
                    
                    ArrayMap<String, NodeLikelihood> nl = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
                    for (String l: t.getLeaves())
                    {
                        nl.put(l, new NodeLikelihood(P.getArrayMap(), s.getCharacter(l)));
                    }

                    //And now internal nodes using any constraints
                    for (String i: t.getInternal())
                    {
                        nl.put(i, new NodeLikelihood(P.getArrayMap(), sc.getConstraint(i)));
                    }
                    
                    SiteCalculator calc = new SiteCalculator(t,P,nl);
                    double l = calc.calculate().getLikelihood();
                    if (l > maxL)
                    {
                        maxL = l;
                        maxA[0] = a;
                        maxA[1] = b;
                        maxA[2] = c;
                    }
                }
            }
        }
        return maxA;
    }
}
