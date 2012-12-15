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
import Likelihood.Calculator.CalculatorException;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Likelihood.SiteLikelihood;
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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Implements Nelder-Mead parameter optimization. <BR><BR>
 * Progress information can optionally be printed to either the screen or a file.
 * @author Daniel Money
 * @version 2.0
 */
public class NelderMead implements Optimizer
{
    /**
     * Constrcutor that defaults to no debug output
     */
    public NelderMead()
    {
	this(DebugLevel.OFF);
    }

    /**
     * Constructor that allows user defined debug level
     * @param debug Debug level
     */
    public NelderMead(DebugLevel debug)
    {
	this.debug = debug;
        timePassed = new TimePassed(365,TimeUnit.DAYS);
    }
    
    public <R extends Likelihood> R maximise(Calculator<R> l, Parameters params) throws RateException, ModelException, TreeException, ParameterException, ParameterException, OutputException, CalculatorException
    {
	return maximise(l,System.out,new Data(params,l));
    }

    public <R extends Likelihood> R maximise(Calculator<R> l, Parameters params, File log) throws RateException, ModelException, TreeException, ParameterException, ParameterException, OutputException, CalculatorException
    {
        try
        {
            PrintStream ps = new PrintStream(new FileOutputStream(log));
            R res = maximise(l,ps,new Data(params,l));
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
    private <R extends Likelihood> R maximise(Calculator<R> l, PrintStream out, Data data) throws RateException, ModelException, TreeException, ParameterException, OutputException, CalculatorException
    {
        //Don't keep Node Likelihoods while we are otimizing
        SiteLikelihood.optKeepNL(false);
        //Reset the timer
        timePassed.reset();
	do
	{
            //If enough time has passed write a checkpoint.
            if (timePassed.hasPassed())
            {
                writeCheckPoint(data);
            }
            
            //Do a round of optimization.  Follows the algorithm in
            //http://www.uib.no/med/avd/miapr/arvid/MRI2000/chap4_hamre.pdf
	    int imin = minIndex(data.values);
	    data.imax = maxIndex(data.values);
	    data.vold = data.values[data.imax];

	    double[] xmax = data.simplexes[data.imax];

	    double[] xhat = new double[data.num];
	    for (int i=0; i < xhat.length; i++)
	    {
		double v = -xmax[i];
		for (int j=0; j < data.num+1; j++)
		{
		    v = v + data.simplexes[j][i];
		}
		xhat[i] = v / data.num;
	    }
	    double[] xref = new double[data.num];
	    for (int i=0; i < xref.length; i++)
	    {
		xref[i] = 2 * xhat[i] - xmax[i];
	    }
	    R vref = evaluate(xref,data,l);
	    double[] xnew = new double[data.num];
	    if (-data.values[imin].getLikelihood() > -vref.getLikelihood())
	    {
		//*Expanding;
		double[] xexp = new double[data.num];
		for (int i=0; i < xref.length; i++)
		{
		    xexp[i] = 2 * xref[i] - xhat[i];
		}
		R vexp = evaluate(xexp,data,l);
		if (-vexp.getLikelihood() <= -vref.getLikelihood())
		{
		    xnew = xexp;
		}
		else
		{
		    xnew = xref;
		}
	    }
	    else
	    {
		double mxi = -Double.MAX_VALUE;
		for (int i = 0; i < data.values.length; i++)
		{
		    if ((i != data.imax) && (-data.values[i].getLikelihood() > mxi))
		    {
			mxi = -data.values[i].getLikelihood();
		    }
		}
		if ((mxi > -vref.getLikelihood()) && (-vref.getLikelihood() >= -data.values[imin].getLikelihood()))
		{
		    //*Reflecting
		    xnew = xref;
		}
		else
		{
		    if (-vref.getLikelihood() > mxi)
		    {
			//*Contraction;
			if (-data.values[data.imax].getLikelihood() <= -vref.getLikelihood())
			{
			    for (int i = 0; i < xnew.length; i++)
			    {
				xnew[i] = 0.5 * (xmax[i] + xhat[i]);
			    }
			}
			else
			{
			    for (int i = 0; i < xnew.length; i++)
			    {
				xnew[i] = 0.5 * (xref[i] + xhat[i]);
			    }
			}
		    }
		}
	    }

	    data.vnew = evaluate(xnew,data,l);

	    if (-data.vnew.getLikelihood() > -data.values[data.imax].getLikelihood())
	    {
		//*Shrinking
		for (int i = 0; i < data.num + 1; i++)
		{
		    for (int j = 0; j < data.num; j ++)
		    {
			data.simplexes[i][j] = (data.simplexes[i][j] + data.simplexes[imin][j]) / 2;
		    }
		}
		for (int i=0; i < data.num+1; i++)
		{
		    data.values[i] = evaluate(data.simplexes[i],data,l);
                    if (-data.values[i].getLikelihood() < -data.vnew.getLikelihood())
                    {
                        data.vnew = data.values[i];
                    }
		}
	    }
	    else
	    {
		data.simplexes[data.imax] = xnew;
		data.values[data.imax] = data.vnew;
	    }

	    if (debug == DebugLevel.ON)
	    {
		out.println(data.vnew.getLikelihood());
		out.println("\t" + Arrays.toString(xnew));
	    }
	}
	while (-data.vold.getLikelihood() - -data.vnew.getLikelihood() > tol);

        //Now keep NodeLikelihoods and calculate the resulting NodeLikelihoods
        SiteLikelihood.optKeepNL(true);
        return l.calculate(data.vnew.getParameters());
    }

    private static <R extends Likelihood> R evaluate(double[] params, Data data, Calculator<R> l) throws RateException, ModelException, TreeException, ParameterException, CalculatorException
    {
	for (int i = 0; i < params.length; i++)
	{
	    if ((data.oldp == null) || (data.oldp[i] != params[i]))
	    {
		double v = params[i];
		v = Math.max(data.map[i].getLowerBound(), v);
		v = Math.min(data.map[i].getUpperBound(), v);
		params[i] = v;
		data.params.setValue(data.map[i], params[i]);
	    }
	}
	data.oldp = Arrays.copyOf(params, params.length);

        return l.calculate(data.params);

    }


    private int minIndex(Likelihood[] a)
    {
        //Returns the index in the array of the likelihood object with the lowest
        //likelihood
	double min = Double.MAX_VALUE;
	int index = 0;
	for (int i = 0; i < a.length; i ++)
	{
	    if (-a[i].getLikelihood() < min)
	    {
		min = -a[i].getLikelihood();
		index = i;
	    }
	}
	return index;
    }

    private int maxIndex(Likelihood[] a)
    {
        //Returns the index in the array of the likelihood object with the highest
        //likelihood
	double max = -Double.MAX_VALUE;
	int index = 0;
	for (int i = 0; i < a.length; i ++)
	{
	    if (-a[i].getLikelihood() > max)
	    {
		max = -a[i].getLikelihood();
		index = i;
	    }
	}
	return index;
    }
    
    public <R extends Likelihood> R restart(Calculator<R> l, File checkPoint) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, OptimizerException, CalculatorException
    {
        return restart(l, checkPoint, System.out);
    }
    
    public <R extends Likelihood> R restart(Calculator<R> l, File checkPoint, File log) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, OptimizerException, CalculatorException
    {
        try
        {
            PrintStream ps = new PrintStream(new FileOutputStream(log));
            R res = restart(l,checkPoint,ps);
            ps.close();
            return res;
        }
        catch(FileNotFoundException ex)
        {
            throw new OutputException("Can't find log file", log.getAbsolutePath(), ex);
        }
    }   
    
    private <R extends Likelihood> R restart(Calculator<R> l, File f, PrintStream out) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, CalculatorException
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

    private static double tol = 1e-8;
    private DebugLevel debug;
    private File checkPoint;
    private TimePassed timePassed;
    
    /**
     * Enumeration of the debug level
     */
    //Done this way for future expansion
    public enum DebugLevel
    {

        /**
         * Debug is on
         */
        ON,
        /**
         * Debug is off
         */
        OFF
    }
    
    //This class stores various parameters that describe the stae of optimization.
    //It is written out as a checkpoint and can be read back in to restart the
    //optimization.
    private static class Data implements Serializable
    {
        //Constructer initialises various parameters.
        private <R extends Likelihood> Data(Parameters p, Calculator<R> l) throws RateException, ModelException, TreeException, ParameterException, CalculatorException
        {
            params = p.clone();
            num = params.numberEstimate();
            map = new Parameter[num];
            
            int mi = 0;
            for (Parameter pa: params)
            {
                if (pa.getEstimate())
                {
                    map[mi] = pa;
                    mi++;
                }
            }
            
            simplexes = new double[num+1][num];
            values = new Likelihood[num+1];

            for (int i=0; i < num; i++)
            {
                for (int j=0; j < num; j++)
                {
                    if (i == j)
                    {
                        simplexes[i][j] = 2.0;
                    }
                    else
                    {
                        simplexes[i][j] = 0.5;
                    }
                }
            }
            for (int i=0; i < num; i++)
            {
                simplexes[num][i] = 1.0;
            }

            for (int i=0; i < num+1; i++)
            {
                values[i] = evaluate(simplexes[i],this,l);
            }
        }
        
        private Parameters params;
        private Parameter[] map;
        private double[][] simplexes;
        private Likelihood[] values;
        private int num;
        private Likelihood vnew, vold;
	private int imax;
        private double[] oldp;
        
        private static final long serialVersionUID = 1;
    }
}
