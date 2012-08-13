/*
 * This file is part of GeLL.
 * 
 * GeLL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeLL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeLL.  If not, see <http://www.gnu.org/licenses/>.
 */

package Parameters;

import Exceptions.GeneralException;
import Exceptions.InputException;
import Exceptions.OutputException;
import Parameters.Parameter.FormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Represents a set of parameters
 * @author Daniel Money
 * @version 1.0
 */
public class Parameters implements Iterable<Parameter>, Serializable
{
    /**
     * Creates an empty set of parameters
     */
    public Parameters()
    {
	params = new ArrayList<>();
	recalc = true;
    }

    /**
     * Creates a set of parameters containing the given parameters
     * @param params A list of parameters
     */
    public Parameters(ArrayList<Parameter> params)
    {
	this.params = params;
	recalc = true;
    }
    
    public Parameters clone()
    {
        Parameters clone = new Parameters();
        for (Parameter p: params)
        {
            clone.addParameter(p.clone());
        }
        return clone;
    }

    /**
     * Add a parameter
     * @param p The parameter
     */
    public void addParameter(Parameter p)
    {
	params.add(p);
    }

    /**
     * Adds the parameters from another set of parameters
     * @param pp The other set of parameters
     */
    public void addParameters(Parameters pp)
    {
	for (Parameter p : pp.params)
	{
	    params.add(p);
	}
    }

    /**
     * Tests whether any parameters that would require recalculation of the
     * probability matrices have been changed since the last call to {@link #calculated()} 
     * @return Whether the probability matrices need to be recalculated.
     */
    public boolean recalculateMatrix()
    {
	return recalc;
    }

    /**
     * Resets the testfor whether probability matrices need to be recalculalated.
     * Should be called just after they have been recalculated.
     */
    public void calculated()
    {
	recalc = false;
    }

    /**
     * Sets the value of a parameter
     * @param p The parameter
     * @param v The value
     * @throws Parameters.Parameters.ParameterException Thrown if an attempt 
     *      is made to set the parameter to a value outside it's bounds 
     */
    public void setValue(Parameter p, double v) throws ParameterException
    {
	p.setValue(v);
	recalc = (recalc || p.matrix());
    }

    /**
     * Gets the value of a parameter
     * @param name The parameters name
     * @return The value of that parameter.
     * @throws Parameters.Parameters.ParameterException Thrown if there is no
     * parameter with that name
     */
    public double getValue(String name) throws ParameterException
    {
	for (Parameter p : params)
	{
	    if (p.getName().equals(name))
	    {
		return p.getValue();
	    }
	}
        throw new ParameterException("No paramter with the name \"" + name + "\" exists.");
    }

    /**
     * Gets the parameter with the given name
     * @param name The name of the parameter
     * @return The parameter
     * @throws Parameters.Parameters.ParameterException Thrown if there is no
     * parameter with that name
     */
    public Parameter getParam(String name) throws ParameterException
    {
	for (Parameter p : params)
	{
	    if (p.getName().equals(name))
	    {
		return p;
	    }
	}
        throw new ParameterException("No paramter with the name \"" + name + "\" exists.");
    }
    
    /**
     * Checks whether there is a parameter with the given name
     * @param name The name of the parameter
     * @return Whether there is a parameter with that name
     */
    public boolean hasParam(String name)
    {
	for (Parameter p : params)
	{
	    if (p.getName().equals(name))
	    {
		return true;
	    }
	}
        return false;
    }

    /**
     * Gets a map from parameter name to the value of that parameter
     * @return Map from parameter name to parameter value
     */
    public HashMap<String, Double> getValues()
    {
	HashMap<String, Double> values = new HashMap<>();
	for (Parameter p : params)
	{
	    values.put(p.getName(), p.getValue());
	}
	return values;
    }

    /**
     * Gets the number of parameters to be estimated
     * @return The number of parameters to be estimated
     */
    public int numberEstimate()
    {
	int i = 0;
	for (Parameter p : params)
	{
	    if (p.getEstimate())
	    {
		i++;
	    }
	}
	return i;
    }
    
    public String toString()
    {
        return toString(true);
    }

    /**
     * Returns a string representation of the parameters
     * @param fixed Whether to include fixed parameters in the output
     * @return A string representation of the parameters
     */
    public String toString(boolean fixed)
    {
	StringBuilder s = new StringBuilder();
	for (Parameter p : params)
	{
            if (fixed || p.getEstimate())
            {
                s.append(p.toString());
                s.append("\n");
            }
	}
	return s.toString();
    }

    public Iterator<Parameter> iterator()
    {
	return params.iterator();
    }
 
    /**
     * Writes the parameter values to a file.  Each parameter uses one line and
     * the format of that line is as described at {@link Parameter#fromString(java.lang.String)}
     * @param f File to write the parameters to
     * @throws OutputException Thrown if an error occurs writing the file
     */
    public void toFile(File f) throws OutputException
    {
	PrintStream out;
	try
	{
	    out = new PrintStream(new FileOutputStream(f));
	}
	catch (FileNotFoundException e)
	{
	    throw new OutputException("File can not be created", f.getAbsolutePath(),e);
	}
	out.print(toString());
	out.close();
    }

    /**
     * Writes the parameter values to a file and writes a line labeled likelihood
     * and which gives the likelihood given as input.  Each parameter uses one line
     * and the format of that line is as described at {@link Parameter#fromString(java.lang.String)}
     * @param f File to write the parameters to
     * @param like The likelihood value
     * @throws OutputException Thrown if an error occurs writing the file
     */
    public void toFile(File f, double like) throws OutputException
    {
	PrintStream out;
	try
	{
	    out = new PrintStream(new FileOutputStream(f));
	}
	catch (FileNotFoundException e)
	{
	    throw new OutputException("File can not be created", f.getAbsolutePath(),e);
	}
	out.println("Like\t" + like + "\n");
	out.print(toString());
	out.close();
    }

    private ArrayList<Parameter> params;

    private boolean recalc;

    /**
     * Reads parameters from a file.  Each parameter is a single line and is
     * described at {@link Parameter#fromString(java.lang.String)}.
     * @param f The input file
     * @return A Parameters instance
     * @throws InputException If there is an input error
     */
    public static Parameters fromFile(File f) throws InputException
    {
	ArrayList<Parameter> params = new ArrayList<>();
	BufferedReader in;
	try
	{
	    in = new BufferedReader(new FileReader(f));
	}
	catch (FileNotFoundException e)
	{
	    throw new InputException(f.getAbsolutePath(), "Not Applicable", "File does not exist", e);
	}

	String line = null;
	do
	{
	    try
	    {
		line = in.readLine();
	    }
	    catch (IOException e)
	    {
		throw new InputException(f.getAbsolutePath(), "Not Applicable", "Problem reading file", e);
	    }
	    if ((line !=null) && (!line.equals("")))
	    {
		try
		{
		    params.add(Parameter.fromString(line));
		}
		catch (FormatException e)
		{
		    throw new InputException(f.getAbsolutePath(), line, "Invalid format", e);
		}
		catch (NumberFormatException e)
		{
		    throw new InputException(f.getAbsolutePath(), line, "Invalid number format", e);
		}
	    }
	}
	while (line != null);

	return new Parameters(params);
    }
    
    private static final long serialVersionUID = 1;
    
    /**
     * Exception related to the parameters
     */
    public static class ParameterException extends GeneralException
    {
        /**
         * Constructor
         * @param msg The reason for the exception
         */
        public ParameterException(String msg)
        {
            super(msg,null);
        }
    }
}
