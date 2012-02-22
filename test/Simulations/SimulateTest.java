package Simulations;

import Parameters.Parameter;
import Likelihood.Probabilities;
import Likelihood.Calculator.SiteCalculator;
import Constraints.SiteConstraints;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import Alignments.Site;
import java.util.Map;
import Trees.Tree;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameters;
import java.util.HashMap;
import Alignments.Alignment;
import Maths.Gamma;
import Maths.SquareMatrix;
import Maths.SquareMatrix.Calculation;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the simulation method
 * @author Daniel Money
 */
public class SimulateTest
{
    /**
     * Tests by comparing the frequency of each state computed by counting simulator
     * output with the theoritical distribution (which is easily calculable for a small
     * tree like this).
     * @throws Exception Thrown if something went wrong!
     */
    @Test
    public void simulateTest() throws Exception
    {
        SquareMatrix.setExpMethod(Calculation.EIGEN);
        
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
        
        Tree t = Tree.fromNewickString("((human :0.1, chimpanzee :0.1) :0.12, gorilla :0.22, orangutan :0.4);");
        
        Parameters p = t.getParameters();
        
        p.addParameter(Parameter.newFixedParameter("a", 10.0));
        p.addParameter(Parameter.newFixedParameter("b", 5.0));
        p.addParameter(Parameter.newFixedParameter("c", 1.0));
        p.addParameter(Parameter.newFixedParameter("d", 2.0));
        p.addParameter(Parameter.newFixedParameter("e", 3.0));
        p.addParameter(Parameter.newFixedParameter("f", 1.0));
        
        p.addParameter(Parameter.newFixedParameter("pT",0.1));
        p.addParameter(Parameter.newFixedParameter("pC",0.2));
        p.addParameter(Parameter.newFixedParameter("pA",0.3));
        p.addParameter(Parameter.newFixedParameter("pG",0.4));
        
        p.addParameter(Parameter.newFixedParameter("g",0.5));
        
        Simulate sim = new Simulate(m,t,p);
        
        Alignment gepul = sim.getAlignment(1000000);
        
        Map<Site,Integer> gepulCounts = new HashMap<>();
        for (Site s: gepul)
        {
            if (gepulCounts.containsKey(s))
            {
                gepulCounts.put(s, gepulCounts.get(s) + 1);
            }
            else
            {
                gepulCounts.put(s, 1);
            }
        }
        
        
        Map<Site,Double> theory = new HashMap<>();
        Set<String> bases = new HashSet<>();
        bases.add("T");
        bases.add("C");
        bases.add("A");
        bases.add("G");
        Probabilities P = new Probabilities(m,t,p);
        for (String h: bases)
        {
            for (String c: bases)
            {
                for (String g: bases)
                {
                    for (String o: bases)
                    {
                        LinkedHashMap<String,String> sm = new LinkedHashMap<>();
                        sm.put("human", h);
                        sm.put("chimpanzee", c);
                        sm.put("gorilla", g);
                        sm.put("orangutan", o);
                        Site s = new Site(sm);
                        SiteCalculator sc = new SiteCalculator(s,t,new SiteConstraints(bases),P);
                        double l = sc.calculate().getLikelihood();
                        theory.put(s, l * 1000000);
                    }
                }
            }
        }   
        
        double chi2 = 0.0;
        for (Site s: theory.keySet())
        {
            chi2 += (Math.pow(gepulCounts.get(s) - theory.get(s),2) / theory.get(s));
        }
                
        assertTrue(chi2 < Gamma.chi2inv(0.95, 255));
    }
}
