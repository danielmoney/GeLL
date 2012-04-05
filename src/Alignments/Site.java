package Alignments;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents a "site" in an "alignment".  Both terms used generously.
 * @author Daniel Money
 * @version 1.1
 */
public class Site implements Serializable
{
    /**
     * Creates a site in a alignment with no ambiguous data
     * @param sites Map from taxa name to state
     */
    public Site(LinkedHashMap<String, String> sites)
    {
        this(sites,new Ambiguous(), null);
    }
    
    /**
     * Creates a site in a alignment with no ambiguous data and with the given
     * class.  <b>Note that either all sites in an alignment must have a class or
     * none should.</b>
     * @param sites Map from taxa name to state
     * @param siteClass The class of this site
     */
    public Site(LinkedHashMap<String,String> sites, String siteClass)
    {
        this(sites,new Ambiguous(), siteClass);
    }
    
    /**
     * Creates a site in a alignment with ambiguous data
     * @param sites Map from taxa name to state
     * @param ambig Description of ambiguous data
     */
    public Site(LinkedHashMap<String,String> sites, Ambiguous ambig)
    {
        this(sites,ambig,null);
    }
    
    /**
     * Creates a site in a alignment with ambiguous data and with the given
     * class.  <b>Note that either all sites in an alignment must have a class or
     * none should.</b>
     * @param sites Map from taxa name to state
     * @param ambig Description of ambiguous data
     * @param siteClass The class of this site
     */
    public Site(LinkedHashMap<String,String> sites, Ambiguous ambig, String siteClass)
    {
        this.sites = sites;
        this.ambig = ambig;
        this.siteClass = siteClass;
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
    
    /**
     * Gets the class of this site
     * @return The class of this site
     */
    public String getSiteClass()
    {
        return siteClass;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Site))
        {
            return false;
        }
        
        Site c = (Site) o;
        boolean sc = false;
        if ((siteClass == null) && (c.siteClass == null))
        {
            sc = true;
        }
        if ((siteClass != null) && (c.siteClass != null))
        {
            sc = siteClass.equals(c.siteClass);
        }
        
        return (sc && sites.equals(c.sites));
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
        return new Site(ns,ambig,siteClass);
    }
    
    public Site recode(Map<String,String> recode, Ambiguous ambig)
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
        return new Site(ns,ambig,siteClass);
    }
    
    public Site limitToTaxa(Collection<String> limit)
    {
        LinkedHashMap<String,String> ns = new LinkedHashMap<>();
        for (Entry<String,String> s: sites.entrySet())
        {
            if (limit.contains(s.getKey()))
            {
                ns.put(s.getKey(), s.getValue());
            }
        }
        return new Site(ns,ambig,siteClass);        
    }
    
    public Ambiguous getAmbiguous()
    {
        return ambig;
    }
    
    private static final long serialVersionUID = 2;
    
    private LinkedHashMap<String,String> sites;
    private Ambiguous ambig;
    private String siteClass;
}
