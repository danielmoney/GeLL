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

import Exceptions.GeneralException;
import Exceptions.InputException;
import Exceptions.OutputException;
import Likelihood.Likelihood;
import Likelihood.SiteLikelihood;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Implements the Conjugate Gradient method of parameter optimisation. <BR><BR>
 * This is heavily based on the <a href=http://code.google.com/p/beast-mcmc/>
 * BEAST implementation</a> <BR><BR>
 * Progress information can optionally be printed to either the screen or a file.
 * @author Daniel Money
 * @version 2.0
 */
public class ConjugateGradient implements Optimizer
{
    /**
     * Constructor that uses default values for rigour (1e-7), update method
     * (BEALE_SORENSON_HESTENES_STIEFEL) and output (none)
     */
    public ConjugateGradient()
    {
        this(E_DIFF,Update.BEALE_SORENSON_HESTENES_STIEFEL,ProgressLevel.NONE);
    }
    
    /**
     * Constructor that uses the given value for rigour and default values for update method
     * (BEALE_SORENSON_HESTENES_STIEFEL) and output (none)
     * @param tol The toelrance to use
     */
    public ConjugateGradient(double tol)
    {
        this(tol,Update.BEALE_SORENSON_HESTENES_STIEFEL,ProgressLevel.NONE);
    }
    
    /**
     * Constructor that uses the givemn update method and default values for rigour (1e-7)
     * and output (none)
     * @param update The update method to use
     */
    public ConjugateGradient(Update update)
    {
        this(E_DIFF,update,ProgressLevel.NONE);
    }
    
    /**
     * Constructor that uses the given tolerance and update method but
     * the default output (none)
     * @param tol The tolerance to use
     * @param update The update method to use
     */
    public ConjugateGradient(double tol, Update update)
    {
        this(tol,update,ProgressLevel.NONE);
    }
    
    /**
     * Constructor that uses the given output level but default values for rigour (1e-7)
     * and update method (BEALE_SORENSON_HESTENES_STIEFEL) 
     * @param progressLevel The output level to use
     */
    public ConjugateGradient(ProgressLevel progressLevel)
    {
        this(E_DIFF,Update.BEALE_SORENSON_HESTENES_STIEFEL,progressLevel);
    }
    
    /**
     * Constructor that uses the given tolerance and output level but the default
     * update method (BEALE_SORENSON_HESTENES_STIEFEL)
     * @param tol The tolerance to use
     * @param progressLevel The output level to use
     */
    public ConjugateGradient(double tol, ProgressLevel progressLevel)
    {
        this(tol,Update.BEALE_SORENSON_HESTENES_STIEFEL,progressLevel);
    }
    
    /**
     * Constructor that uses the given update method
     * (BEALE_SORENSON_HESTENES_STIEFEL) and output (none) but 
     * the default values for rigour (1e-7),
     * @param update the update method
     * @param progressLevel The output level to use
     */
    public ConjugateGradient(Update update, ProgressLevel progressLevel)
    {
        this(E_DIFF,update,progressLevel);
    }

    /**
     * Constructor that uses the given values for rigour, update method
     * and output
     * @param tol The tolerance to use
     * @param update The update method to use
     * @param progressLevel The output level to use
     */
    public ConjugateGradient(double tol, Update update, ProgressLevel progressLevel)
    {
        this.update = update;
        this.tol = tol;
        this.progressLevel = progressLevel;
        timePassed = new TimePassed(365,TimeUnit.DAYS);
        maxPassed = new TimePassed(365,TimeUnit.DAYS);
    }
    
    public <R extends Likelihood> R  maximise(Optimizable<R> c, Parameters p) throws GeneralException
    {
        return maximise(c,System.out,new Data(c,p));
    }

    public <R extends Likelihood> R maximise(Optimizable<R> c, Parameters p, File log) throws GeneralException
    {
        try
        {
            PrintStream ps = new PrintStream(new FileOutputStream(log));
            R res = maximise(c,ps,new Data(c,p));
            ps.close();
            return res;
        }
        catch(FileNotFoundException ex)
        {
            throw new OutputException("Can't find log file", log.getAbsolutePath(), ex);
        }
    }

    private <R extends Likelihood> R maximise(Optimizable<R> c, PrintStream out, Data d) throws GeneralException
    {
        // Don't keep the node likelihoods while we are optimizing
        SiteLikelihood.optKeepNL(false);
        // Reset the timer
        timePassed.reset();
        maxPassed.reset();
        
        do
        {
            // If enough time has passed write a checkpoint.
            if (timePassed.hasPassed())
            {
                writeCheckPoint(d);
            }
            if (maxPassed.hasPassed())
            {
                throw new OptimizerException("Maximum time has passed");
            }
            
            // Output appropiate status
            switch (progressLevel)
            {
                case DETAIL:
                    out.println("*Slope\t\t" + d.slope);
                    out.println("*Direction\t " + d.direction);
                    out.println("*Old Gradient\t* " + d.oldGradient);
                    out.println("*New Gradient\t* " + d.newGradient);
                    out.println("*Step\t\t* " + d.step);
                case PARAMETERS:
                    out.println(d.params.toString(false));
                case LIKELIHOOD:
                    out.println(-d.newML);                     
            }            
            
            // Determine an appropriate step length
            d.step = findStep(c, d.params, d.direction, d.slope, d.step);

            // Update our current guess as to the parameters
            d.params = getPoint(d.params, d.direction, d.step);

            // Store the likelihood for the old parameters
            d.oldML = d.newML;
            // And calculate the likelihood for the new parameters
            d.newML = -c.calculate(d.params).getLikelihood();

            // Compute numerical gradient
            d.newGradient = gradient(c, d.params, d.newML);

            // Determine new search direction
            d.direction = conjugateGradientDirection(d.newGradient, d.oldGradient, d.direction, d.params, update);
            // Check we're not heading towards any boundries and update the
            // direction to avoid this
            d.direction = checkDirection(d.params, d.direction);

            // Compute the slope in new direction
            d.slope = slope(d.direction, d.newGradient);

            // If the slope is greater than 0 then revert to steepest descent
            if (d.slope >= 0)
            {
                // Calculate the steepest descent direction
                d.direction = steepestDescentDirection(d.newGradient, d.params);

                // Compute the slope in thisdirection
                d.slope = slope(d.direction, d.newGradient);

                // Reset to default step length
                d.step = 1.0;
            }

            // Store the old gradient
            d.oldGradient = new HashMap<>(d.newGradient);
        }
        // Do this until we have only a small increase in likelihood between steps
        while (Math.abs(d.newML - d.oldML) > tol);

        // Now store the node likelihoods
        SiteLikelihood.optKeepNL(true);
        // And relcaulate the likelihood (storing the node likelihoods) for the
        // optimized parameters
        return c.calculate(d.params);
    }
    
    public <R extends Likelihood> R  restart(Optimizable<R> l, File checkPoint) throws GeneralException
    {
        return restart(l, checkPoint, System.out);
    }
    
    public <R extends Likelihood> R  restart(Optimizable<R> l, File checkPoint, File log) throws GeneralException
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
    
    private <R extends Likelihood> R  restart(Optimizable<R> l, File f, PrintStream out) throws GeneralException
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
    
    public void setMaximumRunTime(int num, TimeUnit unit) throws OptimizerException
    {
        maxPassed = new TimePassed(num, unit);
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
    
    private static double findStep(Optimizable c, Parameters params, Map<String, Double> direction, double grad1, double lastStep) throws GeneralException
    {
        // Calculate the maximum step size without hititng a boundry
        double maxStep = getMaxStep(params, direction);
        // Not entirely sure we should ever hit these but they're in the BEAST
        // code and I'm not confident enought to remove them.
        if (maxStep <= 0 || grad1 == 0)
        {
            return 0.0;
        }

        // First attempt to bracket the minimum.  x1 is 0 and x2 a multiple of the
        // last step size (taken from BEAST)
        double x1 = 0;
        //The past grad1 is the gradient at x = 0
        double x2 = lastStep * 1.25;        
        // If x2 is larger than the max step size make it a multiple of the max step size.
        // 0.5 is taken from BEAST.
        if (x2 > maxStep)
        {
            x2 = maxStep * 0.5;
        }
        //Find the gradient at x2
        double grad2 = gradient(c, params, direction, x2);

        // Gradient at x1 is negative so we need to find a positive gradient to
        // know we've bracketed the minimum.  If we haven't found one yet, change
        // our bracketing attempt.  x1 must still be negative so set that as the
        // new lower boudn and make a new upper bound of twice that.  Again 2.0
        // is taken from BEAST.
        while (grad2 <= 0 && x2 < maxStep)
        {
            //Set x1 to x2 and the same for the gradient
            x1 = x2;
            grad1 = grad2;
            x2 = x2 * 2.0;
            // If x2 is going to be lager than the max step then set it to the max step
            if (x2 > maxStep)
            {
                x2 = maxStep;
            }
            // Calculate the gradient at x2
            grad2 = gradient(c, params, direction, x2);
        }

        if (grad2 <= 0)
        {
            // If grad2 < 0 we're at boundry so simply return the maxStep
            return x2;
        }
        else
        {
            // We know the minimum is between x1 and x2.  Use linear interpolation
            // to guess the value of x at the minimum.  Reduces to this as the gradient
            // we're interest in is 0.
            return (x1 * grad2 - x2 * grad1) / (grad2 - grad1);
        }
    }
    
    private static Map<String, Double> conjugateGradientDirection(Map<String, Double> newGradient, Map<String, Double> oldGradient, Map<String, Double> direction, Parameters params, Update update) throws ParameterException
    {
        // Calculates the conjugate gradient direction using one of three
        // different methods.  Algorithm pretty much lifted straight from BEAST.
        
        Map<String, Double> newdir = new HashMap<>();
        double gg = 0;
        double dgg = 0;
        for (Entry<String, Double> e : newGradient.entrySet())
        {
            if (isActive(params.getParam(e.getKey()),newGradient))
            {
                switch (update)
                {
                    case FLETCHER_REEVES:
                        dgg += e.getValue() * e.getValue();
                        gg += oldGradient.get(e.getKey()) * oldGradient.get(e.getKey());
                        break;

                    case POLAK_RIBIERE:
                        dgg += e.getValue() * (e.getValue() - oldGradient.get(e.getKey()));
                        gg += oldGradient.get(e.getKey()) * oldGradient.get(e.getKey());
                        break;

                    case BEALE_SORENSON_HESTENES_STIEFEL:
                        dgg += e.getValue() * (e.getValue() - oldGradient.get(e.getKey()));
                        gg += direction.get(e.getKey()) * (e.getValue() - oldGradient.get(e.getKey()));
                        break;
                }
            }
        }
        double beta = dgg / gg;
        if (beta < 0 || gg == 0)
        {
            // Better convergence (Gilbert and Nocedal)
            beta = 0;
        }
        for (Entry<String, Double> e : newGradient.entrySet())
        {
            if (isActive(params.getParam(e.getKey()),newGradient))
            {
                newdir.put(e.getKey(), -e.getValue() + beta * direction.get(e.getKey()));
            }
            else
            {
                newdir.put(e.getKey(),0.0);
            }
        }
        return newdir;
    }

    private static Map<String, Double> steepestDescentDirection(Map<String, Double> gradient, Parameters params) throws ParameterException
    {
        // Calculates the steeest descent direction unless we're near a boundry
        // for a parameter in which case the direction is set to 0 for that 
        // parameter
        Map<String, Double> ret = new HashMap<>();
        for (Entry<String, Double> e : gradient.entrySet())
        {
            // Check whether the parameter is near a boundry
            if (isActive(params.getParam(e.getKey()),gradient))
            {
                ret.put(e.getKey(), -e.getValue());
            }
            else
            {
                ret.put(e.getKey(),0.0);
            }
        }
        return ret;
    }

    private static double slope(Map<String, Double> direction, Map<String, Double> gradient)
    {
        //Calculates the slope given the direction of search and the gradient at that point
        double s = 0;
        for (Entry<String, Double> e : gradient.entrySet())
        {
            s += e.getValue() * direction.get(e.getKey());
        }
        return s;
    }

    private static Map<String, Double> gradient(Optimizable c, Parameters params, double l) throws GeneralException
    {
        // Calculates the gradient for a given point.  l is the likelihood at
        // that point (which saves us calculating it again).
        Map<String, Double> grad = new HashMap<>();
        Parameters ptemp = params.clone();
        for (Parameter p : ptemp)
        {
            // Don't bother with fixed parameters!
            if (p.getEstimate())
            {
                double oldv = p.getValue();
                // BEAST uses the centered first derivative here by also calculatng
                // a fxminus for oldv - SMALL_DIFF.  This however results in a lot
                // of extra likelihood calculations so we use oldv and oldv + 
                // SMALL_DIFF instead. (Unless we're near the upper bound in which
                // case use oldv - SMALL_DIFF and oldv.  Evedience suggests this is 
                // quicker.
                if (p.getValue() + SMALL_DIFF > p.getUpperBound())
                {
                    ptemp.setValue(p, oldv - SMALL_DIFF);
                    double fxminus = -c.calculate(ptemp).getLikelihood();
                                    ptemp.setValue(p, oldv);
                    grad.put(p.getName(), (l - fxminus) / SMALL_DIFF);             
                }
                else
                {
                    ptemp.setValue(p, oldv + SMALL_DIFF);
                    double fxplus = -c.calculate(ptemp).getLikelihood();
                                    ptemp.setValue(p, oldv);
                    grad.put(p.getName(), (fxplus - l) / SMALL_DIFF);
                }

                ptemp.setValue(p, oldv);
            }
        }
        return grad;
    }

    private static Parameters getPoint(Parameters params, Map<String, Double> Difference, double distance) throws ParameterException
    {
        // Gets a point a given distance away from the original settings in
        // the given direction 
        Parameters newParams = params.clone();
        for (Parameter p : newParams)
        {
            // Only worry about parameters we're estimating
            if (p.getEstimate())
            {
                double nv = p.getValue() + distance * Difference.get(p.getName());
                // Safety checks to ensure we don't set a parameter to an invalid
                // value (these are actually needed because of roudning)
                nv = Math.min(nv, p.getUpperBound());
                nv = Math.max(nv, p.getLowerBound());
                newParams.setValue(p, nv);
            }
        }
        return newParams;
    }

    private static double gradient(Optimizable c, Parameters params, Map<String, Double> direction, double distance) throws GeneralException
    {
        // Calculates the gradient in the given direction for a point at the given 
        // distance from the current point (params) in the same direction
        
        // Use diff and diff - SMALL_DIFF as if diff is set to the maximum possible 
        // step then diff + SMALL_DIFF will result in an invalid parameter value
        double u = -c.calculate(getPoint(params, direction, distance)).getLikelihood();
        double l = -c.calculate(getPoint(params, direction, distance - SMALL_DIFF)).getLikelihood();
        return (u - l) / SMALL_DIFF;
    }

    private static double getMaxStep(Parameters params, Map<String, Double> direction)
    {
        // Calculates the maximum step that can be taken in the given direction
        // before hitting a boundry
        
        // Initalise to absolute maximum value
        double maxStep = Double.MAX_VALUE;
        //For each parameter
        for (Parameter p : params)
        {            
            // If it's a parameter we're estimating...
            if (p.getEstimate())
            {
                // Get the direction for this paramter
                double dir = direction.get(p.getName());
                // If dir < 0 then we must be heading towards the lower bound
                if (dir < 0)
                {
                    maxStep = Math.min(maxStep,(p.getLowerBound() - p.getValue()) / dir);
                }
                // If dir > 0 then we must be heading towards the upper bound
                if (dir > 0)
                {
                    maxStep = Math.min(maxStep,(p.getUpperBound() - p.getValue()) / dir);
                }
                // Ignore dir = 0 as then we're not going anywhere!
            }
        }
        return maxStep;
    }

    private static Map<String, Double> checkDirection(Parameters params, Map<String, Double> direction)
    {
        // Checks, for each parameter, that we are not near a boundry and heading
        // towards it.  If so set that direction to zero so that we don't approach
        // nearer.
        Map<String, Double> newdir = new HashMap<>(direction);
        for (Parameter p : params)
        {
            // Check we're not heading towards a lower boundry
            if ((p.getValue() <= p.getLowerBound() + SMALL_DIFF) && (newdir.get(p.getName()) < 0))
            {
                newdir.put(p.getName(), 0.0);
            }
            // Check we're not heading towards an upper boundry
            if ((p.getValue() >= p.getUpperBound() - SMALL_DIFF) && (newdir.get(p.getName()) > 0))
            {
                newdir.put(p.getName(), 0.0);
            }
        }
        return newdir;
    }

    private static boolean isActive(Parameter params, Map<String, Double> gradient)
    {
        // Checks to see if the given parameter is near a boudnry and heading
        // towards the boundry
        
        // Check the lower boundry
        if ((params.getValue() <= params.getLowerBound() + SMALL_DIFF) && (gradient.get(params.getName()) > 0))
        {
            return false;
        }
        
        // Check the upper boundry
        if ((params.getValue() >= params.getUpperBound() - SMALL_DIFF) && (gradient.get(params.getName()) < 0))            
        {
            return false;
        }
        return true;
    }
    
    private ProgressLevel progressLevel;
    private Update update;
    private double tol;
    private File checkPoint;
    private TimePassed timePassed;
    private TimePassed maxPassed;
    
    private static double SMALL_DIFF = 1e-10;
    private static final double E_DIFF = 1e-6;
    
    /**
     * Enumeration of the different update methods
     */
    public enum Update
    {
        /**
         * Use the Fletcher-Reevs method
         */
        FLETCHER_REEVES,
        /**
         * Use the Polak-Ribiere method
         */
        POLAK_RIBIERE,
        /**
         * Use the Beale-Sorenson-Hestenes-Stiefel method
         */
        BEALE_SORENSON_HESTENES_STIEFEL
    }
    
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
         * Output just the likelihood after each itteration
         */
        LIKELIHOOD,
        /**
         * Output the likeihood and parameter values after each itteration
         */
        PARAMETERS,
        /**
         * Output lots of information - including direction vectors etc
         */
        DETAIL
    }
    
    private static class Data implements Serializable
    {
        //Constructer initalises various parameters.
        private Data(Optimizable c, Parameters p) throws GeneralException
        {
            // Clone the parameters just to be sure we don't destroy the input
            // params
            params = p.clone();

            // Calculate the likelihood for our current guess (which will be the
            // satrting value for each parameter.
            newML = -c.calculate(params).getLikelihood();
            // Calcualte the gradient at this guess
            newGradient = gradient(c, params, newML);
            // Store this gradient as the old gradient
            oldGradient = newGradient;

            // Calculate an initial search direction using steepest descent
            direction = steepestDescentDirection(newGradient, params);

            // Calculate the slope in this direction
            slope = slope(direction, newGradient);

            // Set a starting step size
            step = 1.0;
        }
        
        double step;
        Parameters params;
        Map<String, Double> newGradient;
        Map<String, Double> oldGradient;
        Map<String, Double> direction;
        double newML;
        double oldML;
        double slope;
    }
    
    private static final long serialVersionUID = 1;
}
