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

/**
 * Represents a Real number.  This allows use of classes with additional capabilities
 * beyond Double.  Specifically although Double is fast it may not be able to store
 * small enough numbers for some likelihood calculations, hence {@link SmallDouble}.
 * @author Daniel Money
 * @version 2.0
 */
public interface Real
{
    /**
     * Multiplies this Real by another
     * @param o The other Real
     * @return A new Real storing the result
     */
    public Real multiply(Real o);
    /**
     * Multiplies this Real by a double
     * @param o The double
     * @return A new Real storing the result
     */
    public Real multiply(double o);
    /**
     * Adds this Real to another
     * @param o The other Real
     * @return A new Real storing the result
     */
    public Real add(Real o);
    /**
     * Adds this Real to a double
     * @param o The double
     * @return A new Real storing the result
     */
    public Real add(double o);
    /**
     * Returns the natural logarithm of this Real
     * @return The natural logarithm.
     */
    public double ln();   
    /**
     * Returns the natural logithm of one minus the value of this Real
     * @return The natural logithm of one minus this Real
     */
    public double ln1m();
    /**
     * Compares this real to another
     * @param o The other real
     * @return True if this real is larger than the other, else false
     */
    public boolean greaterThan(Real o);
    /**
     * Compares this real to a double
     * @param o The double
     * @return True if this double is larger than the other, else false
     */
    public boolean greaterThan(double o);
    /**
     * Subtracts another Real from this one
     * @param o The other Real
     * @return A new Real storing the result
     */
    public Real subtract(Real o);
    /**
     * Subtracts a double from this Real
     * @param o The double
     * @return A new Real storing the result
     */
    public Real subtract(double o);
    /**
     * Negates this real.
     * @return A real representing minus the value of this Real
     */
    public Real negate();
    /**
     * Returns the inverse of this real (i.e. one divided by the value of this Real)
     * @return A Real representing the inverse of thie Real
     */
    public Real inverse();
    /**
     * Divides this Real by another one
     * @param o The other Real
     * @return A new Real storing the result
     */
    public Real divide(Real o);
    /**
     * Divides this Real by a double
     * @param o The double
     * @return A new Real storing the result
     */
    public Real divide(double o);
    /**
     * Returns this real as a double.  May return infinites and similar if
     * conversion is not possible.
     * @return This Real as a double
     */
    public double toDouble();    
    /**
     * Returns this real as a SmallDouble
     * @return This Real as a SmallDouble
     */
    public SmallDouble toSmallDouble();    
}
