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

import Maths.SquareMatrix;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory;
import Models.RateCategory.RateException;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores the values of each transition, frequency etc for one set of parameters.
 * @author Daniel Money
 * @version 2.0
 */
public class Probabilities
{
    /**
     * Constructor
     * @param m The model
     * @param t The tree
     * @param p The parameters
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     */
    public Probabilities(Model m, Tree t, Parameters p) throws TreeException,
            RateException, ModelException, ParameterException
    {
        //Create a new tree with the branch lengths given in p.  This is a bit
        //odd and could do with changing but it works for now.
        Tree nt = new Tree(t,p);
        //Set the parameters in the model
        m.setParameters(p);
        rateClasses = m.getRates();
        map = m.getMap();
        P = new HashMap<>(rateClasses.size());
        freq = new HashMap<>();
        roots = new HashMap<>();
        rateP = new HashMap<>();
        states = map.keySet();
        //Calculate and store the various probabilities
        for (RateCategory rc: m)
        {
            Map<Branch,SquareMatrix> bP = new HashMap<>(nt.getNumberBranches());
            for (Branch b: nt)
            {
                if (b.getLength() < 0)
                {
                    throw new TreeException("Can't do Likelihood calculations with negative branch lengths");
                }
                bP.put(b, rc.getP(b.getLength()));
            }
            P.put(rc, new RateProbabilities(bP));
            freq.put(rc,rc.getFreq());
            roots.put(rc,rc.getRoot());
            rateP.put(rc,m.getFreq(rc));
        }
    }
    
    /**
     * Gets the set of RateClasses that we have calculated probailities for
     * @return Set of RateClasses
     */
    public Set<RateCategory> getRateCategory()
    {
        return rateClasses;
    }
    
    /**
     * Gets the list of all possible states
     * @return A list of all possible states
     */
    public Set<String> getAllStates()
    {
        return states;
    }

    /**
     * Gets a specific probability for a change from one state to another along
     * a specific branch under a specific RateClass
     * @param r The rate class
     * @return The probability of a transition from start state to end state along
     * the branch under the specified rate class.
     */
    public RateProbabilities getP(RateCategory r)
    {
        return P.get(r);
    }
    
    /**
     * Get a root object, for the given rate category, that can be used to 
     * the total likelihood from the root node likelihoods or provide the
     * frequencies of the various states at the root
     * @param r The rate category to get the root object for
     * @return A root object
     */
    public Root getRoot(RateCategory r)
    {
        return roots.get(r);
    }
    
    /**
     * Gets the probability of a rate class
     * @param r the rate class
     * @return The probability of the rate class
     */
    public double getRateP(RateCategory r)
    {
        return rateP.get(r);
    }
    
    /**
     * Gets an Map linking states to position in an array
     * @return Map linking state to positions
     */
    public Map<String,Integer> getMap()
    {
        return map;
    }
    
    private Set<RateCategory> rateClasses;
    private Map<String,Integer> map;
    private Map<RateCategory,RateProbabilities> P;
    private Map<RateCategory,double[]> freq;
    private Map<RateCategory,Root> roots;
    private Map<RateCategory,Double> rateP;
    private Set<String> states;
    
    /**
     * Stores the values of each transition, frequency etc for one set of parameters
     * and for a single rate category.
     * @author Daniel Money
     * @version 1.0
     */
    public class RateProbabilities
    {
        RateProbabilities(Map<Branch,SquareMatrix> P)
        {
            this.P = P;
        }
        
        /**
         * Gets the probability matrix for a single branch
         * @param b The branch to get the matrix for
         * @return The probability matrix associated with that branch
         */
        public SquareMatrix getP(Branch b)
        {
            return P.get(b);
        }
        
        /**
         * Gets the probability fo a single transition on a single branch
         * @param b The branch
         * @param startState The start state
         * @param endState The end state
         * @return The probabiluty of a transition from start state to end state
         * along the given branch
         */
        public double getP(Branch b, String startState, String endState)
        {
            return P.get(b).getPosition(map.get(endState),map.get(startState));
        }
        
        private Map<Branch,SquareMatrix> P;
    }
}
