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

package Alignments;

import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Trees.Tree;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents a "site" in an "alignment".  Both terms used generously.
 * When comparing sites the site ID is ignored so as to allowed counting
 * of unique sites for caclulation purposes.
 * @author Daniel Money
 * @version 2.0
 */
public class Site implements Serializable
{
    /**
     * Creates a site in a alignment with no ambiguous data
     * @param sites Map from taxa name to state
     */
    public Site(LinkedHashMap<String, String> sites)
    {
        this(null,sites,new Ambiguous(), null);
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
        this(null,sites,new Ambiguous(), siteClass);
    }
    
    /**
     * Creates a site in a alignment with ambiguous data
     * @param sites Map from taxa name to state
     * @param ambig Description of ambiguous data
     */
    public Site(LinkedHashMap<String,String> sites, Ambiguous ambig)
    {
        this(null,sites,ambig,null);
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
        this(null, sites, ambig, siteClass);
    }
    
    /**
     * Creates a site in a alignment with no ambiguous data
     * @param id An ID for the site
     * @param sites Map from taxa name to state
     */
    public Site(String id, LinkedHashMap<String, String> sites)
    {
        this(id, sites,new Ambiguous(), null);
    }
    
    /**
     * Creates a site in a alignment with no ambiguous data and with the given
     * class.  <b>Note that either all sites in an alignment must have a class or
     * none should.</b>
     * @param id An ID for the site
     * @param sites Map from taxa name to state
     * @param siteClass The class of this site
     */
    public Site(String id, LinkedHashMap<String,String> sites, String siteClass)
    {
        this(id, sites,new Ambiguous(), siteClass);
    }
    
    /**
     * Creates a site in a alignment with ambiguous data
     * @param id An ID for the site
     * @param sites Map from taxa name to state
     * @param ambig Description of ambiguous data
     */
    public Site(String id, LinkedHashMap<String,String> sites, Ambiguous ambig)
    {
        this(sites,ambig,null);
    }
    
    /**
     * Creates a site in a alignment with ambiguous data and with the given
     * class.  <b>Note that either all sites in an alignment must have a class or
     * none should.</b>
     * @param id An ID for the site
     * @param sites Map from taxa name to state
     * @param ambig Description of ambiguous data
     * @param siteClass The class of this site
     */
    public Site(String id, LinkedHashMap<String,String> sites, Ambiguous ambig, String siteClass)
    {
        this.sites = sites;
        this.ambig = ambig;
        this.siteClass = siteClass;
        this.id = id;
    }
    
    Site(Site s)
    {
        this.sites = s.sites;
        this.ambig = s.ambig;
        this.siteClass = s.siteClass;
        this.id = s.id;
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
     * @throws Alignments.AlignmentException If the passed taxa name is not valid     
     */
    public String getRawCharacter(String taxa) throws AlignmentException
    {
        String rc = sites.get(taxa);
        if (rc == null)
        {
            throw new AlignmentException("No such taxa: " + taxa);            
        }
        else
        {
            return rc;
        }
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
    
    /**
     * Gets the ID of the site
     * @return The ID
     */
    public String getID()
    {
        return id;
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
     * Gets initial node likelihoods based on the site and tree.
     * 
     * Done like this as creating the node likelihoods is time consuming whereas
     * copying them once initialised is not.  As they only need to be initalised
     * once for each site this saves time as they then need only be copied for
     * each likelihood calculation.
     * @param t The tree
     * @param map A map from state to position in array
     * @return An ArrayMap of NodeLikelihoods which can be used to initialise
     * likelihood calculations
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if all states are initialised to a zero likelihood 
     */
    public Map<String, NodeLikelihood> getInitialNodeLikelihoods(Tree t, Map<String,Integer> map) throws LikelihoodException
    {        
         Map<String, NodeLikelihood> nodeLikelihoods = new HashMap<>(t.getNumberBranches() + 1);        
        for (String l: t.getLeaves())
        {
            nodeLikelihoods.put(l, new NodeLikelihood(map, this.getCharacter(l)));
        }

        //And now internal nodes
        for (String i: t.getInternal())
        {
            nodeLikelihoods.put(i, new NodeLikelihood(map));
        }
        return nodeLikelihoods;
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

    /**
     * Recodes the alignment and returns it and also allows the definition of
     * new ambiguous states
     * @param recode A map from original state to new state, e.g. to recode
     * DNA to RY it would contains A -> R, G -> R, C -> Y, T -> Y
     * @param ambig The new ambiiguous states
     * @return A recoded site
     */    
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
    
    /**
     * Returns a new Site which is the same as this one except it is limited
     * to certain taxa
     * @param limit The taxa to limit the new site to
     * @return The limited site
     */    
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
    
    /**
     * Gets the information about ambiguous states for this site
     * @return Information about the ambiguous states
     */
    public Ambiguous getAmbiguous()
    {
        return ambig;
    }
    
    private static final long serialVersionUID = 2;
    
    private LinkedHashMap<String,String> sites;
    private Ambiguous ambig;
    private String siteClass;
    private String id;
}
