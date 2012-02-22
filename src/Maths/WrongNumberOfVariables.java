package Maths;

/**
 * Exception that is thrown if a function is passed the wrong number of variables
 * @author Daniel Money
 * @version 1.0
 */
public class WrongNumberOfVariables extends Exception
{
    /**
     * Default constrcutor
     * @param name The name of the function
     * @param wanted The number of variables the function takes
     * @param found The number of variables that were actually passed
     */
    public WrongNumberOfVariables(String name, int wanted, int found)
    {
        super("Function: " + name + ", Wanted: " + wanted + ", Found: " + found);
    }
}
