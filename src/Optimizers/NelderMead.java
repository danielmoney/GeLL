package Optimizers;

import Exceptions.InputException;
import Exceptions.OutputException;
import Likelihood.Calculator;
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
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Implements Nelder-Mead parameter optimization. <BR><BR>
 * Progress information can optionally be printed to either the screen or a file.
 * @author Daniel Money
 * @version 1.0
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
    
    public Likelihood maximise(Calculator l, Parameters params) throws RateException, ModelException, TreeException, ParameterException, ParameterException, OutputException
    {
	return maximise(l,System.out,new Data(params,l));
    }

    public Likelihood maximise(Calculator l, Parameters params, File log) throws RateException, ModelException, TreeException, ParameterException, ParameterException, OutputException
    {
        try
        {
            PrintStream ps = new PrintStream(new FileOutputStream(log));
            Likelihood res = maximise(l,ps,new Data(params,l));
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
    private Likelihood maximise(Calculator l, PrintStream out, Data data) throws RateException, ModelException, TreeException, ParameterException, OutputException
    {
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
	    Likelihood vref = evaluate(xref,data,l);
	    double[] xnew = new double[data.num];
	    if (-data.values[imin].getLikelihood() > -vref.getLikelihood())
	    {
		//*Expanding;
		double[] xexp = new double[data.num];
		for (int i=0; i < xref.length; i++)
		{
		    xexp[i] = 2 * xref[i] - xhat[i];
		}
		Likelihood vexp = evaluate(xexp,data,l);
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

	return data.vnew;
    }

    private static Likelihood evaluate(double[] params, Data data, Calculator l) throws RateException, ModelException, TreeException, ParameterException
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
    
    public Likelihood restart(Calculator l, File checkPoint) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, OptimizerException
    {
        return restart(l, checkPoint, System.out);
    }
    
    public Likelihood restart(Calculator l, File checkPoint, File log) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException, OptimizerException
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
    
    private Likelihood restart(Calculator l, File f, PrintStream out) throws RateException, ModelException, TreeException, ParameterException, ParameterException, InputException, OutputException
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
        private Data(Parameters p, Calculator l) throws RateException, ModelException, TreeException, ParameterException
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
