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

package Optimizers;

import Exceptions.InputException;
import Exceptions.OutputException;
import Likelihood.Calculator;
import Likelihood.Calculator.CalculatorException;
import Likelihood.Likelihood;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.TreeException;
import Utils.TimePassed;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implements a golden section search for parameter optimisation.  Each parameter
 * is optimised in turn until the difference in likelihood falls below a set value.
 * This value starts high and slowly decreased so as to not waste time highly
 * optomizing one parameter while other parameters may be highly sub-optimal.<br><br>
 * The amount of logging can be controlled as can the rigor used (the difference
 * in likelihood when the search stops).
 * @author Daniel Money
 * @version 1.3
 */
public class GoldenSection implements Optimizer
{
    /**
     * Constructor that uses default parameters for rigor and output level
     */
    public GoldenSection()
    {
	this(E_DIFF,ProgressLevel.NONE);
    }

    /**
     * Constrcutor which allows a user define output level
     * @param progresslevel The output level
     */
    public GoldenSection(ProgressLevel progresslevel)
    {
	this(E_DIFF,progresslevel);
    }

    /**
     * Constructor which allows a user defined rigor
     * @param rigor Rigor to be used
     */
    public GoldenSection(double rigor)
    {
	this(rigor,ProgressLevel.NONE);
    }

    /**
     * Constructor which allows a user defined rigor and output level
     * @param rigor Rigor to be used
     * @param progresslevel The output level
     */
    public GoldenSection(double rigor, ProgressLevel progresslevel)
    {
	this.rigor = rigor;
	this.progresslevel = progresslevel;
	cal = Calendar.getInstance();
	sdf = new SimpleDateFormat("ddHHmmss");
        timePassed = new TimePassed(365,TimeUnit.DAYS);
    }
    

    public Likelihood maximise(Calculator l, Parameters params) throws RateException, ModelException, TreeException, ParameterException, ParameterException, OutputException, CalculatorException
    {
	return maximise(l,System.out,new Data(params));
    }

    public Likelihood maximise(Calculator l, Parameters params, File log) throws RateException, ModelException, TreeException, ParameterException, ParameterException, OutputException, CalculatorException
    {
        try
        {
            PrintStream ps = new PrintStream(new FileOutputStream(log));
            Likelihood res = maximise(l,ps,new Data(params));
            ps.close();
            return res;
        }
        catch(FileNotFoundException ex)
        {
            throw new OutputException("Can't find log file", log.getAbsolutePath(), ex);
        }
    }    

    //See the Data class for a fuller description but effectively this stores
    //the state of the optimizer.  When created the parameters within it are
    //initalised.
    private Likelihood maximise(Calculator l, PrintStream out, Data data) throws RateException, ModelException, TreeException, ParameterException, OutputException, CalculatorException
    {
        //Don't keep Node Likelihoods while we are otimizing
        Likelihood.optKeepNL(false);
        //In this function two levels of progress output are the same so create
        //a boolean as to whether we're using one of those progress levels.
	boolean progress = (progresslevel == ProgressLevel.CALCULATION ||
                progresslevel == ProgressLevel.PARAMETER);

        //Reset the timer
        timePassed.reset();
        //Repeat optimizing all parameters individually until required rigor is reached.
	do
	{
            //If enough time has passed write a checkpoint.
            if (timePassed.hasPassed())
            {
                writeCheckPoint(data);
            }
	    if (progress)
	    {
		out.println("\t" + data.e_diff);
	    }
	    data.oldML = data.newML;
            
            Parameters np = new Parameters();
            np.addParameters(data.params);
            for (Parameter p : np)
	    //for (Parameter p : data.params)
	    {
                //For each estimated parameter maximise that parameter singularly
		if (p.getEstimate())
		{
		    double oldVal = p.getValue();
		    data.newML = maximiseSingle(data.params, p, l, data.diffs.get(p), data.e_diff, progresslevel, out);
                    //diff keeps track of the difference between two rounds of optimisation
                    //Two times this is used as an initial guess for boudning the area of the optima
                    //for the next round.
		    if (Math.abs(p.getValue() - oldVal) > 0.0)
		    {
			data.diffs.put(p, Math.abs(p.getValue() - oldVal));
		    }
		    if (progress)
		    {
			out.println(p.getName() + "\t" + p.getValue() + "\t" + data.newML.getLikelihood());
		    }
		}
	    }
            //e_diff keeps track of the current level of rigor.  We start with a low level
            //and increase it as our estimates get better.  If the difference between two
            //rounds of optimization is smaller than the rigor then make the rigor
            //tighter.
	    if ((data.oldML != null) && (data.newML.getLikelihood() - data.oldML.getLikelihood() < data.e_diff))
	    {
		data.e_diff = data.e_diff / 10;
	    }
	}
	while ((data.oldML == null) || ((data.e_diff >= rigor) || (data.newML.getLikelihood() - data.oldML.getLikelihood() > rigor)));
        //Now keep NodeLikelihoods and calculate the resulting NodeLikelihoods
        Likelihood.optKeepNL(true);
        return l.calculate(data.newML.getParameters());
	//return data.newML;
    }

    private Likelihood maximiseSingle(Parameters pp, Parameter p, Calculator l, double diff, double e_diff, ProgressLevel progresslevel, PrintStream out) throws RateException, ModelException, TreeException, ParameterException, CalculatorException
    {
        //Maximises a single parameter by golden section search
	boolean progress = (progresslevel == ProgressLevel.CALCULATION);
	Likelihood bestML = l.calculate(pp);
	Likelihood origML = bestML;
	Likelihood aML = bestML;
	Likelihood bML = bestML;
	double a;
	double b;
	double origVal = p.getValue();
        //Bound the area to search
	do
	{
            //Update the best ML foudn to date
            if (aML.getLikelihood() > bML.getLikelihood())
            {
                bestML = aML;
            }
            else
            {
                bestML = bML;
            }
            //Make the bounds twice as wide
	    diff = diff * 2;
            
            //Take accounts of any bounds on the parameter value
	    a = Math.max(origVal - diff, p.getLowerBound());
            //Set the parameter to the new value and calculate the likelihood
	    pp.setValue(p,a);
	    if (progress)
	    {
		out.println("\t\t1 " + p.getName() + "\t" + p.getValue());
	    }
	    aML = l.calculate(pp);
	    if (progress)
	    {
		cal.setTimeInMillis(System.currentTimeMillis());
		out.println("\t\t" + sdf.format(cal.getTime()) + "\t" + p.getName() + "\t" + p.getValue() + "\t" + aML.getLikelihood());
	    }
            //Do similarly for the other bound
	    b = Math.min(origVal + diff, p.getUpperBound());
	    pp.setValue(p,b);
	    if (progress)
	    {
		out.println("\t\t2 " + p.getName() + "\t" + p.getValue());
	    }
	    bML = l.calculate(pp);
	    if (progress)
	    {
		cal.setTimeInMillis(System.currentTimeMillis());
		out.println("\t\t" + sdf.format(cal.getTime()) + "\t" + p.getName() + "\t" + p.getValue() + "\t" + bML.getLikelihood());
	    }
	}
        //Repeat until we've bounded the optimal value
	while (((aML.getLikelihood() >= bestML.getLikelihood()) && (a > p.getLowerBound()))
		|| ((bML.getLikelihood() >= bestML.getLikelihood()) && (b < p.getUpperBound())));

        
        //Do standard golden section search
	double x1 = a + R * (b - a);
	double x2 = b - R * (b - a);

	pp.setValue(p,x1);
	Likelihood x1val = l.calculate(pp);
	pp.setValue(p,x2);
	Likelihood x2val = l.calculate(pp);

	while (Math.abs(x1val.getLikelihood() - x2val.getLikelihood()) > e_diff)
	{
	    if (x1val.getLikelihood() > x2val.getLikelihood())
	    {
		a = x2;
		x2 = x1;
		x2val = x1val;
		x1 = a + R * (b - a);
		pp.setValue(p,x1);
		if (progress)
		{
		    out.println("\t\t3 " + p.getName() + "\t" + p.getValue());
		}
		x1val = l.calculate(pp);
		if (progress)
		{
		    cal.setTimeInMillis(System.currentTimeMillis());
		    out.println("\t\t" + sdf.format(cal.getTime()) + "\t" + p.getName() + "\t" + p.getValue() + "\t" + x1val.getLikelihood());
		}
	    }
	    else
	    {
		b = x1;
		x1 = x2;
		x1val = x2val;
		x2 = b - R * (b - a);
		pp.setValue(p,x2);
		if (progress)
		{
		    out.println("\t\t4 " + p.getName() + "\t" + p.getValue());
		}
		x2val = l.calculate(pp);
		if (progress)
		{
		    cal.setTimeInMillis(System.currentTimeMillis());
		    out.println("\t\t" + sdf.format(cal.getTime()) + "\t" + p.getName() + "\t" + p.getValue() + "\t" + x2val.getLikelihood());
		}
	    }
	}

        //If golden section search has got us close to the lower bound on the
        //parameter check whether the lower bound is the optimal value
	if ((x1 - p.getLowerBound() <= diff))
	{
	    pp.setValue(p,p.getLowerBound());
	    if (progress)
	    {
		out.println("\t\t5 " + p.getName() + "\t" + p.getValue());
	    }
	    Likelihood bval = l.calculate(pp);
	    if (progress)
	    {
		cal.setTimeInMillis(System.currentTimeMillis());
		out.println("\t\t" + sdf.format(cal.getTime()) + "\t" + p.getName() + "\t" + p.getValue() + "\t" + bval.getLikelihood());
	    }
	    if (bval.getLikelihood() > x1val.getLikelihood())
	    {
		x1val = bval;
		x1 = p.getLowerBound();
	    }
	}

        //And similarly for the upper bound
	if ((p.getUpperBound() - x2 <= diff))
	{
	    if (progress)
	    {
		out.println("\t\t6 " + p.getName() + "\t" + p.getValue());
	    }
	    pp.setValue(p,p.getUpperBound());
	    Likelihood bval = l.calculate(pp);
	    if (progress)
	    {
		cal.setTimeInMillis(System.currentTimeMillis());
		out.println("\t\t" + sdf.format(cal.getTime()) + "\t" + p.getName() + "\t" + p.getValue() + "\t" + bval.getLikelihood());
	    }
	    if (bval.getLikelihood() > x2val.getLikelihood())
	    {
		x2val = bval;
		x2 = p.getUpperBound();
	    }
	}

	//If the best we've found is actually worse than the value we started
	//with (because we haven't checked the start value again) then return
	//the start value.
	if ((x1val.getLikelihood() < origML.getLikelihood()) && (x2val.getLikelihood() < origML.getLikelihood()))
	{
	    pp.setValue(p,origVal);
	    return origML;
	}
        //Else return the best value we've found.
	if (x1val.getLikelihood() > x2val.getLikelihood())
	{
	    pp.setValue(p,x1);
	    return x1val;
	}
	else
	{
	    pp.setValue(p,x2);
	    return x2val;
	}
    }
    
    public Likelihood restart(Calculator l, File checkPoint) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, OptimizerException, CalculatorException
    {
        return restart(l, checkPoint, System.out);
    }
    
    public Likelihood restart(Calculator l, File checkPoint, File log) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, OptimizerException, CalculatorException
    {
        try
        {
            PrintStream ps = new PrintStream(new FileOutputStream(log));
            Likelihood res = restart(l,checkPoint,ps);
            ps.close();
            return res;
        }
        catch(FileNotFoundException ex)
        {
            throw new OutputException("Can't find log file", log.getAbsolutePath(), ex);
        }
    }   
    
    private Likelihood restart(Calculator l, File f, PrintStream out) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, CalculatorException
    {
        Object o;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            o = ois.readObject();
        }
        catch (FileNotFoundException ex)
        {
            throw new InputException("Checkpoint file not found","N/A",f.getAbsolutePath(),ex);
        }
        catch (IOException ex)
        {
            throw new InputException("Error reading checkpoint","N/A",f.getAbsolutePath(),ex);
        }
        catch (ClassNotFoundException ex)
        {
            throw new InputException("Serilaization error reading check point -"
                    + "probably using a different version","N/A",f.getAbsolutePath(),ex);            
        }
        if (o instanceof Data)
        {
            return maximise(l,out,(Data) o);
        }
        else
        {
            throw new InputException("File does not appear to be a checkpoint file","N/A",
                    f.getAbsolutePath(),null);
        }
    }
    
    public void setCheckPointFile(File checkPoint) throws OptimizerException
    {
        this.checkPoint = checkPoint;
    }
    
    public void setCheckPointFrequency(int num, TimeUnit unit) throws OptimizerException
    {
        timePassed = new TimePassed(num, unit);
    }
    
    private void writeCheckPoint(Data data) throws OutputException
    {
        if (checkPoint != null)
        {
            try
            {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(checkPoint));
                oos.writeObject(data);
                oos.close();
            }
            catch (FileNotFoundException ex)
            {
                throw new OutputException(checkPoint.getAbsolutePath(),
                        "Unable to find check point file",
                        ex);
            }
            catch (IOException ex)
            {
                throw new OutputException(checkPoint.getAbsolutePath(),
                        "Unable to write to check point file",
                        ex);
            }
        }        
    }

    private double rigor;
    private ProgressLevel progresslevel;

    private static final double R = (Math.sqrt(5.0) - 1) / 2;
    private static final double E_DIFF = 10e-7;
    private Calendar cal;
    private SimpleDateFormat sdf;
    private File checkPoint;
    private TimePassed timePassed;
    
    /**
     * Enumeration of the different levels of output
     */
    public enum ProgressLevel
    {

        /**
         * No output
         */
        NONE,
        /**
         * Output new value and likelihood every time a parameter is optimised
         */
        PARAMETER,
        /**
         * Output value and likelihood for every likelihood calculation
         */
        CALCULATION
    }

    //This class stores various parameters that describe the stae of optimization.
    //It is written out as a checkpoint and can be read back in to restart the
    //optimization.
    private static class Data implements Serializable
    {
        //Constructer initalises various parameters.
        private Data(Parameters p)
        {
            params = p.clone();
            e_diff = 10.0;
            diffs = new HashMap<>();
            for (Parameter pp : this.params)
            {
                diffs.put(pp, 2.0);
            }
            oldML = null;
            newML = null;
        }
        
        private Parameters params;
        private double e_diff;
        HashMap<Parameter, Double> diffs;
        Likelihood oldML;
	Likelihood newML;
    }
    
    private static final long serialVersionUID = 1;
}

