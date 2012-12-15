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

import java.util.HashSet;
import Alignments.Alignment;
import Alignments.PhylipAlignment;
import Alignments.Site;
import Ancestors.AncestralMarginal;
import Ancestors.AncestralMarginal.Result;
import Ancestors.AncestralMarginal.SiteResult;
import Maths.SquareMatrix;
import Maths.SquareMatrix.Calculation;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameter;
import Parameters.Parameters;
import Trees.Tree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the marginal reconstruction method
 * @author Daniel Money
 * @version 2.0
 */
public class MarginalTest
{
    /**
     * Tests by comparing GeLL to PAML output
     * @throws Exception Thrown if something went wrong!
     */
    @Test
    public void testReconstruction() throws Exception
    {
        SquareMatrix.setExpMethod(Calculation.EIGEN);
        
        Tree t = Tree.fromNewickString("(((Human: 0.056428, Chimpanzee: 0.070879)A: 0.029155, Gorilla: 0.073129)B: 0.102499, Orangutan: 0.274348, Gibbon: 0.433137)C;");
        Alignment a = new PhylipAlignment(new File("test\\PAML\\MarginalReconstruction\\brown.nuc"));

        String[][] ma = new String[4][4];

        ma[0][0] = "-"; ma[0][1] = "1.245580"; ma[0][2] = "0.041083"; ma[0][3] = "0.000005";
        ma[1][0] = "0.958724"; ma[1][1] = "-"; ma[1][2] = "0.091353"; ma[1][3] = "0.008317";
        ma[2][0] = "0.033343"; ma[2][1] = "0.096326"; ma[2][2] = "-"; ma[2][3] = "0.453433";
        ma[3][0] = "0.000011"; ma[3][1] = "0.025828"; ma[3][2] = "1.335427"; ma[3][3] = "-";

        String[] freq = {"0.25318",  "0.32894",  "0.31196",  "0.10592"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        Model m = Model.gammaRates(new RateCategory(ma,freq,map),"g",4);

        AncestralMarginal am = new AncestralMarginal(m,a,t);

        Parameters p = t.getParameters();
        p.addParameter(Parameter.newFixedParameter("g", 0.22539));

        Result res = am.calculate(p);

        String re = "([ACTG]{5}).*([ACTG])\\((\\d\\.\\d+)\\).*([ACTG])\\((\\d\\.\\d+)\\).*([ACTG])\\((\\d\\.\\d+)\\)";
        Pattern pattern = Pattern.compile(re);
        
        BufferedReader in = new BufferedReader(new FileReader("test\\PAML\\MarginalReconstruction\\rst"));
        
        String line;
        boolean good = true;
        HashSet<Site> unique = new HashSet<>();
        while ((line = in.readLine()) != null)
        {
            Matcher match = pattern.matcher(line);
            if (match.find())
            {
                String origSeq = match.group(1);
                
                LinkedHashMap<String,String> origSiteMap = new LinkedHashMap<>();
                origSiteMap.put("Human",origSeq.substring(0, 1));
                origSiteMap.put("Chimpanzee",origSeq.substring(1, 2));
                origSiteMap.put("Gorilla",origSeq.substring(2, 3));
                origSiteMap.put("Orangutan",origSeq.substring(3, 4));
                origSiteMap.put("Gibbon",origSeq.substring(4, 5));
                
                Site origSite = new Site(origSiteMap);
                
                if (!unique.contains(origSite))
                {
                    unique.add(origSite);
                    SiteResult sr = res.getSiteResult(origSite);

                    Site reconSite = sr.getSite();
                    NumberFormat nf = NumberFormat.getNumberInstance();
                    nf.setMaximumFractionDigits(3);
                    nf.setMinimumFractionDigits(3);

                    good = good && reconSite.getRawCharacter("A").equals(match.group(6));
                    good = good && reconSite.getRawCharacter("B").equals(match.group(4));
                    good = good && reconSite.getRawCharacter("C").equals(match.group(2));
                    good = good && ((sr.getProbability("A", reconSite.getRawCharacter("A")).toDouble() - Double.parseDouble(match.group(7))) <= 0.001);
                    good = good && ((sr.getProbability("B", reconSite.getRawCharacter("B")).toDouble() - Double.parseDouble(match.group(5))) <= 0.001);
                    good = good && ((sr.getProbability("C", reconSite.getRawCharacter("C")).toDouble() - Double.parseDouble(match.group(3))) <= 0.001);
                }
            }
        }
        in.close();
        
        assertTrue(good);
    }
}
