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

package Exceptions;

/**
 * Represents an error that should never occur.  Used when someone else's code
 * (normally Java) throws an error but you know the condition that caused the
 * error should never occur.
 * @author Daniel Money
 * @version 2.0
 */
public class UnexpectedError extends Error
{
    /**
     * Default constructor
     * @param cause The Throwable that caused the problem
     */
    public UnexpectedError(Throwable cause)
    {
	super("Private Error\nThis condition wasn't expected to be reached.", cause);
    }
    
    /**
     * Constructor to use when no other throwable caused the problem
     */
    public UnexpectedError()
    {
        super("Private Error\nThis condition wasn't expected to be reached.");
    }
}
