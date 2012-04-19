package Maths;

/**
 * Exception that is thrown when a function has not been defined
 * @author Daniel Money
 * @version 1.2
 */
public class NoSuchVariable extends Exception
{
    /**
     * Default constructor
     * @param name The name of the function that is undefined
     */
    public NoSuchVariable(String name)
    {
        super("No variable with name \"" + name + "\" was found");
    }
}
