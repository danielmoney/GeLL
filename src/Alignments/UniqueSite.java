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

package Alignments;

/**
 * Used to represent a unique site in an alignment.  Augments the normal site
 * class with a count of how often the site occurs.
 * @author Daniel Money
 * @version 2.0
 */
public class UniqueSite extends Site
{
    /**
     * Default constructor
     * @param s The site
     * @param c How often the site occurs
     */
    public UniqueSite(Site s, int c)
    {
        super(s);
        this.c = c;
    }

    /**
     * Get the number of times the site occurs in the related alignment
     * @return The number of times the site occurs
     */
    public int getCount()
    {
        return c;
    }

    public String toString()
    {
        return super.toString() + "\t" + c;
    }

    private int c;
}
