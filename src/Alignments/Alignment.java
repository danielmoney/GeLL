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

import Exceptions.UnexpectedError;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Represents an "alignment", used very loosely.  Classes should extend this to
 * represent the different types of alignment, e.g. {@link DuplicationAlignment},
 * {@link FastaAlignment}.
 * 
 * @author Daniel Money
 * @version 2.0
 */
public class Alignment implements Iterable<Site>
{
    /**
     * Default constructor.  Called by classed that extend this one.
     * Classes that do extend this class should ensure that they populate data
     * and taxa appropriately.
     */
    private Alignment()
    {
        this.classSizes = null;
    }
    
    /**
     * Creates an alignment
     * @param data  A list of sites in the alignment
     * @throws Alignments.AlignmentException Thrown if the sites passed have different taxa 
     */
    public Alignment(List<Site> data) throws AlignmentException
    {
	this.data = data;
        this.taxa = data.get(0).getTaxa();
        this.hasClasses = data.get(0).getSiteClass() != null;
        for (Site s: data)
        {
            if (!s.getTaxa().equals(taxa))
            {
                throw new AlignmentException("Sites have different taxa");
            }
            if (hasClasses != (s.getSiteClass() != null))
            {
                throw new AlignmentException("Some sites have a class, some don't");
            }
        }
        this.classSizes = null;
    }

    /**
     * Gets the length of the aligmnet
     * @return The length of the alignment
     */
    public int getLength()
    {
	return data.size();
    }
    
    /**
     * Returns the average length of the sequences
     * @param gaps The set of gap characters (i.e. characters to be ignored when
     * coutning length)
     * @return The average sequence length
     */
    public double averageLength(Set<String> gaps)
    {        
        int i = 0;
        try
        {
            for (Site s: data)
            {
                for (String t: s.getTaxa())
                {
                    if (!gaps.contains(s.getRawCharacter(t)))
                    {
                        i++;
                    }
                }
            }
        }
        catch (AlignmentException ex)
        {
            throw new UnexpectedError(ex);
        }
        return (double) i / (double) taxa.size();
    }

    /**
     * Gets the number of taxa in the alignment
     * @return The number of taxa in the alignment
     */
    public int getNumber()
    {
        return taxa.size();
    }
    
    /**
     * Returns a list of unique sites in the aligment.  Useful for calculating
     * likelihoods as each site pattern only has to be calculated once.
     * @return A set of unique sites
     */
    public List<UniqueSite> getUniqueSites()
    {
        if (us == null)
        {
            Map<Site,Integer> counts = new HashMap<>();
            for (Site s: data)
            {
                if (counts.containsKey(s))
                {
                    counts.put(s,counts.get(s) + 1);
                }
                else
                {
                    counts.put(s,1);
                }
            }
            us = new ArrayList<>();
            for (Entry<Site,Integer> e: counts.entrySet())
            {
                us.add(new UniqueSite(e.getKey(), e.getValue()));
            }
        }
        return us;
    }
    

    /**
     * Returns the site at a given position in the alignment
     * @param s The site to be returned
     * @return The site at posiiton s
     */
    public Site getSite(int s)
    {
        return data.get(s);
    }
    
    /**
     * Gets the names of the taxa in the aligment
     * @return List of taxa names
     */
    public Set<String> getTaxa()
    {
        return taxa;
    }
    
    public Iterator<Site> iterator()
    {
        return data.iterator();
    }
    
    /**
     * Checks whether the map contains an entry for every class in the alignment
     * @param map The map
     * @return Whether the map has an entry for every class
     */
    public boolean check(Map<String,?> map)
    {
        Set<String> aClass = new HashSet<>();
        for (Site s: data)
        {
            aClass.add(s.getSiteClass());
        }
        
        for (String c: aClass)
        {
            if (!map.containsKey(c))
            {
                return false;
            }
        }
        return true;
    }
       
    /**
     * Recodes the alignment and returns it
     * @param recode A map from original state to new state, e.g. to recode
     * DNA to RY it would contains A -> R, G -> R, C -> Y, T -> Y
     * @return A recoded alignment
     */
    public Alignment recode(Map<String, String> recode)
    {
        Alignment na = new Alignment();
        na.data = new ArrayList<>();
        na.taxa = taxa;
        for (Site os: data)
        {
            na.data.add(os.recode(recode));
        }
        return na;
    }
    
    /**
     * Recodes the alignment and returns it and also allows the definition of
     * new ambiguous states
     * @param recode A map from original state to new state, e.g. to recode
     * DNA to RY it would contains A -> R, G -> R, C -> Y, T -> Y
     * @param ambig The new ambiiguous states
     * @return A recoded alignment
     */
    public Alignment recode(Map<String, String> recode, Ambiguous ambig)
    {
        Alignment na = new Alignment();
        na.data = new ArrayList<>();
        na.taxa = taxa;
        for (Site os: data)
        {
            na.data.add(os.recode(recode,ambig));
        }
        return na;
    }
    
    /**
     * Returns a new Alignment which is the same as this one except it is limited
     * to certain taxa
     * @param limit The taxa to limit the new alignment to
     * @return The limited alignment
     */
    public Alignment limitToTaxa(Collection<String> limit)
    {
        Alignment na = new Alignment();
        na.data = new ArrayList<>();
        na.taxa.addAll(limit);
        for (Site os: data)
        {
            na.data.add(os.limitToTaxa(limit));
        }
        return na;      
    }
    
    /**
     * Gets the frequency of a raw character
     * @param character The character to get the frequency for
     * @return The frequency of that chaacter
     */
    public double getRawFreq(String character)
    {
        int c = 0;
        for (Site s: data)
        {
            for (String t: taxa)
            {
                try
                {
                    if (s.getRawCharacter(t).equals(character))
                    {
                        c++;
                    }
                }
                catch (AlignmentException e)
                {
                    //Should never reach here as we're looping over the known taxa hence...
                    throw new UnexpectedError(e);
                }
            }
        }
        return (double) c / (double) (data.size() * taxa.size());
    }
    
    /**
     * Gets the size of a class, that is how many sites belong to the given class
     * @param cl The class
     * @return How many sites are in that class
     */
    public int getClassSize(String cl)
    {
        if (classSizes == null)
        {
            classSizes = new HashMap<>();
            for (Site s: data)
            {
                String sc = s.getSiteClass();
                if (classSizes.containsKey(sc))
                {
                    classSizes.put(sc,classSizes.get(sc) + 1);
                }
                else
                {
                    classSizes.put(sc, 1);
                }
            }
        }
        if (classSizes.containsKey(cl))
        {
            return classSizes.get(cl);
        }
        else
        {
            return 0;
        }
    }
    
    public List<String> getSiteClasses()
    {
        List<String> classes = new ArrayList<>(data.size());
        for (Site s: data)
        {
            classes.add(s.getSiteClass());
        }
        return classes;
    }
    
    /**
     * Checks whether the sites in this alignment have classes
     * @return Whether the sites have clases
     */
    public boolean hasClasses()
    {
        return hasClasses;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Alignment))
        {
            return false;
        }
        Alignment a = (Alignment) o;
        if (a.getLength() != getLength())
        {
            return false;
        }
        for (int i = 0; i <  getLength(); i++)
        {
            if (!getSite(i).equals(a.getSite(i)))
            {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode()
    {
        if (getLength() > 0)
        {
            return getSite(0).hashCode();
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * The list of sites in the alignment
     */
    private List<Site> data = new ArrayList<>();
    
    /**
     * The list of taxa in the alignment
     */
    private Set<String> taxa = new HashSet<>();
    
    /**
     * Whether the sites in this alignment contain class information
     */
    private boolean hasClasses = false;
    
    private List<UniqueSite> us;
    
    private Map<String,Integer> classSizes;
    
}
