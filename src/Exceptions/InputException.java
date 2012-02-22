package Exceptions;

/**
 * General excpetion for an exeption in or about an input file
 * @author Daniel Money
 * @version 1.0
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
