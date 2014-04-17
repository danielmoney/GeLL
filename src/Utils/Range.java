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

package Utils;

/**
 * Used to represent a range (using doubles)
 * @author Daniel Money
 * @version 2.0
 */
public class Range
{
    /**
     * Constructor.  If lower > upper automatically swaps.
     * @param lower The lower value of the range
     * @param upper The upper value of the range
     */
    public Range(double lower, double upper)
    {
	l = Math.min(lower,upper);
	u = Math.max(lower,upper);
    }

    /**
     * Gets the lower value
     * @return The lower value
     */
    public double lower()
    {
	return l;
    }

    /**
     * Gets the upper value
     * @return The upper value
     */
    public double upper()
    {
	return u;
    }

    private double l;
    private double u;
}
