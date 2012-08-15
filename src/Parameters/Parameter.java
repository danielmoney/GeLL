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

import Parameters.Parameters.ParameterException;
import java.io.Serializable;

/**
 * Represents a parameter of a model
 * @author Daniel Money
 * @version 1.3
 */
public class Parameter implements Serializable
{
    private Parameter(String name, double value, boolean estimate,
	    double lbound, double ubound, boolean matrix)
    {
	this.name = name;
	this.value = value;
	this.estimate = estimate;
	this.lbound = lbound;
	this.ubound = ubound;
	this.matrix = matrix;
    }
    
    public Parameter clone()
    {
        return new Parameter(name,value,estimate,lbound,ubound,matrix);
    }
    
    /**
     * Gets the name of the parameter
     * @return The name
     */
    public String getName()
    {
	return name;
    }
    
    /**
     * Gets the value of the parameter
     * @return The value
     */
    public double getValue()
    {
	return value;
    }
    
    void setValue(double val) throws ParameterException
    {
        if ((val < lbound) || (val > ubound))
        {
            throw new ParameterException("Attempt to set parameter to value not within bounds");
        }
	value = val;
    }
    
    /**
     * Gets the lower bound of a parameter
     * @return The lower bound
     */
    public double getLowerBound()
    {
	return lbound;
    }
    
    /**
     * Gets the upper bound of a parameter
     * @return The upper bound
     */
    public double getUpperBound()
    {
	return ubound;
    }
    
    /**
     * Returns whether this paramater is a parameter that should be estimated
     * @return Whether this parameter is to be estimated
     */
    public boolean getEstimate()
    {
	return estimate;
    }

    /**
     * Whether this parameter is in a rate matrix.
     * @return Whether this parameter is in a rate matrix.
     */
    public boolean matrix()
    {
	return matrix;
    }

    public String toString()
    {
	return name + "\t" + value;
    }

    private String name;
    private double value;
    private boolean estimate;
    private double lbound;
    private double ubound;
    private boolean matrix;
    
    /**
     * Creates a new parameter that has to be positive and is estimated.  Defaults
     * to being in a rate matrix.
     * @param name The name of the parameter
     * @return The parameter
     */
    public static Parameter newEstimatedPositiveParameter(String name)
    {
	return new Parameter(name,1.0,true,0.0,Double.MAX_VALUE,true);
    }

    /**
     * Creates a new (unbounded) paramater that is estimated.  Defaults to being
     * in a rate matrix.
     * @param name The name of the parameter
     * @return The parameter
     */
    public static Parameter newEstimatedParameter(String name)
    {
	return new Parameter(name,1.0,true,-Double.MAX_VALUE,Double.MAX_VALUE,true);
    }
    
    /**
     * Creates a new bounded parameter that is estimated.  Defaults to being in
     * a rate matrix.
     * @param name The name of the parameter
     * @param lbound The lower bound of the value the parameter can take
     * @param ubound The upper bound of the value the parameter cantake
     * @return The parameter
     */
    public static Parameter newEstimatedBoundedParameter(String name,
	    double lbound, double ubound)
    {
	return new Parameter(name,0.8,true,lbound,ubound,true);
    }
    
    /**
     * Creates a new fixed parameter (i.e. it is not estimated).
     * @param name The parameters name
     * @param value The value of the parameter
     * @return The parameter
     */
    public static Parameter newFixedParameter(String name, double value)
    {
	return new Parameter(name,value,false,-Double.MAX_VALUE,Double.MAX_VALUE, false);
    }

    /**
     * Creates a new positive parameter that is estimated and is optionally in
     * a rate matrix.
     * @param name The name of the parameter
     * @param matrix Whether the parameter is in a rate matrix
     * @return The parameter
     */
    public static Parameter newEstimatedPositiveParameter(String name, boolean matrix)
    {
	return new Parameter(name,1.0,true,0.0,Double.MAX_VALUE,matrix);
    }

    /**
     * Creates a new unbounded parameter that is estimated and is optionally in
     * a rate matrix.
     * @param name The name if the parameter
     * @param matrix Whether the parameter is in a rate matrix
     * @return The parameter
     */
    public static Parameter newEstimatedParameter(String name, boolean matrix)
    {
	return new Parameter(name,1.0,true,-Double.MAX_VALUE,Double.MAX_VALUE,matrix);
    }

    /**
     * Creates a new bounded paramater that is estimated and is optionally in a
     * rate matrix
     * @param name The name of the parameter
     * @param lbound The lower bound of the value the parameter can take
     * @param ubound The upper bound of the value the parameter cantake
     * @param matrix Whether the parameter is in a rate matrix
     * @return The parameter
     */
    public static Parameter newEstimatedBoundedParameter(String name,
	    double lbound, double ubound, boolean matrix)
    {
	return new Parameter(name,1.0,true,lbound,ubound,matrix);
    }

    /**
     * Creates a new paramter from a string. <br><br>
     * Strings are tab seperated.  The first field is the type of the parameter
     * and the second is the name of the parameter.  Subsquent fields depend on
     * the parameter type.<br><br>
     * Type values
     * <ul>
     * <li>EB - Estimated bound parameter that is in a rate matrix.  3rd field is 
     * the lower bound, 4th the upper.</li>
     * <li>EBN - Estimated bound parameter that is not in a rate matrix.  3rd field
     * is the lower bound, 4th the upper.</li>
     * <li>EP - Estimated positive parameter that is in a rate matrix.</li>
     * <li>EPN - Estimate positive parameter that is not in a rate matrix.</li>
     * <li>E - Estimated (unbounded) paramater that is in a rate matrix.</li>
     * <li>EN - Estimated (unbounded) parameter that is not in a rate matrix.</li>
     * <li>F - Fixed parameter.  3rd field is the value.</li>
     * </ul>
     * @param s The string to be parsed
     * @return A Parameter based on that string
     * @throws NumberFormatException
     * @throws FormatException
     */
    static Parameter fromString(String s) throws NumberFormatException, FormatException
    {
	String[] parts = s.split("\\s+");

	if (parts[0].equals("EB"))
	{
	    if (parts.length == 4)
	    {
		return newEstimatedBoundedParameter(parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}

	if (parts[0].equals("EBN"))
	{
	    if (parts.length == 4)
	    {
		return newEstimatedBoundedParameter(parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3]), false);
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}
	
	if (parts[0].equals("EP"))
	{
	    if (parts.length == 2)
	    {
		return newEstimatedPositiveParameter(parts[1]);
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}

	if (parts[0].equals("EPN"))
	{
	    if (parts.length == 2)
	    {
		return newEstimatedPositiveParameter(parts[1], false);
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}

	if (parts[0].equals("E"))
	{
	    if (parts.length == 2)
	    {
		return newEstimatedParameter(parts[1]);
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}

	if (parts[0].equals("EN"))
	{
	    if (parts.length == 2)
	    {
		return newEstimatedParameter(parts[1], false);
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}

	if (parts[0].equals("F"))
	{
	    if (parts.length == 3)
	    {
		return newFixedParameter(parts[1], Double.valueOf(parts[2]));
	    }
	    else
	    {
		throw new FormatException("Wrong number of variables");
	    }
	}

	throw new FormatException("Not a valid parameter setting");
    }
    
    /**
     * Exception thrown when the format of a parameter string is incorrect
     */
    public static class FormatException extends Exception
    {
        /**
         * Default constuctor
         * @param msg Message explaining the format error
         */
        public FormatException(String msg)
        {
            super(msg);
        }
    }

    private static final long serialVersionUID = 1;
}
