package Constraints;

import Alignments.Site;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;


/**
 * Stores the constraints for a single site
 * @author Daniel Money
 * @version 1.0
 */
public class SiteConstraints
{
    /**
     * Standard constructor that creates an object with no constraints
     * @param allStates The set of all possible states
     */
    public SiteConstraints(List<String> allStates)
    {
        con = new TreeMap<>();
        def = allStates;
    }
    
    /**
     * Adds a constraint where a node is constrained to a single state
     * @param n The node
     * @param c The state it is constrained to
     */
    public void addConstraint(String n, String c)
    {
        Set<String> s = new HashSet<>();
        s.add(c);
        con.put(n,s);
    }
    
    /**
     * Adds a constraint
     * @param n The node to add the constraint to
     * @param c The states the node is constrained to
     */
    public void addConstraint(String n, Set<String> c)
    {
        con.put(n,c);
    }
    
    /**
     * Gets the constraints for a node
     * @param n The node to get constraints for
     * @return The states that node is constrained to
     */
    public Set<String> getConstraint(String n)
    {
        if (con.get(n) != null)
        {
            return con.get(n);
        }
        else
        {
            Set<String> ret = new HashSet<>();
            ret.addAll(def);
            return ret;
        }
    }
    
    /**
     * Tests whether a given site (that should include data for internal nodes)
     * meets the defined constraints
     * @param s The site
     * @return Whether the constraints are met
     */
    public boolean meetsConstrains(Site s)
    {
        boolean good = true;
        for (Entry<String, Set<String>> e: con.entrySet())
        {
            good = good && e.getValue().containsAll(s.getCharacter(e.getKey()));
        }
        return good;
    }
    
    /**
     * Tests whether a node has a constraint
     * @param n The node
     * @return Whether the node is constrained
     */
    public boolean nodeIsConstrained(String n)
    {
        return con.containsKey(n);
    }
    
    public SiteConstraints clone()
    {
        SiteConstraints clone = new SiteConstraints(def);
        for (Entry<String, Set<String>> e: con.entrySet())
        {
            Set<String> nv = new HashSet<>();
            for (String i: e.getValue())
            {
                nv.add(i);
            }
            clone.addConstraint(e.getKey(), nv);
        }
        return clone;
    }
    
    private List<String> def;
    private Map<String,Set<String>> con;
}
