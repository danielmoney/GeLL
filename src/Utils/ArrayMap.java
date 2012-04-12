package Utils;

import java.lang.reflect.Array;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayMap<K,V>
{
    //public ArrayMap()
    //public ArrayMap(int size)
    @SuppressWarnings("unchecked")
    public ArrayMap(Class<K> k, Class<V> v, int size)
    {
        //list = new ArrayList<>();
        //list = new Object[size];
        //keys = new Object[size];
        //values = new Object[size];
        keys = (K[]) Array.newInstance(k, size);
        values = (V[]) Array.newInstance(v, size);
        next = 0;
    }
    
    public void put(K k, V v)
    {
        //list.add(new SimpleEntry<>(k,v));
        //list[next] = new SimpleEntry<>(k,v);
        keys[next] = k;
        values[next] = v;
        next++;
    }
    
    public void put(int i, K k, V v)
    {
        //list[i] = new SimpleEntry<>(k,v);
        keys[i] = k;
        values[i] = v;
    }
    
    public ArrayList<K> keyList()
    {
        ArrayList<K> keylist = new ArrayList<>();
        //for (int i = 0; i < list.length; i++)
        for (int i = 0; i < keys.length; i++)
        {
            //keylist.add(getEntry(i).getKey());
            keylist.add((K) keys[i]);
        }
        return keylist;
    }
    
    public SimpleEntry<K,V> getEntry(int i)
    {
        K k = (K) keys[i];
        V v = (V) values[i];
        return new SimpleEntry<>(k,v);
        //return (SimpleEntry<K,V>) list[i];
    }
    
    public int size()
    {
        //return list.length;
        return keys.length;
    }
    
    public boolean containsKey(K k)
    {
        /*for (SimpleEntry<K,V> e: list)
        {
            if (e.getKey().equals(k))
            {
                return true;
            }
        }
        return false;*/
        //for (int i = 0; i < list.length; i++)
        for (int i = 0; i < keys.length; i++)
        {
            //SimpleEntry<K,V> e = (SimpleEntry<K,V>) list[i];
            //if (e.getKey().equals(k))
            if (keys[i].equals(k))
            {
                return true;
            }
        }
        return false;
    }
    
    public V get(K k)
    {
        //for (int i = 0; i < list.length; i++)
        for (int i = 0; i < keys.length; i++)
        {
            //SimpleEntry<K,V> e = (SimpleEntry<K,V>) list[i];
            //K ek = e.getKey();
            //if (ek == null)
            if (keys[i] == null)
            {
                if (k == null)
                {
                    //return e.getValue();
                    return (V) values[i];
                }
            }
            else
            {
                //if (ek.equals(k))
                if (keys[i].equals(k))
                {
                    //return e.getValue();
                    return (V) values[i];
                }
            }
        }
        return null;
        /*for (SimpleEntry<K,V> e: list)
        {
            if (e.getKey().equals(k))
            {
                return e.getValue();
            }
        }
        return null;*/
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof ArrayMap)
        {
            ArrayMap am = (ArrayMap) o;
            //THIS WILL NEED A FIX ADS IT REQUIRES THE SAME ORDER
            //return Arrays.deepEquals(list, am.list);
            return (Arrays.deepEquals(values, am.values) && Arrays.deepEquals(keys, am.keys));
        }
        else
        {
            return false;
        }
    }
    
    //private ArrayList<SimpleEntry<K,V>> list;
    //private Object[] list;
    
    //private Object[] keys;
    //private Object[] values;
    
    private K[] keys;
    private V[] values;
    
    private int next;
}
