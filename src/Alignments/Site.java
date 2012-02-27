package Alignments;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents a "site" in an "alignment".  Both terms used generously.
 * @author Daniel Money
 * @version 1.0
 */
public class Site implements Serializable
{
    /**
     * Creates a site in a alignment with no ambiguous data
     * @param sites Map from taxa name to state
     */
    public Site(LinkedHashMap<String, String> sites)
    {
        this(sites,new Ambiguous());
    }
    
    /**
     * Creates a site in a alignment with ambiguous data
     * @param sites Map from taxa name to state
     * @param ambig Description of ambiguous data
     */
    public Site(LinkedHashMap<String,String> sites, Ambiguous ambig)
    {
        this.sites = sites;
        this.ambig = ambig;
    }
    
    /**
     * Gets the possible characters for a given taxa.  This returns a set as it
     * takes into account ambiguous data.  For example if N is defined to be
     * A, C, T or G then this function will return a set containing A, C, T and
     * G if the raw data contains a N
     * @param taxa Taxa to return the character for
     * @return The character for the given taxa
     */
    public Set<String> getCharacter(String taxa)
    {
        return ambig.getPossible(sites.get(taxa));
    }
    
    /**
     * Gets the raw charcater for a given taxa.  This no account of ambiguous
     * data and will just return the raw character
     * @param taxa Taxa to return the character for
     * @return The character for the given taxa
     */
    public String getRawCharacter(String taxa)
    {
        return sites.get(taxa);
    }
    
    /**
     * Get the number of taxa
     * @return The number of taxa
     */
    public int getNum()
    {
        return sites.size();
    }
    
    /**
     * Get the set of taxa
     * @return Set containing taxa names
     */
    public Set<String> getTaxa()
    {
        return sites.keySet();
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Site))
        {
            return false;
        }
        
        Site c = (Site) o;
        
        return sites.equals(c.sites);
    }

    public int hashCode()
    {
        return sites.hashCode();
    }
    
    public String toString()
    {
        StringBuilder ret = new StringBuilder();
        for (Entry<String,String> s: sites.entrySet())
        {
            ret.append(s.getValue());
            ret.append(" ");
        }
        ret.delete(ret.length()-1, ret.length());
        return ret.toString();
    }
    
    /**
     * Recodes the site and returns it
     * @param recode A map from original state to new state, e.g. to recode
     * DNA to RY it would contains A -> R, G -> R, C -> Y, T -> Y
     * @return A recoded site
     */
    public Site recode(Map<String,String> recode)
    {
        LinkedHashMap<String,String> ns = new LinkedHashMap<>();
        for (Entry<String,String> s: sites.entrySet())
        {
            if (recode.containsKey(s.getValue()))
            {
                ns.put(s.getKey(),recode.get(s.getValue()));
            }
            else
            {
                ns.put(s.getKey(),s.getValue());
            }
        }
        return new Site(ns,ambig);
    }
    
    private static final long serialVersionUID = 1;
    
    private LinkedHashMap<String,String> sites;
    private Ambiguous ambig;
}
