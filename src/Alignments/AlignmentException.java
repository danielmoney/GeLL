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

import Exceptions.GeneralException;

/**
 * Exception related to an alignment (currently only input errors)
 * @author Daniel Money
 * @version 1.0
 */
public class AlignmentException extends GeneralException
{
    /**
     * Constructor for when there is a problem unrelated to an input file
     * @param msg The cause of the problem
     */
    public AlignmentException(String msg)
    {
        super(msg,null);
    }
    
    /**
     * Constructor for when there is a problem in the input file
     * @param location Information on the location in the file
     * @param text The text that caused the problem
     * @param reason Why the text was problematic
     * @param cause The Throwable that caused the problem (if applicable)
     */
    public AlignmentException(String location, String text, String reason, Throwable cause)
    {
	super("Alignment Error\n\tLocation:\t" + location + "\n\tText:\t" + text +
		"\n\tReason:\t" + reason, cause);
    }

}
