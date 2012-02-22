package Ancestors;

import Exceptions.GeneralException;

/**
 * Exception related to ancestral reconstruction
 * @author Daniel Money
 * @version 1.0
 */
public class AncestralException extends GeneralException
{
    /**
     * Constructor
     * @param reason The reason for the exception
     * @param cause Any underlying cause (null if none)
     */
    public AncestralException(String reason, Throwable cause)
    {
	super("Ancestral Reconstruction Error\n\tReason:\t" + reason, cause);
    }
}
