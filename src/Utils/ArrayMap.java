package Utils;

import java.lang.reflect.Array;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A crude map of a fixed size that is backed by an array.  Created to be memory
 * effecient and allow quick updating of values (when their index is known) but
 * still allow more user friendly access by key.  
 * 
 * Error checks are deliberately not done on put() and getEntry() for effeciency
 * reasons.  Calling code should ensure the map is big enough and that the index
 * is valid else an ArrayIndexOutOfBoundsException will be thrown
 * 
 * @author Daniel Money
 * @version 1.2
 * @param <K> The key type
 * @param <V> The value type
 */
public class ArrayMap<K,V>
{
    /**
     * Default Constructor.  Passing the classes like this is the easiest way
     * to deal with the java problems that come with using generics and arrays
     * in Java
     * @param k The class of the keys
     * @param v The class of the values
     * @param size The size of the map
     */
    @SuppressWarnings("unchecked")
    public ArrayMap(Class<K> k, Class<V> v, int size)
    {
        keys = (K[]) Array.newInstance(k, size);
        values = (V[]) Array.newInstance(v, size);
        next = 0;
    }
    
    /**
     * Puts a new key/value pair into the map
     * 
     * Error checks are deliberately not done for effeciency reasons.  Calling 
     * code should ensure the map is big enough else an 
     * ArrayIndexOutOfBoundsException will be thrown
     * @param k The key
     * @param v The value
     */
    public void put(K k, V v)
    {
        keys[next] = k;
        values[next] = v;
        next++;
    }
    
    /**
     * Puts a new key/value pair into the map at a given location
     * 
     * Error checks are deliberately not done for effeciency
     * reasons.  Calling code should ensure that the index
     * is valid else an ArrayIndexOutOfBoundsException will be thrown
     * @param i The location to add the key / value pair
     * @param k The key
     * @param v The value
     */
    public void put(int i, K k, V v)
    {
        keys[i] = k;
        values[i] = v;
    }
    
    /**
     * Gets a list of keys in the map
     * @return A list of keys
     */
    public ArrayList<K> keyList()
    {
        ArrayList<K> keylist = new ArrayList<>();
        for (int i = 0; i < keys.length; i++)
        {
            keylist.add((K) keys[i]);
        }
        return keylist;
    }
    
    /**
     * Gets the key/value entry at a given location
     * 
     * Error checks are deliberately not done for effeciency
     * reasons.  Calling code should ensure that the index
     * is valid else an ArrayIndexOutOfBoundsException will be thrown
     * @param i The location to return the entry for
     * @return The entry at that location
     */
    public SimpleEntry<K,V> getEntry(int i)
    {
        K k = (K) keys[i];
        V v = (V) values[i];
        return new SimpleEntry<>(k,v);
    }
    
    /**
     * Returns the size of the map.  This is how many key/value pairs the map
     * can hold, not how many it is holding
     * @return The size of the map
     */
    public int size()
    {
        return keys.length;
    }
    
    /**
     * Tests whether the map contains a key
     * @param k The key to test for
     * @return Whether the map contains the key
     */
    public boolean containsKey(K k)
    {
        for (int i = 0; i < keys.length; i++)
        {
            if (keys[i].equals(k))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the value for a given key
     * @param k The key
     * @return The value associated with that key
     */
    public V get(K k)
    {
        for (int i = 0; i < keys.length; i++)
        {
            if (keys[i] == null)
            {
                if (k == null)
                {
                    return (V) values[i];
                }
            }
            else
            {
                if (keys[i].equals(k))
                {
                    return (V) values[i];
                }
            }
        }
        return null;
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof ArrayMap)
        {
            ArrayMap am = (ArrayMap) o;
            return (Arrays.deepEquals(values, am.values) && Arrays.deepEquals(keys, am.keys));
        }
        else
        {
            return false;
        }
    }
    
    private K[] keys;
    private V[] values;
    
    private int next;
}
