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
 * Exception thrown when there is a problem with the Settings machinery
 * @author Daniel Money
 * @version 2.0
 */
public class SettingException extends Exception
{
    /**
     * Default constructor
     * @param msg Description of the problem
     */
    public SettingException(String msg)
    {
	super(msg);
    }
}
