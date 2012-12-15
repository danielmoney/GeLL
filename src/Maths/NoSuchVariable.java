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
 * Exception that is thrown when a function has not been defined
 * @author Daniel Money
 * @version 2.0
 */
public class NoSuchVariable extends Exception
{
    /**
     * Default constructor
     * @param name The name of the function that is undefined
     */
    public NoSuchVariable(String name)
    {
        super("No variable with name \"" + name + "\" was found");
    }
}
