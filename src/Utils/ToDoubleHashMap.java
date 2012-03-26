package Utils;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * A HashMap where the values are doubles.  Allows quicker mathematical operations
 * on the values.
 * @author Daniel Money
 * @version 1.0
 * @param <K> The type of the keys
 */
public class ToDoubleHashMap<K> extends HashMap<K,Double>
{
    /**
     * Multiply the value associated with a key
     * @param k The key of the value to be changed
     * @param v The amount to multiply the value by
     */
    public void multiply(K k, double v)
    {
        put(k,get(k) * v);
    }
    
    /**
     * Add to the the value associated with a key
     * @param k The key of the value to be changed
     * @param v The amount to add to the value
     */
    public void add(K k, double v)
    {
        put(k,get(k) + v);
    }
    
    /**
     * Returns the key which has the maximum value associated with it
     * @return The key with the maximum value
     */
    public K getMaxKey()
    {
        double mv = -Double.MAX_VALUE;
        K mk = null;
        for (Entry<K,Double> e: entrySet())
        {
            if (e.getValue() > mv)
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
        for (Entry<K,Double> e: entrySet())
        {
            sb.append(e.getKey());
            sb.append("=>");
            sb.append(e.getValue());
            sb.append(",");
        }
       return sb.substring(0, sb.length()-1);
    }
}
