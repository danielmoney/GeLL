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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Represents an "alignment", used very losely.  Classes should extend this to
 * represent the different types of alignment, e.g. {@link DuplicationAlignment},
 * {@link SequenceAlignment}.
 * 
 * @author Daniel Money
 * @version 1.0
 */
public class Alignment implements Iterable<Site>
{
    /**
     * Default constructor.  Called by classed that extend this one.
     * Classes that do extend this class should ensure that they populate data
     * and taxa appropiately.
     */
    protected Alignment()
    {
    }
    
    /**
     * Creates an alignment
     * @param data  A list of sites in the alignment
     * @throws AlignmentException Thrown if the sites passed have different taxa 
     */
    public Alignment(List<Site> data) throws AlignmentException
    {
	this.data = data;
        this.taxa = data.get(0).getTaxa();
        for (Site s: data)
        {
            if (!s.getTaxa().equals(taxa))
            {
                throw new AlignmentException("Sites have different taxa");
            }
        }
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
    public Set<Site> getUniqueSites()
    {
        return new HashSet<>(data);
    }
    
    /**
     * Gets a count of hoften a site occurs in the alignment
     * @param s The site 
     * @return How often it occurs
     */
    //This is probably slightly ineffecient but will be quite minor in the 
    //ground scheme of things.
    public int getCount(Site s)
    {
        int c = 0;
        for (Site ps: data)
        {
            if (ps.equals(s))
            {
                c++;
            }
        }
        return c;
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
     * The list of sites in the alignment
     */
    protected List<Site> data = new ArrayList<>();
    
    /**
     * The list of taxa in the alignment
     */
    protected Set<String> taxa = new HashSet<>();
}
