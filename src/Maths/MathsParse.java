package Maths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used to parse an equation represented by a string and return the result.
 * Variables are represented by a letter followed by any number of alphanumeric
 * characters.  Multiply (represented by "*" should be stated explicitly, e.g. 
 * a * b NOT a b or ab (the later of which would be parsed as a single variable).
 * Functions should be represented by "f[a,b,...]" where f is the function name 
 * and a,b etc. are inputs.  Inputs cannot contain other functions but can otherwise
 * contain an expression. The following functions are defined by default:
 * <ul>
 *  <li>ln[a] - The natural logarithm</li>
 *  <li>g[a,b,c] - The rate modifier of the bth class of c classes using a gamma
 * distribution with alpha value of a as per Yang 1993.</li>
 * </ul>
 * Additional functions can be defined by passing instances of classes that
 * implement {@link FunctionParser} to the constructor.
 * @author Daniel Money
 * @version 1.0
 */
public class MathsParse
{
    /**
     * Default constructor.
     * @param fps A variable number of parsers that define additional functions.
     * If none is passed then only the default functions are avaliable.
     */
    public MathsParse(FunctionParser... fps)
    {
        functions = new HashMap<>();
        
        FunctionParser bi = new BuiltIn();
        for (String f : bi.implemented())
        {
            functions.put(f, bi);
        }

	for (FunctionParser fp: fps)
	{
	    for (String f : fp.implemented())
	    {
		functions.put(f, fp);
	    }
	}
    }


    /**
     * Parses an equation and returns the result
     * @param equation The equation to parse
     * @param values Map from a string to a double that represents the value of
     * any variables in the equation
     * @return The result of the equation
     * @throws NoSuchFunction If the equation uses an undefined function
     * @throws WrongNumberOfVariables If the equation uses a function and passes
     * it the wrong number of variables
     */
    public double parseEquation(String equation, Map<String,Double> values) throws NoSuchFunction, WrongNumberOfVariables
    {
        //Keeps track of already evaluated parts of the equation.
        //Parts of the equation that have been evaluated are replaced with
        //{id} and this map keeps track of what the value of that sting is.
        //Done this way so as not to lose precission by writing value as text
        //in equation.
        HashMap<String,Double> results = new HashMap<>(values);
        
        //Keeps track of the next unused id for use in the above.
	int id = 0;
        
	//Remove spaces
	equation = equation.replaceAll(" ", "");

	/************************************
	 * START SCIENTIFIC NOTATION        *
	 ************************************/
        
        //Gets rid of any scientific notation in the string by "evaluating" it as
        //otherwise the 'e' will cause problems later
	Pattern p = Pattern.compile("\\d+\\.\\d+[eE]-?\\d+");
	Matcher m = p.matcher(equation);
	while (m.matches())
	{
	    results.put("{" + id + "}",Double.parseDouble(m.group()));
	    equation = equation.substring(0,m.start()) + "{" + id + "}" + equation.substring(m.end());
	    id++;

	    m = p.matcher(equation);
	}
	
	/************************************
	 * START BRACKETS                   *
	 ************************************/
	
        //Keeps a running track of how many unclosed brackets have been encountered
        int numb = 0;
        
        //Keeps track of the position of the earliest unmatched bracket
        int start = 0;
        
        //Keeps track of the current position in the string
        int pos = 0;
        
        //Parse the string keeping track of the outermost unmatched start bracket...
	while (pos < equation.length())
	{
	    if (equation.charAt(pos) == '(')
	    {
		if (numb == 0)
		{
		    start = pos;
		}
		numb++;
	    }
            //When we match the outermost bracket recursively evaluate what's in
            //the bracket and replace in the equation.
	    if (equation.charAt(pos) == ')')
	    {
		numb--;
		if (numb == 0)
		{
		    results.put("{" + id + "}",parseEquation(equation.substring(start+1,pos),values));
		    equation = equation.substring(0,Math.max(0,start)) + "{" + id + "}" + equation.substring(pos+1);
		    id++;
		    pos = 0;
		}
	    }
	    pos++;
	}
	
	/************************************
	 * START FUNCTIONS                  *
	 ************************************/

         //While we have unevaluated functions (i.e. we still have one or more '['
         //present in the string
	 while (equation.indexOf('[') > -1)
	 {
             //Find andevaluate the inputs to the function
	    String[] inputs = equation.substring(equation.indexOf('[')+1,equation.indexOf(']')).split(",");
	    Double[] vs = new Double[inputs.length];
	    for (int j = 0; j < inputs.length; j++)
	    {
		vs[j] = parseEquation(inputs[j],values);
	    }
	    
            //Find the string to the left of the [
	    String fLeft = equation.substring(0,equation.indexOf('['));
	    
            //And then find the right most operator in that string
	    int ls = -1;
	    ls = Math.max(ls,fLeft.lastIndexOf('+'));
	    ls = Math.max(ls,fLeft.lastIndexOf('-'));
	    ls = Math.max(ls,fLeft.lastIndexOf('*'));
	    ls = Math.max(ls,fLeft.lastIndexOf('/'));
	    ls = Math.max(ls,fLeft.lastIndexOf('('));
	    ls = Math.max(ls,fLeft.lastIndexOf('^'));
	    	    
            //And whats between that operator and the [ must be the function name
	    String name = fLeft.substring(ls+1);

            //Evaluate the function, store the result and replace in the equation
	    if (functions.containsKey(name))
	    {
		results.put("{" + id + "}",functions.get(name).evaluate(name,vs));
	    }
	    else
	    {
		throw new NoSuchFunction(name);
	    }
	    equation = equation.substring(0,Math.max(0,ls+1)) + "{" + id + "}" + equation.substring(equation.indexOf(']')+1);
	    id++;
	 }
	
	/************************************
	 * START POWERS                     *
	 ************************************/
        
        //Keeps the location of the operator for powers, multiply/divide and
        //plus/minus
        int f;
	
        //While there are unevcaluated power operations
	while ((f = equation.indexOf('^')) > -1)
	{
            //Find the string to the left and the right of the operator
	    String fLeft = equation.substring(0,f);
	    String fRight = equation.substring(f+1);
	    
            //Find the right most operator in the left string
	    int ls = -1;
	    ls = Math.max(ls,fLeft.lastIndexOf('+'));
	    ls = Math.max(ls,fLeft.lastIndexOf('-'));
	    ls = Math.max(ls,fLeft.lastIndexOf('*'));
	    ls = Math.max(ls,fLeft.lastIndexOf('/'));
	    
            //And then what's between the operator and the ^ must be the a in a^b
	    String left = fLeft.substring(ls+1);
            //Calculate the value of a (stored in lv)
	    double lv;
            //If it's a variable or an already evaluate string get the result
	    if (left.matches("\\{\\d+\\}") || left.matches(VAR))
	    {
		lv = results.get(left);
	    }
            //Else parse as a double
	    else
	    {
		lv = Double.valueOf(left);
	    }
	    
            //Do the same for the right side of the string, i.e. the b in a^b
	    int re = fRight.length();
	    if (Math.min(re,fRight.indexOf('+'))>-1) {re = Math.min(re,fRight.indexOf('+'));}
	    if (Math.min(re,fRight.indexOf('-'))>-1) {re = Math.min(re,fRight.indexOf('-'));}
	    if (Math.min(re,fRight.indexOf('*'))>-1) {re = Math.min(re,fRight.indexOf('*'));}
	    if (Math.min(re,fRight.indexOf('/'))>-1) {re = Math.min(re,fRight.indexOf('/'));}
	    if (Math.min(re,fRight.indexOf('^'))>-1) {re = Math.min(re,fRight.indexOf('^'));}
	    
	    String right = fRight.substring(0,re);
	    double rv;
	    if (right.matches("\\{\\d+\\}") || right.matches(VAR))
	    {
		rv = results.get(right);
	    }
	    else
	    {
		rv = Double.valueOf(right);
	    }
	    
            //Calculate the result, store it and replace it in the string
	    results.put("{" + id + "}",Math.pow(lv,rv));
	    equation = equation.substring(0,Math.max(0,ls+1)) + "{" + id + "}" + equation.substring(f + re + 1);
	    id++;
	}
	
	/************************************
	 * START MULTIPLY / DIVIDE          *
	 ************************************/
	
        //This works essentially the same as the power section except we need
        //to know whether we're doing multiply or divide and the location of the
        //first instance of either
	while ((equation.indexOf('*') > -1) || (equation.indexOf('/') > -1))
	{
	    boolean div;
            // Calculate where the first multiply or divide is and what one it is
	    int ft; if (equation.indexOf('*') > - 1) {ft = equation.indexOf('*');} else {ft = Integer.MAX_VALUE;}
	    int fd; if (equation.indexOf('/') > - 1) {fd = equation.indexOf('/');} else {fd = Integer.MAX_VALUE;}
	    if (fd < ft)
	    {
		div = true;
		f = fd;
	    }
	    else
	    {
		div = false;
		f = ft;
	    }
	    String fLeft = equation.substring(0,f);
	    String fRight = equation.substring(f+1);
	    
	    int ls = -1;
	    ls = Math.max(ls,fLeft.lastIndexOf('+'));
	    ls = Math.max(ls,fLeft.lastIndexOf('-'));
	    
	    String left = fLeft.substring(ls+1);
	    double lv;
	    if (left.matches("\\{\\d+\\}") || left.matches(VAR))
	    {
		lv = results.get(left);
	    }
	    else
	    {
		lv = Double.valueOf(left);
	    }
	    
	    int re = fRight.length();
	    if (Math.min(re,fRight.indexOf('+'))>-1) {re = Math.min(re,fRight.indexOf('+'));}
	    if (Math.min(re,fRight.indexOf('-'))>-1) {re = Math.min(re,fRight.indexOf('-'));}
	    if (Math.min(re,fRight.indexOf('*'))>-1) {re = Math.min(re,fRight.indexOf('*'));}
	    if (Math.min(re,fRight.indexOf('/'))>-1) {re = Math.min(re,fRight.indexOf('/'));}
	    
	    String right = fRight.substring(0,re);
	    double rv;
	    if (right.matches("\\{\\d+\\}") || right.matches(VAR))
	    {
		rv = results.get(right);
	    }
	    else
	    {
		rv = Double.valueOf(right);
	    }
	    
	    if (div)
	    {
		results.put("{" + id + "}",lv/rv);
	    }
	    else
	    {
		results.put("{" + id + "}",lv*rv);
	    }
	    equation = equation.substring(0,Math.max(0,ls)) + "{" + id + "}" + equation.substring(f + re + 1);
	    id++;
	}
	
	/************************************
	 * START PLUS / MINUS               *
	 ************************************/
	
        //Same as multiply / divide
	while ((equation.indexOf('+') > -1) || (equation.indexOf('-') > -1))
	{
	    boolean minus;
	    int fp; if (equation.indexOf('+') > - 1) {fp = equation.indexOf('+');} else {fp = Integer.MAX_VALUE;}
	    int fm; if (equation.indexOf('-') > - 1) {fm = equation.indexOf('-');} else {fm = Integer.MAX_VALUE;}
	    if (fm < fp)
	    {
		minus = true;
		f = fm;
	    }
	    else
	    {
		minus = false;
		f = fp;
	    }
	    String fLeft = equation.substring(0,f);
	    String fRight = equation.substring(f+1);
	    
	    int ls = -1;
	    ls = Math.max(ls,fLeft.lastIndexOf('+'));
	    ls = Math.max(ls,fLeft.lastIndexOf('-'));
	    
	    String left = fLeft.substring(ls+1);
	    double lv;
	    if (left.matches("\\{\\d+\\}") || left.matches(VAR))
	    {
		lv = results.get(left);
	    }
	    else
	    {
		lv = Double.valueOf(left);
	    }
	    
	    int re = fRight.length();
	    if (Math.min(re,fRight.indexOf('+'))>-1) {re = Math.min(re,fRight.indexOf('+'));}
	    if (Math.min(re,fRight.indexOf('-'))>-1) {re = Math.min(re,fRight.indexOf('-'));}
	    if (Math.min(re,fRight.indexOf('*'))>-1) {re = Math.min(re,fRight.indexOf('*'));}
	    if (Math.min(re,fRight.indexOf('/'))>-1) {re = Math.min(re,fRight.indexOf('/'));}
	    
	    String right = fRight.substring(0,re);
	    double rv;
	    if (right.matches("\\{\\d+\\}") || right.matches(VAR))
	    {
		rv = results.get(right);
	    }
	    else
	    {
		rv = Double.valueOf(right);
	    }
	    
	    if (minus)
	    {
		results.put("{" + id + "}",lv-rv);
	    }
	    else
	    {
		results.put("{" + id + "}",lv+rv);
	    }
	    equation = equation.substring(0,Math.max(0,ls)) + "{" + id + "}" + equation.substring(f + re + 1);
	    id++;
	}
        
	/************************************
	 * RETURN                           *
	 ************************************/        
        
        //Whats left will either be a variable or previously evaluated string
        //in which case returnt the value of that
	if (equation.matches("\\{\\d+\\}") || equation.matches(VAR))
	{
	    return results.get(equation);
	}
        //Or a number (if that's all that was passed) so parse and return
	else
	{
	    return Double.parseDouble(equation);
	}
    }

    //FunctionParser that evaluates ln and gamma as discussed in class javadoc
    private class BuiltIn implements FunctionParser
    {
	public double evaluate(String name, Double[] vs) throws NoSuchFunction, WrongNumberOfVariables
	{
	    if (name.equalsIgnoreCase("ln"))
	    {
		if (vs.length == 1)
		{
		    return Math.log(vs[0]);
		}
		else
		{
		    throw new WrongNumberOfVariables(name,1,vs.length);
		}
	    }
	    if (name.equalsIgnoreCase("g"))
	    {
		if (vs.length == 3)
		{
		    return Gamma.rates(vs[0],vs[2].intValue())[vs[1].intValue()-1];
		}
		else
		{
		    throw new WrongNumberOfVariables(name,3,vs.length);
		}
	    }
	    throw new NoSuchFunction(name);
	}

	public List<String> implemented()
	{
	    ArrayList<String> list = new ArrayList<>();
	    list.add("ln");
	    list.add("g");
	    return list;
	}
    }

    private HashMap<String,FunctionParser> functions;

    private static final String VAR = "[a-zA-Z_][a-zA-Z0-9]*";
}
