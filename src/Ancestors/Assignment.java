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

package Ancestors;

import Alignments.Site;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Trees.Tree;
import Utils.ArrayMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * Stores the constraints for a single site
 * @author Daniel Money
 * @version 1.2
 */
public class Assignment
{
    /**
     * Standard constructor that creates an object with no constraints
     * @param allStates The set of all possible states
     */
    public Assignment(/*List<String> allStates*/)
    {
        con = new TreeMap<>();
        //def = allStates;
    }
    
    /**
     * Adds a constraint where a node is constrained to a single state
     * @param n The node
     * @param c The state it is constrained to
     */
    public void addAssignment(String n, String c)
    {
        //Set<String> s = new HashSet<>();
        //s.add(c);
        //con.put(n,s);
        con.put(n,c);
    }
    
    /**
     * Adds a constraint
     * @param n The node to add the constraint to
     * @param c The states the node is constrained to
     */
    //public void addConstraint(String n, Set<String> c)
    //{
    //    con.put(n,c);
    //}
    
    /**
     * Gets the constraints for a node
     * @param n The node to get constraints for
     * @return The states that node is constrained to
     */
    //public Set<String> getAssignment(String n)
    public String getAssignment(String n)
    {
        return con.get(n);
        /*if (con.get(n) != null)
        {
            return con.get(n);
        }
        else
        {
            Set<String> ret = new HashSet<>();
            ret.addAll(def);
            return ret;
        }*/
    }
    
    /**
     * Tests whether a given site (that should include data for internal nodes)
     * meets the defined constraints
     * @param s The site
     * @return Whether the constraints are met
     */
    /*public boolean meetsConstrains(Site s)
    {
        boolean good = true;
        for (Entry<String, Set<String>> e: con.entrySet())
        {
            good = good && e.getValue().containsAll(s.getCharacter(e.getKey()));
        }
        return good;
    }*/
    
    /**
     * Tests whether a node has a constraint
     * @param n The node
     * @return Whether the node is constrained
     */
    public boolean nodeIsAssigned(String n)
    {
        return con.containsKey(n);
    }
    
    public Assignment clone()
    {
        Assignment clone = new Assignment();
        for (Entry<String,String> e: con.entrySet())
        {
            clone.addAssignment(e.getKey(), e.getValue());
        }
        /*Assignment clone = new Assignment(def);
        for (Entry<String, Set<String>> e: con.entrySet())
        {
            Set<String> nv = new HashSet<>();
            for (String i: e.getValue())
            {
                nv.add(i);
            }
            clone.addConstraint(e.getKey(), nv);
        }*/
        return clone;
    }
    
    public ArrayMap<String, NodeLikelihood> getInitialNodeLikelihoods(Tree t,  Site s, ArrayMap<String,Integer> map) throws LikelihoodException    
    {        
        ArrayMap<String, NodeLikelihood> nodeLikelihoods = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
        for (String l: t.getLeaves())
        {
            //nodeLikelihoods.put(l, new NodeLikelihood(tp.getAllStates(), s.getCharacter(l)));
            nodeLikelihoods.put(l, new NodeLikelihood(map, s.getCharacter(l)));
        }

        //And now internal nodes using any constraints
        for (String i: t.getInternal())
        {
            //nodeLikelihoods.put(i, new NodeLikelihood(tp.getAllStates(), con.getConstraint(i)));
            if (getAssignment(i) != null)
            {
                nodeLikelihoods.put(i, new NodeLikelihood(map, getAssignment(i)));
            }
            else
            {
                nodeLikelihoods.put(i, new NodeLikelihood(map));
            }
            
        }
        return nodeLikelihoods;
    }
    
    //private List<String> def;
    //private Map<String,Set<String>> con;
    private Map<String,String> con;
}
