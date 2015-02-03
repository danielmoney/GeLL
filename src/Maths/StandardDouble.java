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
 * Represents a real.  Uses the standard java Double class
 * @author Daniel Money
 * @version 2.0
 */
public class StandardDouble implements Serializable, Real
{
    /**
     * Creates a StandardDouble representing the same number as a double
     * @param d The double to be represented
     */
    public StandardDouble(double d)
    {
        this.d = d;
    }
    
   
    public StandardDouble multiply(Real o)
    {
        return new StandardDouble(d * o.toDouble());
    }
    
    public StandardDouble multiply(double o)
    {
        return new StandardDouble(d * o);
    }
    
    public StandardDouble add(Real o)
    {
        return new StandardDouble(d + o.toDouble());
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
            double d3d = d3 / 3;
            
            return -d - d2d - d3d;
        }
    }
    
    public boolean greaterThan(Real o)
    {
        return d > o.toDouble();
    }
    
    public boolean greaterThan(double o)
    {
        return d > o;
    }
    
    public StandardDouble subtract(Real o)
    {
        return new StandardDouble(d - o.toDouble());
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
    
    public StandardDouble divide(Real o)
    {
        return new StandardDouble(d / o.toDouble());
    }
    
    public StandardDouble divide(double o)
    {
        return new StandardDouble(d / o);
    }
    
    public double toDouble()
    {
        return d;
    }
    
    public SmallDouble toSmallDouble()
    {
        return new SmallDouble(d);
    }
    
    public void addip(Real o)
    {
        d += o.toDouble();
    }
    
    public void multiplyip(Real o)
    {
        d = d * o.toDouble();
    }
    
    public void multiplyip(double o)
    {
        d = d * o;
    }
    
    public void addproductip(Real o1, double o2)
    {
        d += (o1.toDouble() * o2);
    }
    
    public String toString()
    {
        return Double.toString(d);
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof StandardDouble)
        {
            StandardDouble sd = (StandardDouble) o;
            return sd.d == d;
        }
        if (o instanceof Double)
        {
            Double dd = (Double) o;
            return dd == d;
        }
        if (o instanceof SmallDouble)
        {
            return o.equals(this);
        }
        return false;
    }
    
    public int hashCode()
    {
        return (new Double(d)).hashCode();
    }
    
    private double d;
    
    /**
     * A StandardDouble representing the smallest possible value StandardDouble can represent
     */
    public static final StandardDouble SMALLEST = new StandardDouble(-Double.MAX_VALUE);
}
