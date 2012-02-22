package Maths;

/**
 * Exception that is thrown when a function has not been defined
 * @author Daniel Money
 * @version 1.0
 */
public class NoSuchFunction extends Exception
{
    /**
     * Default constructor
     * @param name The name of the function that is undefined
     */
    public NoSuchFunction(String name)
    {
        super("No function with name \"" + name + "\" was found");
    }
}
