package Maths;

import java.util.List;

/**
 * Interface for a function parser.  That is a class that parses a string of
 * the form "function[double1,double2,...]" and return a numerical function
 * of it.  {@link MathsParse} implements some common functions used in phylogenetics
 * and is a useful example of how to implement this class.
 * @author Daniel Money
 * @version 1.0
 */
public interface FunctionParser
{
    /**
     * A function that evaluates all the functions implemented
     * @param function The function name
     * @param variables An array of inputs in the order they appear in the function
     *      call
     * @return The numerical result of the function
     * @throws WrongNumberOfVariables Thrown when the incorrect number of imputs
     *      to that function is passed.
     * @throws NoSuchFunction Thrown when the class can't parse a function of
     *      that name
     */
    public double evaluate(String function, Double[] variables) throws WrongNumberOfVariables, NoSuchFunction;
    
    public int numberInputs(String function) throws NoSuchFunction;
    
    /**
     * Returns a list of function names that this class can calculate
     * @return  A list of functions the class implements
     */
    public List<String> implemented();
}
