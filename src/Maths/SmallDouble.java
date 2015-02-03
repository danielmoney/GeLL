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

package Maths;

import java.io.Serializable;

/**
 * Represents a real number.  Stores the exponent separately as an Integer so
 * can store much smaller (or indeed larger) reals than the standard double.
 * @author Daniel Money
 * @version 2.0
 */
public class SmallDouble implements Serializable, Real
{
    /**
     * Creates a SmallDouble representing the same number as a double
     * @param d The double to be represented
     */
    public SmallDouble(double d)
    {
        e = Math.getExponent(d);
        m = Math.scalb(d, -e);
    }
    
    /**
     * Creates a SmallDouble representing the same number as m*2^e
     * @param m m
     * @param e e
     */
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
    
    public void addip(Real o)
    {
        //At the moment this is a quick fix that resorts to the old more memory
        //ineffecient way.
        SmallDouble res = this.add(o);
        this.e = res.e;
        this.m = res.m;
    }
    
    public void multiplyip(Real o)
    {
        //At the moment this is a quick fix that resorts to the old more memory
        //ineffecient way.
        SmallDouble res = this.multiply(o);
        this.e = res.e;
        this.m = res.m;
    }
    
    public void multiplyip(double d)
    {
        //At the moment this is a quick fix that resorts to the old more memory
        //ineffecient way.
        SmallDouble res = this.multiply(d);
        this.e = res.e;
        this.m = res.m;
    }
    
    public void addproductip(Real o1, double o2)
    {
        //At the moment this is a quick fix that resorts to the old more memory
        //ineffecient way.
        SmallDouble res = this.add(o1.multiply(o2));
        this.e = res.e;
        this.m = res.m;       
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof SmallDouble)
        {
            SmallDouble d = (SmallDouble) o;
            return ((m == d.m) && (e == d.e));
        }
        if (o instanceof StandardDouble)
        {
            StandardDouble d = (StandardDouble) o;
            return equals(d.toSmallDouble());
        }
        if (o instanceof Double)
        {
            return equals(new SmallDouble((Double) o));
        }
        return false;
    }
    
    public int hashCode()
    {
        return (new Double(m)).hashCode();
    }
    
    private double m;
    private int e;
    
    /**
     * A SmallDouble representing the smallest possible value SmallDouble can represent
     */
    public static final SmallDouble SMALLEST = new SmallDouble(1.0,Integer.MIN_VALUE);
}
