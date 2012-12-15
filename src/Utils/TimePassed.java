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

import java.util.concurrent.TimeUnit;

/**
 * A class to check wether a set amount of time has passed.
 * @author Daniel Money
 * @version 2.0
 */
public class TimePassed
{
    /**
     * Defcault constructor.  Sets how much time should pass.
     * @param num The number of time units that should pass between checkpoint
     * writes.
     * @param unit The time unit
     */
    public TimePassed(int num, TimeUnit unit)
    {
        diff = unit.toMillis(num);
        oldTime = System.currentTimeMillis();
    }
    
    /**
     * Check to see if the required amount of time has passed.  If it has reset
     * the timer and return true.
     * @return Whether the required amount of time has passed.
     */
    public boolean hasPassed()
    {
        long newTime = System.currentTimeMillis();
        if ((newTime - oldTime) > diff)
        {
            oldTime = newTime;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Reset the timer.
     */
    public void reset()
    {
        oldTime = System.currentTimeMillis();
    }
    
    private long diff;
    private long oldTime;
}
