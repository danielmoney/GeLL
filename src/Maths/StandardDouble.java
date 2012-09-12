package Maths;

import java.io.Serializable;

public class StandardDouble implements Serializable
{
    public StandardDouble(double d)
    {
        this.d = d;
    }
    
   
    public StandardDouble multiply(StandardDouble o)
    {
        return new StandardDouble(d * o.d);
    }
    
    public StandardDouble multiply(double o)
    {
        return new StandardDouble(d * o);
    }
    
    public StandardDouble add(StandardDouble o)
    {
        return new StandardDouble(d + o.d);
    }
    
    public StandardDouble add(double o)
    {
        return new StandardDouble(d + o);
    }
    
    public double ln()
    {
        return Math.log(d);
    }
    
    public double ln1m()
    {
        if (Math.getExponent(d) > -4)
        {
            return Math.log(1.0 - d);
        }
        else
        {
            double d2 = d * d;
            double d3 = d2 * d;
            double d2d = d2 / 2;
            double d3d = d3 / d;
            
            return -d - d2 - d3;
        }
    }
    
    public boolean graterThan(StandardDouble o)
    {
        return d > o.d;
    }
    
    public StandardDouble subtract(StandardDouble o)
    {
        return new StandardDouble(d - o.d);
    }
    
    public StandardDouble subtract(double o)
    {
        return new StandardDouble(d - o);
    }
        
    public StandardDouble negate()
    {
        return new StandardDouble(-d);
    }
    
    public StandardDouble inverse()
    {
        return new StandardDouble(1/d);
    }
    
    public StandardDouble divide(StandardDouble o)
    {
        return new StandardDouble(d / o.d);
    }
    
    public StandardDouble divide(double o)
    {
        return new StandardDouble(d / o);
    }
    
    public double toDouble()
    {
        return d;
    }
    
    public String toString()
    {
        return Double.toString(d);
    }
    
    private double d;
    
    public static final StandardDouble SMALLEST = new StandardDouble(-Double.MAX_VALUE);
}
