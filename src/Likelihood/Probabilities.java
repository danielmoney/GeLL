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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the values of each transition, frequency etc for one set of parameters.
 * @author Daniel Money
 * @version 1.0
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
        //P = new HashMap<>();
        //P = new ArrayMap<>(rateClasses.size());
        P = new ArrayMap<>(RateCategory.class,RateProbabilities.class,rateClasses.size());
        freq = new HashMap<>();
        rateP = new HashMap<>();
        states = map.keyList();
        //Calculate and store the various probabilities
        for (RateCategory rc: m)
        {
            //ArrayMap<Branch,BranchProbabilities> bP = new ArrayMap<>(nt.getNumberBranches());
            //ArrayMap<Branch,SquareMatrix> bP = new ArrayMap<>(nt.getNumberBranches());
            ArrayMap<Branch,SquareMatrix> bP = new ArrayMap<>(Branch.class,SquareMatrix.class,nt.getNumberBranches());
            for (Branch b: nt)
            {
                //bP.put(b, new BranchProbabilities(rc.getP(b.getLength())));
                bP.put(b, rc.getP(b.getLength()));
            }
            P.put(rc, new RateProbabilities(bP));
            /*Map<Branch,SquareMatrix> rcP = new HashMap<>();
            for (Branch b: nt)
            {
                rcP.put(b,rc.getP(b.getLength()));
            }
            P.put(rc,rcP);*/
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
     * Gets the set of all possible states
     * @return The set of all possible states
     */
    public List<String> getAllStates()
    {
        return states;
    }

    /**
     * Gets a specific probability for a change from one state to another along
     * a specific branch under a specific RateClass
     * @param r The rate class
     * @param b The branch
     * @param startState The start state
     * @param endState The end state
     * @return The probability of a transition from start state to end state along
     * the branch under the specified rate class.
     */
    public RateProbabilities getP(RateCategory r)
    //public double getP(RateCategory r, Branch b, String startState, String endState)
    {
        return P.get(r); //.get(b).getPosition(
                //map.get(endState),
                //map.get(startState));
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
    
    public ArrayMap<String,Integer> getMap()
    {
        return map;
    }
    
    private Set<RateCategory> rateClasses;
    private ArrayMap<String,Integer> map;
    //private ArrayMap<RateCategory,Map<Branch,SquareMatrix>> P;
    private ArrayMap<RateCategory,RateProbabilities> P;
    private Map<RateCategory,double[]> freq;
    private Map<RateCategory,Double> rateP;
    private ArrayList<String> states;
    
    public class RateProbabilities
    {
        //public RateProbabilities(ArrayMap<Branch,BranchProbabilities> P)
        public RateProbabilities(ArrayMap<Branch,SquareMatrix> P)
        {
            this.P = P;
        }
        
        //public BranchProbabilities getP(Branch b)
        public SquareMatrix getP(Branch b)
        {
            return P.get(b);
        }
        
        public double getP(Branch b, String startState, String endState)
        {
            return P.get(b).getPosition(map.get(endState),map.get(startState));
        }
        
        //private ArrayMap<Branch,BranchProbabilities> P;
        private ArrayMap<Branch,SquareMatrix> P;
    }
    
    public class BranchProbabilities
    {
        public BranchProbabilities(SquareMatrix m)
        {
            this.m = m;
        }
        
        public double getP(String startState, String endState)
        {
            //System.err.println(m.getPosition(map.get(endState),map.get(startState)));
            return m.getPosition(map.get(endState),map.get(startState));
        }
        
        public double getP(int startState, int endState)
        {
            //System.err.println(m.getPosition(endState, startState));
            return m.getPosition(endState, startState);
        }
        
        private SquareMatrix m;
    }
}
