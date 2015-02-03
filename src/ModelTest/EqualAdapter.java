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

package ModelTest;

import Alignments.Alignment;
import Alignments.Site;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The equal adapter.  See Whelan et al 2015 for more
 * @author Daniel Money
 * @version 2.0
 */
public class EqualAdapter implements Adapter
{
   
    /**
     * Creates an instance
     * @param mapping Map from compound state to distinct state
     */
    public EqualAdapter(Map<String,String> mapping)
    {
        this.mapping = mapping;
        
        ctot = new HashMap<>();
        for (Entry<String,String> e: mapping.entrySet())
        {
            String mapped = e.getValue();
            if (!ctot.containsKey(mapped))
            {
                ctot.put(mapped,0);
            }
            ctot.put(mapped,ctot.get(mapped) + 1);
        }
    }
    
    public double likelihood(Alignment distinct)
    {        
        double l = 0.0;
        for (Site s: distinct)
        {
            double sp = 1.0;
            for (String t: distinct.getTaxa())
            {
                Set<String> mappedTo = new HashSet<>();
                double top = 0.0;
                for (String c: s.getCharacter(t))
                {
                    top ++;
                    mappedTo.add(mapping.get(c));
                }
                
                double bot = 0.0;
                
                for (String c: mappedTo)
                {
                    bot += ctot.get(c);
                }
                
                sp = sp * (top / bot);
            }
            l += Math.log(sp);
        }
        return l;
    }
    
    public int numberParameters()
    {
        return mapping.size() - 1;
    }

    private Map<String,String> mapping;
    private Map<String,Integer> ctot;
}
