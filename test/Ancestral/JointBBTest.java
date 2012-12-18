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
import Likelihood.Probabilities.RateProbabilities;
import Likelihood.SiteLikelihood;
import Likelihood.SiteLikelihood.RateLikelihood;
import Maths.Real;
import Maths.SquareMatrix;
import Trees.Branch;
import java.util.Map;
import Likelihood.Calculator.SiteCalculator;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import java.util.ArrayList;
import java.util.List;
import Alignments.Alignment;
import Alignments.PhylipAlignment;
import Alignments.Site;
import Ancestors.AncestralJoint;
import Likelihood.Probabilities;
import Models.Model;
import Models.RateCategory;
import Parameters.Parameter;
import Parameters.Parameters;
import Ancestors.Assignment;
import Trees.Tree;
import java.io.File;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the branch-and-bound joint reconstruction method
 * @author Daniel Money
 * @version 2.0
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
        Site s) throws LikelihoodException
    {
        List<String> bases = new ArrayList<>();
        bases.add("A"); bases.add("C"); bases.add("G"); bases.add("T");
        double maxL = -Double.MAX_VALUE;
        String[] maxA = new String[3];
        for (String a: bases)
        {
            for (String b: bases)
            {
                for (String c: bases)
                {
                    Assignment sc = new Assignment();
                    sc.addAssignment("A", a);
                    sc.addAssignment("B", b);
                    sc.addAssignment("C", c);
                    
                    Map<String, NodeLikelihood> nl = new HashMap<>(t.getNumberBranches() + 1);
                    for (String l: t.getLeaves())
                    {
                        nl.put(l, new NodeLikelihood(P.getMap(), s.getCharacter(l)));
                    }

                    //And now internal nodes
                    for (String i: t.getInternal())
                    {
                        nl.put(i, new NodeLikelihood(P.getMap(), sc.getAssignment(i)));
                    }
                    
                    //SiteCalculator calc = new SiteCalculator(t,P,nl);
                    //double l = calc.calculate().getLikelihood().toDouble();
                    double l = calculateSite(t,P,nl).getLikelihood().toDouble();
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
    
    public SiteLikelihood calculateSite(Tree t, Probabilities tp, Map<String,NodeLikelihood> nl)
    {    
        List<Branch> branches = t.getBranches();
        Map<RateCategory,RateLikelihood> rateLikelihoods = new HashMap<>(tp.getRateCategory().size());

        //Calculate the likelihood for each RateCategory
        for (RateCategory rc: tp.getRateCategory())
        {
            RateProbabilities rp = tp.getP(rc);
            //Initalise the lieklihood values at each node.  first internal
            //using the alignemnt.
            Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(branches.size() + 1);
            for (String l: t.getLeaves())
            {
                nodeLikelihoods.put(l, nl.get(l).clone());
            }

            //And now internal nodes
            for (String i: t.getInternal())
            {
                nodeLikelihoods.put(i, nl.get(i).clone());
            }

            //for each branch.  The order these are returned in from tree means
            //we visit any branch with a node as it's parent before we visit
            //the branch with the node as the child.  Hence we treverse the
            //tree in the standard manner.  For each branch we will update
            //the likelihood at the parent node...
            for (Branch b: branches)
            {
                //BranchProbabilities bp = rp.getP(b);
                SquareMatrix bp = rp.getP(b);
                //So for each state at the parent node...
                //for (String endState: tp.getAllStates())
                for (String endState: tp.getAllStates())
                {
                    //l keeps track of the total likelihood from each possible
                    //state at the child
                    //Real l = SiteLikelihood.getReal(0.0);//new Real(0.0);
                    //For each possible child state
                    //for (String startState: tp.getAllStates())
                    Real[] n = nodeLikelihoods.get(b.getChild()).getLikelihoods();
                    //for (int j = 0; j < tp.getAllStates().size(); j ++)
                    Real l = n[0].multiply(bp.getPosition(tp.getMap().get(endState), 0));
                    for (int j = 1; j < n.length; j++)
                    {
                        //Add the likelihood of going from start state to
                        //end state along that branch in that ratecategory
                        l = l.add(n[j].multiply(bp.getPosition(tp.getMap().get(endState), j)));
                    }
                    //Now multiply the likelihood of the parent by this total value.
                    //This will happen for each possible child as per standard techniques
                    nodeLikelihoods.get(b.getParent()).multiply(endState,l);
                }
            }

            //Rate total traxcks the total likelihood for this site and rate category
            Real ratetotal = null;//SiteLikelihood.getReal(0.0);//new Real(0.0);
            //Get the root likelihoods
            NodeLikelihood rootL = nodeLikelihoods.get(t.getRoot());

            ratetotal = tp.getRoot(rc).calculate(rootL);

            //For each possible state
            /*for (String state: this.tp.getAllStates())
            {
                try
                {
                    //Get the likelihood at the root, multiply by it's root frequency
                    //and add to the ratde total.
                    if (ratetotal == null)
                    {
                        ratetotal = rootL.getLikelihood(state).multiply(tp.getFreq(rc,state));
                    }
                    else
                    {
                        ratetotal = ratetotal.add(rootL.getLikelihood(state).multiply(tp.getFreq(rc,state)));
                    }
                }
                catch (LikelihoodException ex)
                {
                    //Shouldn't reach here as we know what oinformation should
                    // have been claculated and only ask for that
                    throw new UnexpectedError(ex);
                }
            }*/
            //Store the results for that rate
            rateLikelihoods.put(rc, new RateLikelihood(ratetotal,nodeLikelihoods));
            //Update the total site likelihood with the likelihood for the rate
            //category multiplied by the probility of being in that category
        }
        //return an object containg the results for that site
        return new SiteLikelihood(rateLikelihoods, tp);
    }
}
