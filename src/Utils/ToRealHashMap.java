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

package Utils;

import Maths.Real;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A HashMap where the values are Reals.  Allows quicker mathematical operations
 * on the values.
 * @author Daniel Money
 * @version 1.0
 * @param <K> The type of the keys
 */
public class ToRealHashMap<K> extends HashMap<K,Real>
{
    /**
     * Multiply the value associated with a key
     * @param k The key of the value to be changed
     * @param v The amount to multiply the value by
     */
    public void multiply(K k, Real v)
    {
        put(k,get(k).multiply(v));
    }
    
    /**
     * Add to the the value associated with a key
     * @param k The key of the value to be changed
     * @param v The amount to add to the value
     */
    public void add(K k, Real v)
    {
        if (containsKey(k))
        {
            put(k,get(k).add(v));
        }
        else
        {
            put(k,v);
        }        
    }
    
    /**
     * Returns the key which has the maximum value associated with it
     * @return The key with the maximum value
     */
    public K getMaxKey()
    {
        Real mv = null;
        K mk = null;
        for (Entry<K,Real> e: entrySet())
        {
            if ((mv == null) || (e.getValue().greaterThan(mv)))
            {
                mk = e.getKey();
                mv = e.getValue();
            }
        }
            
        return mk;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Entry<K,Real> e: entrySet())
        {
            sb.append(e.getKey());
            sb.append("=>");
            sb.append(e.getValue());
            sb.append(",");
        }
       return sb.substring(0, sb.length()-1);
    }
}
