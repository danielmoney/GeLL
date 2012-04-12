package Models;

import Exceptions.GeneralException;
import Exceptions.InputException;
import Exceptions.UnexpectedError;
import Maths.CompiledFunction;
import Maths.CompiledFunction.Constant;
import Maths.FunctionParser;
import Maths.MathsParse;
import Maths.NoSuchFunction;
import Maths.WrongNumberOfVariables;
import Models.RateCategory.RateException;
import Parameters.Parameters;
import Utils.ArrayMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an evolutionary model.  May contain many different {@link RateCategory}.<br><br>
 * Rates and frequencies within a model (including the frequency of the different
 * rate classes) are represent by strings.  These strings may contain "parameters"
 * represented by a letter followed by a alphanumeric character which may later
 * (see {@link Parameters.Parameter}) be assigned a fixed value or optimised.  
 * They may also contain numbers and mathematical operations.  See 
 * {@link FunctionParser} for more on how these rates are evaluated.
 * @author Daniel Money
 * @version 1.0
 */
public class Model implements Iterable<RateCategory>
{
    /**
     * Creates a new model with a single rate class
     * @param r The single rate class
     */
    public Model(RateCategory r)
    {
	f = new HashMap<>();
	freq = new HashMap<>();
	//freq.put(r,"1.0");
        freq.put(r, new Constant(1.0));
	nStates = r.getNumberStates();
	map = r.getMap();
	scale = 1.0;
    }
    
    /**
     * Creates a new model with multiple rate classes
     * @param freq A map from the rate classes in the model to the frequency of
     * the rate class (as a String - see the introduction to this class).
     * Frequencies need not sum to one as they ae rescaled to do so.
     * @throws ModelException If the states in each of the rate classes are not
     * identical.
     */
    public Model(Map<RateCategory,String> freq) throws ModelException
    {
	//this.freq = freq;
        this.freq = new HashMap<>();
        for (Entry<RateCategory,String> e: freq.entrySet())
        {
            try
            {
                this.freq.put(e.getKey(),mp.compileFunction(e.getValue()));
            }
	    catch (NoSuchFunction ex)
	    {
		throw new ModelException("Frequency" +
			e.getValue() + ": No Such Function", ex);
	    }
	    catch (WrongNumberOfVariables ex)
	    {
		throw new ModelException("Frequency" +
			e.getValue() + ": Wromg Number of Variables for Function", ex);
	    }
        }

	f = new HashMap<>();

	nStates = -1;
	map = null;
	for (RateCategory r : freq.keySet())
	{
	    if ((nStates != -1) && (r.getNumberStates() != nStates))
	    {
		throw new ModelException("Rates have different number of states");
	    }
	    else
	    {
		nStates = r.getNumberStates();
	    }
	    if ((map != null) && (!r.getMap().equals(map)))
	    {
		throw new ModelException("Rates have different states");
	    }
	    else
	    {
		map = r.getMap();
	    }
	}
    }

    /**
     * Get the number of states represented in the model
     * @return The number of states in the model
     */
    public int getNumberStates()
    {
	return nStates;
    }

    /**
     * Gets a map from the rate name to its index in the rate matrix
     * Should it really be a method of Model rather than Rate
     * @return Map from rate name to index
     */
    public ArrayMap<String,Integer> getMap()
    {
	return map;
    }
    
    /**
     * Gets the set of all states in the model
     * @return The set of all states
     */
    public List<String> getStates()
    {
        return map.keyList();
    }
    
    /**
     * Gets the frquency of a rate class
     * @param r The rate class to get the frquency for
     * @return The frequency of the rate class
     */
    public double getFreq(RateCategory r)
    {
	return f.get(r);
    }

    /**
     * Sets the parameters of a model to the values contained in the
     * {@link Parameters.Parameters} data structure.
     * @param p The parameter values
     * @throws RateException If there is an error while setting the parameter
     * values for one of the rate classes.
     * @throws ModelException If there is an error while setting the parameter
     * values for the frequency of the rate classes.
     */
    public void setParameters(Parameters p) throws RateException, ModelException
    {
	HashMap<String,Double> values = p.getValues();
	for (RateCategory r: freq.keySet())
	{
            f.put(r,freq.get(r).compute(values));
	    /*try
	    {
		f.put(r,mp.parseEquation(freq.get(r), values));
	    }
	    catch (NoSuchFunction ex)
	    {
		throw new ModelException("Frequency" +
			freq.get(r) + ": No Such Function", ex);
	    }
	    catch (WrongNumberOfVariables ex)
	    {
		throw new ModelException("Frequency" +
			freq.get(r) + ": Wromg Number of Variables for Function", ex);
	    }*/
	}
	// Scale to total of 1.0
	double total = 0.0;
	for (double ff: f.values())
	{
	    total += ff;
	}
	for (RateCategory r:  freq.keySet())
	{
	    f.put(r, f.get(r) / total);
	}


	if (p.recalculateMatrix())
	{
	    total = 0.0;
	    for (RateCategory r :  freq.keySet())
	    {
		r.setParameters(p);
		total += f.get(r) * r.getTotalRate();
	    }
            if (rescale)
            {
                scale = 1.0/total;
                for (RateCategory r :  freq.keySet())
                {
                    r.setScale(scale);
                }
                p.calculated();
            }
	}
    }

    /**
     * Gets the scale - the value the rate matrices need to be multiplied by
     * to ensure an average rate of 1.
     * @return The scale
     */
    public double getScale()
    {
	return scale;
    }

    /**
     * Tests whether the model has a single rate class.
     * @return Whether this model has a single rate class.
     */
    public boolean hasSingleRate()
    {
	return (freq.keySet().size() == 1);
    }
    
    public Iterator<RateCategory> iterator()
    {
	return  freq.keySet().iterator();
    }

    /**
     * Gets a set of rate classes in the model
     * @return The set of rate classes in the model
     */
    public Set<RateCategory> getRates()
    {
	return  freq.keySet();
    }
    
    /**
     * Sets whether the matrix should be rescaled so the average rate of change
     * is one.  By default this is true as the rate will be confounded with branch
     * lengths if both are being estimated, however if branch lengths are fixed
     * rescaling may be inappropiate<br><br>
     * <b>Note: Particular care should be taken when deciding not to rescale to
     * ensure that parameters you are estimating are not confounded.</b>
     * @param rescale Whether to rescale
     */
    public void setRescale(boolean rescale)
    {
        this.rescale = rescale;
    }

    /**
     * Creates a new model with multiple rate classes distributed by a gamma
     * distribution (see Yang 1993) based on a single rate class
     * @param r The rate class to base the model on
     * @param gamma The value of gamma as a string (as it will be evaluated)
     * @param cats The number of categories, or rate classes, in the new model
     * @return The model
     */
    public static Model gammaRates(RateCategory r, String gamma, int cats) throws RateException
    {
	HashMap<RateCategory,String> freq = new HashMap<>();

	for (int i = 1; i <= cats; i++)
	{
	    RateCategory nr = r.multiplyBy("g[" + gamma + "," + i + "," + cats + "]");
            nr.setName("Gamma Category " + i);
	    freq.put(nr, Double.toString(1.0 / (double) cats));
	}

	try
	{
	    return new Model(freq);
	}
	catch (Exception e)
	{
            //As this code is constructing the model itself we shouldn't get any
            //error as it should create it properly!
	    throw new UnexpectedError(e);
	}
    }

    /**
     * Creates a new model from a file.  The first line controls the type of model.
     * Possible types and the subsquent format of the rest of the file are:
     * <ul>
     * <li><i>Gamma distributed rate categories</i>
     * <ul>
     * <li>First line should start "**G" (without the quotes) followed by a tab,
     * followed by the parameter name the alpha value is to be called by.
     * This should be followed a tab and the number of categories desired.</li>
     * <li>Second line should contain a file path to the RateCategory file that
     * describes the basic model.</li>
     * </ul></li>
     * <li><i>Equally likely rate categories
     * <ul>
     * <li>First line should contain "**E" (without the quotes)</li>
     * <li>Subsquent lines should each contain a file path to a RateCategory file
     * </ul></li>
     * <li><i>Given frequency rate categories
     * <ul>
     * <li>First line should contain "**F" (without the quotes)</li>
     * <li>Subsquent lines should each contain an equation describing the frequency
     * of that ratecategory (see {@link Maths.MathsParse} for the format of this
     * equation) followed by a tab followed by a file path to a RateCategoy file.
     * </ul></li>
     * </ul>
     * @param f The input file
     * @return The model
     * @throws InputException If there is an exception reading a file
     * @throws ModelException If there is a problem initialising the model
     * @throws Models.RateCategory.RateException If there is a problem with one of
     * the Rate Categories in the model
     */
    public static Model fromFile(File f) throws InputException, ModelException, RateException
    {
	BufferedReader in;
	try
	{
	    in = new BufferedReader(new FileReader(f));
	}
	catch (FileNotFoundException e)
 	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","File does not exist",e);
	}

	try
	{
	    String line = in.readLine();

	    Matcher m = gammaRE.matcher(line);
	    if (m.matches())
	    {
		line = in.readLine();
		in.close();
		return gammaRates(RateCategory.fromFile(new File(line)),
			m.group(2),
			Integer.parseInt(m.group(1)));
	    }
	    if (line.matches("^\\*\\*E"))
	    {
		HashSet<RateCategory> rates = new HashSet<>();
		while ((line = in.readLine()) != null)
		{
		    rates.add(RateCategory.fromFile(new File(line)));
		}
		HashMap<RateCategory,String> freq = new HashMap<>();
		for (RateCategory r: rates)
		{
		    freq.put(r,Double.toString(1.0/(double) rates.size()));
		}
		return new Model(freq);
	    }
	    if (line.matches("^\\*\\*F"))
	    {
		HashMap<RateCategory,String> freq = new HashMap<>();
		while ((line = in.readLine()) != null)
		{
		    String[] parts = line.split("\t+");
		    RateCategory r = RateCategory.fromFile(new File(parts[1]));
		    freq.put(r, parts[0]);
		}
		return new Model(freq);
	    }
	    throw new InputException(f.getAbsolutePath(),line,"Not a valid first line",null);
	}
	catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
    }

    private double scale;
    private int nStates;
    private Map<RateCategory,Double> f;
    //private Map<RateCategory,String> freq;
    private Map<RateCategory,CompiledFunction> freq;
    private ArrayMap<String,Integer> map;
    private boolean rescale = true;

    private static final Pattern gammaRE = Pattern.compile("^\\*\\*G\\s+(\\d+)\\s+(\\w+)");
    private static MathsParse mp = new MathsParse();
    
    /**
     * Exception thrown when there is a problem with the model
     */
    public class ModelException extends GeneralException
    {
        /**
         * Constructor when there is no underlying Throwable that caused the problem.
         * Currnetly used when there is a problem constructing the model,
         * e.g. different number of states in the RateClasses.
         * @param reason The reason for the exception
         */
        public ModelException(String reason)
        {
            super("Rates Exception\n\tReason:\t" + reason,null);
        }

        /**
         * Constructor when there is an underlying Throwable that caused the problem.
         * Currently used when the frequency for a RateClass can not be evaluated
         * @param reason The reason for the exception
         * @param cause The Throwable that caused the problem
         */
        public ModelException(String reason, Throwable cause)
        {
            super("Rates Exception\n\tReason:\t" + reason,cause);
        }
    }
}
