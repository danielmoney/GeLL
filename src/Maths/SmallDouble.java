package Maths;

import java.io.Serializable;

public class SmallDouble implements Serializable, Real//<SmallDouble>
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
    
    public SmallDouble multiply(Real r)
    {
        SmallDouble o = r.toSmallDouble();
        return new SmallDouble(m * o.m, e + o.e);
    }
    
    public SmallDouble multiply(double d)
    {
        return new SmallDouble(m * d, e);
    }
    
    public SmallDouble add(Real r)
    {
        SmallDouble o = r.toSmallDouble();
        if (m == 0.0)
        {
            return o;
        }
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
    
    public SmallDouble add(double d)
    {
        return add(new SmallDouble(d));
    }
    
    public double ln()
    {
        return Math.log(m) + e * Math.log(2);
    }
    
    public double ln1m()
    {
        if (e > -4)
        {
            return Math.log(1.0 - toDouble());
        }
        else
        {
            SmallDouble x2 = multiply(this);
            SmallDouble x3 = x2.multiply(this);
            SmallDouble x2d = x2.divide(2);
            SmallDouble x3d = x3.divide(3);
            
            return negate().subtract(x2d).subtract(x3d).toDouble();
        }
    }
    
    public boolean greaterThan(Real r)
    {
        SmallDouble o = r.toSmallDouble();
        if (e > o.e)
        {
            return true;
        }
        if (e < o.e)
        {
            return false;
        }
        return (m > o.m);
    }
    
    public boolean greaterThan(double r)
    {
        return greaterThan(new SmallDouble(r));
    }
    
    public SmallDouble subtract(Real o)
    {
        return add(o.negate());
    }
    
    public SmallDouble subtract(double d)
    {
        return add(new SmallDouble(-d));
    }
        
    public SmallDouble negate()
    {
        return new SmallDouble(-m,e);
    }
    
    public SmallDouble inverse()
    {
        return new SmallDouble(1/m,-e);
    }
    
    public SmallDouble divide(Real o)
    {
        return multiply(o.inverse());
    }
    
    public SmallDouble divide(double d)
    {
        return new SmallDouble(m / d, e);
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
    
    public SmallDouble toSmallDouble()
    {
        return this;
    }
    
    private double m;
    private int e;
    
    public static final SmallDouble SMALLEST = new SmallDouble(1.0,Integer.MIN_VALUE);
}
