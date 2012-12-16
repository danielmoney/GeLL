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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * Stores the ancestral reconstruction assignment for a single site
 * @author Daniel Money
 * @version 2.0
 */
public class Assignment
{
    /**
     * Standard constructor that creates an object with no assignments
     */
    public Assignment()
    {
        assign = new TreeMap<>();
    }
    
    /**
     * Adds a asssignment 
     * @param n The node
     * @param c The state it is assigned
     */
    public void addAssignment(String n, String c)
    {
        assign.put(n,c);
    }
    
    /**
     * Gets the assignment for a node
     * @param n The node to get the assignment for
     * @return The state that node is assigned
     */
    public String getAssignment(String n)
    {
        return assign.get(n);
    }
    
    /**
     * Tests whether a node has an assignment
     * @param n The node
     * @return Whether the node is assigned
     */
    public boolean nodeIsAssigned(String n)
    {
        return assign.containsKey(n);
    }
    
    public Assignment clone()
    {
        Assignment clone = new Assignment();
        for (Entry<String,String> e: assign.entrySet())
        {
            clone.addAssignment(e.getKey(), e.getValue());
        }
        return clone;
    }
    
    /**
     * Creates initial node likelihoods using this assignment.  Internal nodes
     * with an assignment are set so only that state is allowed.
     * @param t The tree to create the node likelihood for.
     * @param s The site being reconstructed
     * @param map A map from state to position in array
     * @return An Map of NodeLikelihoods which can be used to initialise
     * likelihood calculations
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if all states are initialised to a zero likelihood 
     */
    public Map<String, NodeLikelihood> getInitialNodeLikelihoods(Tree t,  Site s, Map<String,Integer> map) throws LikelihoodException    
    {        
        Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(t.getNumberBranches() + 1);
        for (String l: t.getLeaves())
        {
            nodeLikelihoods.put(l, new NodeLikelihood(map, s.getCharacter(l)));
        }

        //And now internal nodes
        for (String i: t.getInternal())
        {
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

    private Map<String,String> assign;
}
