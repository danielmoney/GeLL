package Alignments;

import Exceptions.GeneralException;

/**
 * Exception related to an alignment (currently only input errors)
 * @author Daniel Money
 * @version 1.0
 */
public class AlignmentException extends GeneralException
{
    /**
     * Constructor for when there is a problem unrelated to an input file
     * @param msg The cause of the problem
     */
    public AlignmentException(String msg)
    {
        super(msg,null);
    }
    
    /**
     * Constructor for when there is a problem in the input file
     * @param location Information on the location in the file
     * @param text The text that caused the problem
     * @param reason Why the text was problematic
     * @param cause The Throwable that caused the problem (if applicable)
     */
    public AlignmentException(String location, String text, String reason, Throwable cause)
    {
	super("Alignment Error\n\tLocation:\t" + location + "\n\tText:\t" + text +
		"\n\tReason:\t" + reason, cause);
    }

}
