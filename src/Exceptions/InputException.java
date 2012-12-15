/*
 * Copyright 2012 Daniel Money
 * 
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
 * General excpetion for an exeption in or about an input file
 * @author Daniel Money
 * @version 2.0
 */
public class InputException extends GeneralException
{
    /**
     * Default constructor
     * @param file Description of the file (e.g. file name)
     * @param line Line of the file where the problem occured (use N/A or similar
     * if not applicable)
     * @param reason The reason for the exception
     * @param cause The cause of the exception (if applicable - use null if not)
     */
    public InputException(String file, String line, String reason, Throwable cause)
    {
	super("Input error\n\tFile:\t" + file + "\n\tLine:\t" + line +
		"\n\tReason:\t" + reason, cause);
    }
}
