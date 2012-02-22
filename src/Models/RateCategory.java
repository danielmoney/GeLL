package Models;

import Exceptions.GeneralException;
import Exceptions.InputException;
import Exceptions.UnexpectedError;
import Parameters.Parameters;
import Maths.MathsParse;
import Maths.NoSuchFunction;
import Maths.WrongNumberOfVariables;
import Maths.SquareMatrix;
import Maths.SquareMatrix.SquareMatrixException;
import Models.Distributions.DistributionsException;
import Parameters.Parameter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a rate category of a phylogenetic model.
 * @author Daniel Money
 * @version 1.0
 */
public class RateCategory implements Serializable
{
    /**
     * Constrcutor for when the root distribution is defined as the stationary
     * or quasi-staionary distribution.
     * @param rates Array representing the rate matrix
     * @param freqType How the root frequency is calculated
     * @param map Map from State to position in matrix (0-index).  For example
     * map would contain A -> 0 if state A was in the first position (column and
     * row of the matrix.
     * @throws Models.RateCategory.RateException  If the rate matrix is not square
     */
    public RateCategory(String[][] rates, FrequencyType freqType, HashMap<String, Integer> map) throws RateException
    {
        this(rates, freqType, null, map);
    }

    /**
     * Constructor for when the root distribution is defined.
     * @param rates Array representing the rate matrix
     * @param freq Root frequency.
     * @param map Map from State to position in matrix (0-index) or root frequency.  
     * For example map would contain A -> 0 if state A was in the first position
     * (column and row) of the matrix and in the first position of the root frequency
     * array.
     * @throws Models.RateCategory.RateException  If the rate matrix is not square
     * or the frequncy array is not the same length as the rate matrix.
     */
    public RateCategory(String[][] rates, String[] freq, HashMap<String, Integer> map) throws RateException
    {
        this(rates,FrequencyType.MODEL,freq,map);
    }
    

    private RateCategory(String[][] rates, FrequencyType freqType, String[] freq, HashMap<String, Integer> map) throws RateException
    {
        //The two non-private constructors should ensure these are never reached
        //but just in case...
        switch (freqType)
        {
            case MODEL:
                if (freq == null)
                {
                    throw new RateException("Frequency type set to model, yet no frequency array given");
                }
                break;
            default:
                if (freq != null)
                {
                    throw new RateException("Frequency type not set to model, yet frequency array given");
                }
        }
        
        //Check we've been passed a square matrix
        int size = rates.length;
        for (String[] i: rates)
        {
            if (i.length != size)
            {
                throw new RateException("Rate matrix is not square");
            }
        }
        //And if the frequency is being defined by the "model" make sure this
        //is the same size as the rate matrix
        if (freqType == freqType.MODEL)
        {
            if (freq.length != size)
            {
                throw new RateException("Frequency array is not the same length as the "
                        + "rate matrix");
            }
        }
        
	this.rates = rates;
	this.freq = freq;
	this.freqType = freqType;
	this.map = map;
	setNeeded();
    }

    private void setNeeded()
    {
        //Build a list of needed parameters from the parameters appearing in
        //each element of the rate matrix.
        
        //First concetenate each element into one long string
	StringBuilder raw = new StringBuilder();
	for (int i = 0; i < rates.length; i++)
	{
	    for (int j = 0; j < rates.length; j++)
	    {
		raw.append(rates[i][j]);
		raw.append(" ");
	    }
	}
        //Add the frequency params if apropaite
	if (freq != null)
	{
	    for (int i = 0; i < freq.length; i++)
	    {
		raw.append(freq[i]);
		raw.append(" ");
	    }
	}
        //Now find all the variables in the resulting string
	Pattern p = Pattern.compile("[A-Za-z]\\w*(?![\\[\\w])");
	Matcher match = p.matcher(raw.toString());
	neededParams = new TreeSet<>();
	while (match.find())
	{
	    neededParams.add(match.group());
	}
    }

    /**
     * Updates the parameters in the RateCategory.  Should only be called if
     * a parameter in the matrix / frequency defintion has chnaged.
     * @param p The new parameters
     * @throws Models.RateCategory.RateException If the parameters passed does not
     * include all the parameters in the model.
     */
    public void setParameters(Parameters p) throws RateException
    {
        //Check if there are missing parameters...
        Set<String> missing = new TreeSet<>();
	for (String s : neededParams)
	{
	    boolean has = false;
	    for (Parameter pp : p)
	    {
		if (s.equals(pp.getName()))
		{
		    has = true;
		}
	    }
	    if (!has)
	    {
		missing.add(s);
	    }
	}
        
        //And if so thrown an exception
	if (missing.size() > 0)
	{
	    StringBuilder miss = new StringBuilder();
	    for (String s : missing)
	    {
		miss.append(s);
		miss.append(", ");
	    }
	    throw new RateException( 
                    "Parameters " + miss.substring(0, miss.length() - 2) + " have not been passed");
	}
        
        //Calculate and store the rate matrix and frequency
	m = calculateMatrix(p);
	f = calculateFreq(p);
        
        //Since we have a new rate matrix we need a new cache.  This effectively
        //stores P matrices for given lengths.  GoldenSection search will only
        //update one length at a time why the others stay the same so no point
        //recaluclating them all
        cache = new HashMap<>();
        
        //Set the scaled matrix to the same as the normal matrix.  We need to set
        //the parameters, then calculate the rate at the model level (across all
        //categories) before setting the scale so we can't set this to it's final
        //value here
        sm = m;
    }
    
    /**
     * Sets the scale that should be used to ensure the enclosing model has an
     * average rate of 1.
     * @param scale The scale to be used
     */
    void setScale(double scale)
    {
	sm = m.scalarMultiply(scale);
    }

    /**
     * Gets the total rate of the rate class.
     * @return The total rate
     */
    public double getTotalRate()
    {
        double[][] old = m.getArray();
        double t = 0.0;
	for (int i = 0; i < old.length; i++)
	{
	    for (int j = 0; j < old.length; j++)
	    {
		if (i != j)
		{
		    t += old[i][j] * f[i];
		}
	    }
	}
	return t;
    }

    private SquareMatrix calculateMatrix(Parameters params) throws RateException
    {
        //Calculate a rate matrix (of doubles) from the equations in the matrix
	double[][] n = new double[rates.length][rates.length];

	HashMap<String, Double> values = params.getValues();

        //Evaluate an equation is slow and the same equation is often used
        //multiple times so cache this
	HashMap<String, Double> equationCache = new HashMap<>();

	for (int i = 0; i < rates.length; i++)
	{
	    double total = 0.0;
	    for (int j = 0; j < rates.length; j++)
	    {
                //The diagonal are calculated as the sum of the other row entries
                //so ignore them here apart from to calculate the total
		if (i != j)
		{
		    try
		    {
			if (equationCache.containsKey(rates[i][j]))
			{
			    n[i][j] = equationCache.get(rates[i][j]);
			}
			else
			{
			    n[i][j] = mp.parseEquation(rates[i][j], values);
			    equationCache.put(rates[i][j], n[i][j]);
			}
		    }
		    catch (NoSuchFunction ex)
		    {
			throw new RateException("Rate + [" + i + "," + j + "]",
				rates[i][j], "No Such Function", ex);
		    }
		    catch (WrongNumberOfVariables ex)
		    {
			throw new RateException("Rate + [" + i + "," + j + "]",
				rates[i][j], "Wromg Number of Variables for Function", ex);
		    }
		    total += n[i][j];
		}
	    }
            //Set the diagonal entry
	    n[i][i] = -total;
	}

	try
	{
	    return new SquareMatrix(n);
	}
	catch (SquareMatrixException e)
	{
            //Constructor for SquareMatrix only throws an error if the matrix
            //isn't square but we know this shouldn't occur as it's been tested
            //in the constructor
	    throw new UnexpectedError(e);
	}
    }

    /**
     * Gets the root frequencies
     * @return An array containing the root frequencies.  Order is that given by
     * the map returned by {@link #getMap()} and which was passed to the constuctor.
     */
    public double[] getFreq()
    {
	return f;
    }

    private double[] calculateFreq(Parameters params) throws RateException
    {
        //Calculate the frequency either from the rate matrix or by evaluating
        //the equations in the array
	//SquareMatrix matrix;
	switch (freqType)
	{
	    case STATIONARY:
		try
		{
                     return Distributions.stationary(m);
		}
		catch (DistributionsException e)
		{
		    throw new RateException("Problem "
                            + "calculating stationary distribution", e);
		}
	    case QSTAT:
		try
		{
                    return Distributions.quasiStationary(m);
		}
		catch (DistributionsException e)
		{
		    throw new RateException("Problem "
                            + "calculating stationary distribution", e);
		}
	    case MODEL:
	    default:
		HashMap<String, Double> values = params.getValues();
		double[] fr = new double[freq.length];
		for (int i = 0; i < freq.length; i++)
		{
		    try
		    {
			fr[i] = mp.parseEquation(freq[i], values);
		    }
		    catch (NoSuchFunction ex)
		    {
			throw new RateException("Frequency + [" + i + "]",
				freq[i], "No Such Function", ex);
		    }
		    catch (WrongNumberOfVariables ex)
		    {
			throw new RateException("Frequency + [" + i + "]",
				freq[i], "Wromg Number of Variables for Function", ex);
		    }
		}
		// Scale to total of 1.0
		double total = 0.0;
		for (double ff : fr)
		{
		    total += ff;
		}
		for (int i = 0; i < fr.length; i++)
		{
		    fr[i] = fr[i] / total;
		}
		return fr;
	}
    }

    /**
     * Gets the number of states in the rate class
     * @return The number of states
     */
    public int getNumberStates()
    {
	return map.keySet().size();
    }

    /**
     * Gets the map that maps state to position in matrix
     * @return Map from state to position in matrix
     */
    public HashMap<String, Integer> getMap()
    {
	return map;
    }

    /**
     * Returns a new RateClass where every position in the rate matrix is multiplied
     * by a given value
     * @param mult Value to be multiplied (as a string as it will be evaluated as
     * a equation)
     * @return The new RateClass
     */
    RateCategory multiplyBy(String mult)
    {
	String[][] nr = new String[rates.length][rates.length];
	for (int i = 0; i < rates.length; i++)
	{
	    for (int j = 0; j < rates.length; j++)
	    {
		nr[i][j] = "(" + mult + ")*(" + rates[i][j] + ")";
	    }
	}

        try
        {
            return new RateCategory(nr, freqType, freq, map);
        }
        catch (RateException ex)
        {
            // Shouldn't happen as would have thrown an error when creating
            // the original object
            throw new UnexpectedError(ex);
        }
    }

    /**
     * Gets the P-matrix for a given length
     * @param length The length
     * @return The probability matrix.   Order is that given by the map returned
     * by {@link #getMap()} and which was passed to the constuctor.
     * @throws Models.RateCategory.RateException Thrown if the matrix cannot be
     * calculated.  
     */
    public SquareMatrix getP(double length) throws RateException
    {
        if (cache.containsKey(length))       
        {
            return cache.get(length);
        }
        else
        {
            try
            {
                SquareMatrix P = sm.expMult(length);
                cache.put(length,P);
                return P;
            }
            catch (SquareMatrixException e)
            {
                throw new RateException("Problem calculating P matrix",e);
            }
        }
    }
    
    /**
     * Sets the name of the rate category.  Used so there is something meaningful
     * to return to the user'
     * @param name The rate category's name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Get the name of the rate category
     * @return The rate category's name.
     */
    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        if (name != null)
        {
            return name;
        }
        else
        {
            return super.toString();
        }
    }


    private SquareMatrix sm;    
    private Map<Double,SquareMatrix> cache;
    private TreeSet<String> neededParams;
    private SquareMatrix m;
    private double[] f;
    private String[] freq;
    private String[][] rates;
    private HashMap<String, Integer> map;
    private FrequencyType freqType;
    private static MathsParse mp = new MathsParse();    
    private String name = null;
    
    /**
     * Enumeration of the different ways of defining the root frequency
     */
    public enum FrequencyType
    {
        /**
         * Uses the values defined in the model
         */
        MODEL,
        /**
         * Use the stationary distribution of the rate matrix
         */
        STATIONARY,
        /**
         * Use the quasi-stationary distribution of the rate matrix
         */
        QSTAT
    }

    /**
     * Creates an instance from the information in a file.  See {@link Maths.MathsParse}
     * for a description of the format of the equations that can be in the rate
     * matrix and root distribution.
     * File format is as follows:
     * <ul>
     * <li>First line contains the number of states the RateCategory has</li>
     * <li>Second line is blank</li>
     * <li>Third line is a list of states in the order they appear in the rate matrix,
     * tab-seprated</li>
     * <li>Forth line is blank</li>
     * <li>Fifth and subsquent lines contain the rate matrix, one row per line.  Columns
     * in a row are serated by tabs.  Each entry can be an equation.</li>
     * <li>The rate matrix is followed by a blank line</li>
     * <li>Finally thee is a line giivng the base frequencies.  Three different
     * values are allowed:
     * <ol>
     * <li><i>Model frequencies</i> - This line contains an equation for the frequency
     * of each state, in the same order as the rate matrix and tab-seperated
     * <li><i>Stationary distribution</i> - Line contains just "**S" (without the quotes)
     * <li><i>Quasi-stationary distribution</i> - Line contains just "**Q" 
     * (without the quotes)
     * </ol></li>
     * </ul>
     * @param mfile The input file
     * @return An instance of this class
     * @throws InputException If there is a problem with the input file
     * @throws Models.RateCategory.RateException If the RateCategory can not be created 
     */
    public static RateCategory fromFile(File mfile) throws InputException, RateException
    {
	String line = null;
	try
	{
	    HashMap<String, Integer> map = new HashMap<>();
	    String[][] rates = null;
	    FrequencyType freqType = FrequencyType.MODEL;
	    String[] freq = null;

	    BufferedReader in = new BufferedReader(new FileReader(mfile));

	    String[] parts;

	    line = in.readLine();
	    int size = Integer.parseInt(line);

	    in.readLine();

	    line = in.readLine();
	    parts = line.split("\\t+");
	    map = new HashMap<>();
	    for (int i = 0; i < size; i++)
	    {
		map.put(parts[i], i);
	    }

	    in.readLine();

	    rates = new String[size][size];
	    for (int i = 0; i < size; i++)
	    {
		line = in.readLine();
		parts = line.split("\\t+");

		for (int j = 0; j < parts.length; j++)
		{
		    rates[i][j] = parts[j];
		}
	    }

	    in.readLine();

	    line = in.readLine();
	    if (line.startsWith("**"))
	    {
		if (line.equals("**Q"))
		{
		    freqType = FrequencyType.QSTAT;
		}
		if (line.equals("**S"))
		{
		    freqType = FrequencyType.STATIONARY;
		}
	    }
	    else
	    {
		freq = line.split("\\t+");
	    }
	    in.close();
	    RateCategory r = new RateCategory(rates, freqType, freq, map);
	    return r;
	}
	catch (FileNotFoundException e)
	{
	    throw new InputException(mfile.getAbsolutePath(), "Not Applicable", "File does not exist", e);
	}
	catch (IOException e)
	{
	    throw new InputException(mfile.getAbsolutePath(), "Not Applicable", "Problem reading file", e);
	}
	catch (NumberFormatException e)
	{
	    throw new InputException(mfile.getAbsolutePath(), line, "Number format problem", e);
	}
    }
    
    private static final long serialVersionUID = 1;
    
    /**
     * Exception thrown if there is a problem within a RateClass
     */
    public class RateException extends GeneralException
    {
        
        /**
         * Constructor when there is a problem at a specific point in the rate matrix
         * or frequency array
         * @param location A description of where the problem occured
         * @param text The text that caused the problem (if applicable)
         * @param reason Description of the problem
         * @param cause The underlying Throwable if applicable or null if not
         */
        public RateException(String location, String text, String reason, Throwable cause)
        {
            super("Rate Error\n\tLocation:\t" + location + "\n\tText:\t" + text +
                    "\n\tReason:\t" + reason, cause);
        }

        /**
         * Constructor for other exception when there isn't an underlying cause
         * @param msg The problem
         */
        public RateException(String msg)
        {
            super(msg,null);
        }
        
        /**
         * Constructor for other exception when there is an underlying cause
         * @param msg The problem
         * @param cause The underlying cause
         */
        public RateException(String msg, Throwable cause)
        {
            super(msg,cause);
        }
    }
}
