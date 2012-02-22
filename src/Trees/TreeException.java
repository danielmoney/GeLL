package Trees;

import Exceptions.GeneralException;

/**
 * Exception thrown when there is a problem with a tree
 * @author Daniel Money
 * @version 1.0
 */
public class TreeException extends GeneralException
{
    /**
     * Default constructor
     * @param msg The cause of the problem
     */
    public TreeException(String msg)
    {
	super(msg,null);
    }
}
