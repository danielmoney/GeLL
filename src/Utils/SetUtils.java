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

import java.util.Set;

/**
 * Utility class for sets
 * @author Daniel Money
 * @version 2.0
 */
public class SetUtils
{
    private SetUtils()
    {
        // This class has all static methods so no need for a constructor.
        // As that's not possible make the only constructor private so it can't
        // be called.        
    }
    
    /**
     * Gets the single element of a set if it has only one element.  Throws an
     * exception if there is more than one element.  Returns null if there are no
     * elements
     * @param <T> The type of the elements of the set
     * @param set The set
     * @return The single element in the set
     * @throws Utils.SetUtils.SetHasMultipleElementsException If the set has multiple
     * elements in it
     */
    public static <T> T getSingleElement(Set<T> set) throws SetHasMultipleElementsException
    {
        if (set.size() > 1)
        {
            throw new SetHasMultipleElementsException();
        }
        for (T e: set)
        {
            return e;
        }
        return null;
    }
    
    /**
     * Exception thrown if an attempt is made to get the single element from
     * a set that has multiple elements.
     */
    public static class SetHasMultipleElementsException extends Throwable
    {
        private SetHasMultipleElementsException()
        {
            super("Set has more than one element");
        }
    }
}
