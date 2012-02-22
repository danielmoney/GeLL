package Utils;

/**
 * Exception thrown when there is a problem with the Settings machinery
 * @author Daniel Money
 * @version 1.0
 */
public class SettingException extends Exception
{
    /**
     * Deafult constructor
     * @param msg Description of the problem
     */
    public SettingException(String msg)
    {
	super(msg);
    }
}
