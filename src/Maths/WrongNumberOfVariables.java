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
 * Exception that is thrown if a function is passed the wrong number of variables
 * @author Daniel Money
 * @version 1.0
 */
public class WrongNumberOfVariables extends Exception
{
    /**
     * Default constrcutor
     * @param name The name of the function
     * @param wanted The number of variables the function takes
     * @param found The number of variables that were actually passed
     */
    public WrongNumberOfVariables(String name, int wanted, int found)
    {
        super("Function: " + name + ", Wanted: " + wanted + ", Found: " + found);
    }
}
