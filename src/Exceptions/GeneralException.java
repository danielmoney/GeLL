package Exceptions;

/**
 * Custom exception class that provides different levels of information in a call
 * to <code>toString()</code> based on a debug level.
 * @author Daniel Money
 * @version 1.0
 */
public class GeneralException extends Exception
{
    /**
     * Default constructor
     * @param message The reason for the problem
     * @param cause The underlying throwable that caused the problem if applicable or null
     * if not
     */
    public GeneralException(String message, Throwable cause)
    {
	super(message, cause);
    }

    public String toString()
    {
	if (super.getCause() != null)
	{
	    switch (debug)
	    {
		case UNDERLYING_MESSAGE:
		    return getMessage() + "\n\t\t" + super.getCause().getMessage();
		case STACK_TRACE:
		    return getMessage() + "\n\t\t" + super.getCause().getMessage() + "\n" +
			    stackTrace() + "\n";
		default:
		    return getMessage();
	    }
	}
	else
	{
	    if (debug == Debug.STACK_TRACE)
	    {
		return getMessage() + "\n" + stackTrace();
	    }
	    else
	    {
		return getMessage();
	    }
	}
    }

    private String stackTrace()
    {
	StringBuilder message = new StringBuilder();
	StackTraceElement[] ses = super.getStackTrace();
	for (StackTraceElement se: ses)
	{
	    message.append("\t\t\t");
	    message.append(se.toString());
	    message.append("\n");
	}
	return message.toString();
    }

    /**
     * Set debug level
     * @param i The debug level.
     */
    public static void setDebug(Debug i)
    {
	debug = i;
    }
    
    /**
     * Enumeration of the different debug levels
     */
    public enum Debug
    {

        /**
         * Just output the message assocaited with the error
         */
        MESSAGE,
        /**
         * Output the error associated with both the error and any underlying
         * error
         */
        UNDERLYING_MESSAGE,
        /**
         * Output the messages and the stack trace
         */
        STACK_TRACE
    }

    private static Debug debug = Debug.MESSAGE;
}
