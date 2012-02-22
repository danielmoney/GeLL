package Exceptions;

/**
 * Exception for problems writing an output file
 * @author Daniel Money
 * @version 1.0
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
