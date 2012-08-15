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
import Utils.ArrayMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the values of each transition, frequency etc for one set of parameters.
 * @author Daniel Money
 * @version 1.3
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
        map = m.getArrayMap();
        P = new ArrayMap<>(RateCategory.class,RateProbabilities.class,rateClasses.size());
        freq = new HashMap<>();
        rateP = new HashMap<>();
        states = map.keyList();
        //Calculate and store the various probabilities
        for (RateCategory rc: m)
        {
            ArrayMap<Branch,SquareMatrix> bP = new ArrayMap<>(Branch.class,SquareMatrix.class,nt.getNumberBranches());
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
        HashSet<String> ret = new HashSet<>();
        ret.addAll(states);
        return ret;
    }
    
    /**
     * Gets the list of all possible states.  Called this as {@link #getAllStates()} 
     * is kept for backward compitability
     * @return A list of all possible states
     */
    public List<String> getAllStatesAsList()
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
     * Gets the root frequency for a specified state under a specified RateClass
     * @param r The rate class
     * @param state The state
     * @return The frequency of the state under the rate class
     */
    public double getFreq(RateCategory r, String state)
    {
        return freq.get(r)[map.get(state)];
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
     * Gets an ArrayMap linking states to position in an array
     * @return Map linking state to positions
     */
    public ArrayMap<String,Integer> getArrayMap()
    {
        return map;
    }
    
    private Set<RateCategory> rateClasses;
    private ArrayMap<String,Integer> map;
    private ArrayMap<RateCategory,RateProbabilities> P;
    private Map<RateCategory,double[]> freq;
    private Map<RateCategory,Double> rateP;
    private ArrayList<String> states;
    
    /**
     * Stores the values of each transition, frequency etc for one set of parameters
     * and for a single rate category.
     * @author Daniel Money
     * @version 1.0
     */
    public class RateProbabilities
    {
        RateProbabilities(ArrayMap<Branch,SquareMatrix> P)
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
        
        private ArrayMap<Branch,SquareMatrix> P;
    }
}
