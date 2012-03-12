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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for maps
 * @author Daniel Money
 * @version 1.0
 */
public class MapUtils
{
    private MapUtils()
    {
        // This class has all static methods so no need for a constructor.
        // As that's not possible make the only constructor private so it can't
        // be called.
    }    
    
    /**
     * Returns the key set sorted by the associated value
     * @param <K> Type of the map's keys
     * @param <V> Tyoe of the map's values
     * @param map The map
     * @return A list of the map's key sorted by associated value
     */
    public static <K,V extends Comparable<V>> List<K> keySetSortedByValue(Map<K, V> map)
    {
	return sortedList(map, false);
    }
    
    /**
     * Returns the key sey sorted by the associated value in reverse order
     * @param <K> Type of the map's keys
     * @param <V> Type of the map's values
     * @param map The map
     * @return A list of the map's key sorted by asscoiated value in reverse order
     */
    public static <K,V extends Comparable<V>> ArrayList<K> keySetSortedByValueReverse(Map<K, V> map)
    {
	return sortedList(map, true);
    }
    
    /**
     * Reverse lookup in the map.  Returns the first key with the associated value.
     * Returns null if the value is not in the map
     * @param <K> Type of the map's key
     * @param <V> Type of the map's avlues
     * @param map The map
     * @param value The value to return a key for
     * @return The key
     */
    public static <K,V> K reverseLookup(Map<K,V> map, V value)
    {
	for (K key : map.keySet())
	{
	    if (map.get(key).equals(value))
	    {
		return key;
	    }
	}
	return null;
    }
    
    /**
     * Reverse lookup in the map.  Returns all keys with the associated value.
     * Returns an empty set if the value is not in the map.
     * @param <K> The type of the map's keys
     * @param <V> The type of the map's values
     * @param map The map
     * @param value
     * @return The value to return the keys for
     */
    public static <K,V> Set<K> reverseLookupMulti(Map<K,V> map, V value)
    {
	HashSet<K> keys = new HashSet<>();
	for (K key : map.keySet())
	{
	    if (map.get(key).equals(value))
	    {
		keys.add(key);
	    }
	}
	return keys;
    }
    
    private static <K,V extends Comparable<V>> ArrayList<K> sortedList(Map<K, V> map, boolean reverse)
    {
	Comp<K,V> c = new Comp<>(map, reverse);
	ArrayList<K> l = new ArrayList<>(map.keySet());
	Collections.sort(l,c);
	return l;	
    }
    
    private static class Comp<K,V extends Comparable<V>> implements Comparator<K>
    {
	public Comp (Map<K,V> map, boolean reverse)
	{
	    this.map = map;
	    this.reverse = reverse;
	}
	
	public int compare(K o1, K o2)
	{
            int comp = map.get(o1).compareTo(map.get(o2));
	    if (!reverse)
	    {
		return comp;
	    }
	    else
	    {
		return -comp;
	    }		
	}
	
	private Map<K,V> map;
	private boolean reverse;
    }
}
