package Maths;

import java.io.Serializable;

public class SmallDouble implements Serializable
{
    public SmallDouble(double d)
    {
        e = Math.getExponent(d);
        m = Math.scalb(d, -e);
    }
    
    public SmallDouble(double m, int e)
    {
        int s = Math.getExponent(m);
        this.m = Math.scalb(m, -s);
        this.e = s + e;
    }
    
    public SmallDouble multiply(SmallDouble o)
    {
        return new SmallDouble(m * o.m, e + o.e);
    }
    
    public SmallDouble multiply(double d)
    {
        return new SmallDouble(m * d, e);
    }
    
    public SmallDouble add(SmallDouble o)
    {
        if (e - o.e > Double.MAX_EXPONENT)
        {
            return this;
        }
        if (o.e - e > Double.MAX_EXPONENT)
        {
            return o;
        }
        int diff = e - o.e;
        return new SmallDouble(Math.scalb(m,diff) + o.m, o.e);
    }
    
    public double ln()
    {
        return Math.log(m) + e * Math.log(2);
    }
    
    public double toDouble()
    {
        return Math.scalb(m, e);
    }
    
    public String toString()
    {
        double t = (double) e * Math.log(2) / Math.log(10);
        double se = Math.floor(t);
        double sm = m * Math.pow(10,t - se);
        if (sm < 10.0)
        {
            return (sm + "e" + ((int) se));
        }
        else
        {
            return ((sm/10.0) + "e" + ((int) se + 1));
        }
    }
    
    private double m;
    private int e;
}
