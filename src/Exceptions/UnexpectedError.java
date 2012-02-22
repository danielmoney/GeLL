package Exceptions;

/**
 * Represnts an error that should never occur.  Used when someone else's code
 * (normally Java) throws an error but you know the condition that caused the
 * error should never occur.
 * @author Daniel Money
 * @version 1.0
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
}
