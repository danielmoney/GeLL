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

package Likelihood;

import java.util.LinkedHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Likelihood.Likelihood.SiteLikelihood;
import Alignments.Site;
import org.junit.Test;
import org.junit.BeforeClass;
import Alignments.Alignment;
import Alignments.SequenceAlignment;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameter;
import Parameters.Parameters;
import Trees.Tree;
import java.io.File;
import java.util.HashMap;
import static org.junit.Assert.*;

/**
 * Tests whether we're doing likeliohood calcualations right by comparing results
 * to PAML
 * @author Daniel Money
 * @version 1.0
 */
public class LikelihoodTest
{
    /**
     * Creates the Likelihood data structure that will be tested.
     * @throws Exception Thrown if something went wrong!
     */
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        Tree t = Tree.fromNewickString("(((Human: 0.057987, Chimpanzee: 0.074612)A: 0.035490, Gorilla: 0.074352)B: 0.131394, Orangutan: 0.350156, Gibbon: 0.544601)C;");
        a = new SequenceAlignment(new File("test\\PAML\\Likelihood\\brown.nuc"));

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

        Calculator c = new Calculator(m,a,t);

        Parameters p = t.getParameters();
        p.addParameter(Parameter.newFixedParameter("g", 0.19249));

        l = c.calculate(p);
    }
    
    /**
     * Tests the likelihood by comparing GeLL to PAML
     */
    @Test
    public void testTotalLikelihood()
    {
        assertTrue(Math.log10(Math.abs(l.getLikelihood() - -2616.073763)) < -3);
    }
    
    /**
     * Tests the rate assignments by comaping GeLL to PAML
     * @throws Exception
     */
    @Test
    public void testRateAssignments() throws Exception
    {
        BufferedReader in = new BufferedReader(new FileReader("test\\PAML\\Likelihood\\rates"));
        
        String re = "([ACTG]{5})\\s+\\S+\\s+(\\d)";
        Pattern pattern = Pattern.compile(re);
        
        String line;
        HashMap<Site,String> pamlCats = new HashMap<>();
        while ((line = in.readLine()) != null)
        {
            Matcher match = pattern.matcher(line);
            if (match.find())
            {
                String seq = match.group(1);
                LinkedHashMap<String,String> siteMap = new LinkedHashMap<>();
                siteMap.put("Human",seq.substring(0, 1));
                siteMap.put("Chimpanzee",seq.substring(1, 2));
                siteMap.put("Gorilla",seq.substring(2, 3));
                siteMap.put("Orangutan",seq.substring(3, 4));
                siteMap.put("Gibbon",seq.substring(4, 5));
                Site s = new Site(siteMap);
                pamlCats.put(s,match.group(2));
            }
        }
        
        boolean good = true;
        for (Site s: a.getUniqueSites())
        {
            SiteLikelihood sl = l.getSiteLikelihood(s);
            RateCategory mostProbable = sl.getMostProbableRateCategory();
            String gepulCat = mostProbable.getName().substring(15);
            good = good && gepulCat.equals(pamlCats.get(s));
        }
        
        assertTrue(good);
    }
    
    private static Likelihood l;
    private static Alignment a;
}
