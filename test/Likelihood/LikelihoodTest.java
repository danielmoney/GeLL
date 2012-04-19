package Likelihood;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
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
import Alignments.PhylipAlignment;
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
 * @version 1.2
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
        a = new PhylipAlignment(new File("test\\PAML\\Likelihood\\brown.nuc"));

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
    
    /**
     * Tests the site class methods by splititng an alignment in two
     * and using a different model calculate the likelihood for each half.
     * Compare the sum of these two likelihoods with the result using the site
     * class method.
     * @throws Exception
     */
    @Test
    public void testClassLikelihood() throws Exception
    {
        String A = "AAGCTTCACCGGCGCAGTCA";
        String B = "AAGCTTCACCGGCGCAATTA";
        String C = "AAGCTTCACCGGCGCAGTTG";
        String D = "AAGCTTCACCGGCGCAACCA";
        String E = "AAGCTTTACAGGTGCAACCG";
        
        List<Site> all = new ArrayList<>();
        List<Site> p1 = new ArrayList<>();
        List<Site> p2 = new ArrayList<>();
        
        for (int i = 0; i < 20; i++)
        {
            LinkedHashMap<String,String> s = new LinkedHashMap<>();
            s.put("A",A.substring(i, i+1));
            s.put("B",B.substring(i, i+1));
            s.put("C",C.substring(i, i+1));
            s.put("D",D.substring(i, i+1));
            s.put("E",E.substring(i, i+1));
            if (i < 10)
            {
                all.add(new Site(s,"1"));
                p1.add(new Site(s));
            }
            else
            {
                all.add(new Site(s,"2"));
                p2.add(new Site(s));
            }
        }
        
        Alignment aa = new Alignment(all);
        Alignment a1 = new Alignment(p1);
        Alignment a2 = new Alignment(p2);
        
        Tree t = Tree.fromNewickString("(((A: 0.057987, B: 0.074612): 0.035490, C: 0.074352): 0.131394, D: 0.350156, E: 0.544601);");
        
        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        String[][] ma1 = new String[4][4];

        ma1[0][0] = "-"; ma1[0][1] = "1.370596"; ma1[0][2] = "0.039081"; ma1[0][3] = "0.000004";
        ma1[1][0] = "0.931256"; ma1[1][1] = "-"; ma1[1][2] = "0.072745"; ma1[1][3] = "0.004875";
        ma1[2][0] = "0.028434"; ma1[2][1] = "0.077896"; ma1[2][2] = "-"; ma1[2][3] = "0.439244";
        ma1[3][0] = "0.000011"; ma1[3][1] = "0.017541"; ma1[3][2] = "1.475874"; ma1[3][3] = "-";

        String[] freq1 = {"0.23500", "0.34587", "0.32300", "0.09613"};

        Model m1 = new Model(new RateCategory(ma1,freq1,map));
        
        String[][] ma2 = new String[4][4];

        ma2[0][0] = "-"; ma2[0][1] = "0.370596"; ma2[0][2] = "1.039081"; ma2[0][3] = "0.000004";
        ma2[1][0] = "0.931256"; ma2[1][1] = "-"; ma2[1][2] = "1.072745"; ma2[1][3] = "0.004875";
        ma2[2][0] = "0.028434"; ma2[2][1] = "1.077896"; ma2[2][2] = "-"; ma2[2][3] = "0.439244";
        ma2[3][0] = "0.000011"; ma2[3][1] = "0.017541"; ma2[3][2] = "1.475874"; ma2[3][3] = "-";

        String[] freq2 = {"0.33500", "0.24587", "0.30300", "0.11613"};

        Model m2 = new Model(new RateCategory(ma2,freq2,map));
    
        Parameters p = t.getParameters();
        
        Map<String,Model> ma = new HashMap<>();
        ma.put("1",m1);
        ma.put("2",m2);
        
        Calculator c1 = new Calculator(m1,a1,t);
        Calculator c2 = new Calculator(m2,a2,t);
        Calculator ca = new Calculator(ma,aa,t);
        
        double l1 = c1.calculate(p).getLikelihood();
        //Cloning is due to known issue - see documentation.
        double l2 = c2.calculate(p.clone()).getLikelihood();
        double la = ca.calculate(p.clone()).getLikelihood();
        
        assertTrue(Math.abs(l1 + l2 - la) < 1e-10);
    }
    
    private static Likelihood l;
    private static Alignment a;
}
