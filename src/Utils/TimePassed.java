package Utils;

import java.util.concurrent.TimeUnit;

/**
 * A class to check wether a set amount of time has passed.
 * @author Daniel Money
 * @version 1.0
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
