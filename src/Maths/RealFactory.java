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
 * A factory for creating reals
 * @author Daniel Money
 * @version 2.0
 */
public class RealFactory
{
    /**
     * Returns a new real of the given type and value
     * @param type The type of Real to create
     * @param d The value of the real
     * @return A real of the given type and value
     */
    public static Real getReal(RealType type, double d)
    {
        switch (type)
        {
            case SMALL_DOUBLE:
                return new SmallDouble(d);
            default:
                return new StandardDouble(d);
        }
    }
    
    /**
     * Returns the smallest possible real of the given type
     * @param type The type of the real
     * @return The smallest real of the given type
     */
    public static Real getSmallestReal(RealType type)
    {
        switch (type)
        {
            case SMALL_DOUBLE:
                return SmallDouble.SMALLEST;
            default:
                return StandardDouble.SMALLEST;
        }
    }
    
    /**
     * An enumeration of the different Real types
     */
    public enum RealType
    {
        /**
         * {@link StandardDouble}
         */
        STANDARD_DOUBLE,
        /**
         * {@link SmallDouble}
         */
        SMALL_DOUBLE
    }
}
