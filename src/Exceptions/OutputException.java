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
 * Exception for problems writing an output file
 * @author Daniel Money
 * @version 2.0
 */
public class OutputException extends GeneralException
{
    /**
     * Default constructor
     * @param file String representing the file that there is a problem with
     * @param reason The reason for the problem
     * @param cause The throwable that caused the problem (if applicable)
     */
    public OutputException(String file, String reason, Throwable cause)
    {
	super("Output error\n\tFile:\t" + file +
		"\n\tReason:\t" + reason, cause);
    }
}
