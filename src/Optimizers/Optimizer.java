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
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameters;
import Trees.TreeException;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Interface for different likelihood optimizers
 * @author Daniel Money
 * @version 2.0
 */
public interface Optimizer
{
    /**
     * Maximises the likelihood, logging to screen.  Logging level sould be
     * set in the constructor of implementing classes.
     * @param <R> The type returned by the calculator
     * @param l The likelihood calculator
     * @param p The parameters to maximise.  Parameters are modified.
     * @return The maximised likelihood (in a structure that includes most
     * intermediate likelihoods).
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws OutputException Thrown if unable to write a checkpoint file
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive or NaN) log likelihood is calculated  
     */
    public <R extends Likelihood> R maximise(Optimizable<R> l, Parameters p) throws GeneralException;

    /**
     * Maximises the likelihood, logging to a file.  Logging level sould be
     * set in the constructor of implementing classes.
     * @param <R> The type returned by the calculator
     * @param l The likelihood calculator
     * @param params The parameters to maximise.  Parameters are modified.
     * @param log The log file
     * @return The maximised likelihood (in a structure that includes most
     * intermediate likelihoods).
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws OutputException Thrown if unable to write a checkpoint file
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive or NaN) log likelihood is calculated  
     */
    public <R extends Likelihood> R maximise(Optimizable<R> l, Parameters params, File log) throws GeneralException;

    /**
     * Maximises the likelihood starting from a checkpoint file (see {@link #setCheckPointFile(java.io.File)}, 
     * logging to the screen.  Logging level sould be set in the constructor of implementing classes.
     * @param <R> The type returned by the calculator
     * @param l The likelihood calculator
     * @param checkPoint The checkpoint file
     * @return The maximised likelihood (in a structure that includes most
     * intermediate likelihoods).
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws OutputException Thrown if unable to write a checkpoint file 
     * @throws InputException Thrown if there is a problem with the checkpoint file
     * @throws Optimizers.Optimizer.OptimizerException Thrown if optomizer is unable
     * to restart from a checkpoint file
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive or NaN) log likelihood is calculated  
     */
    public <R extends Likelihood> R restart(Optimizable<R> l, File checkPoint) throws GeneralException;
    
    /**
     * Maximises the likelihood starting from a checkpoint file (see {@link #setCheckPointFile(java.io.File)}, 
     * logging to a file.  Logging level sould be set in the constructor of implementing classes.
     * @param <R> The type returned by the calculator
     * @param l The likelihood calculator
     * @param checkPoint The checkpoint file
     * @param log The log file
     * @return The maximised likelihood (in a structure that includes most
     * intermediate likelihoods).
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws OutputException Thrown if unable to write a checkpoint file 
     * @throws InputException Thrown if there is a problem with the checkpoint file
     * @throws Optimizers.Optimizer.OptimizerException Thrown if optomizer is unable
     * to restart from a checkpoint file
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive or NaN) log likelihood is calculated  
     */
    public <R extends Likelihood> R restart(Optimizable<R> l, File checkPoint, File log) throws GeneralException;
    
    /**
     * Sets a checkpoint file.  If set will write a checkpoint file of the
     * optimizers state at regular intervals (set by {@link #setCheckPointFrequency(int, java.util.concurrent.TimeUnit)}).
     * This checkpoint file can then be used to restart the optimizer from this
     * state.  Can be useful if optimization is likely to take a long and the
     * process could be stopped for some reason.
     * @param checkPoint The chekpoint file.
     * @throws Optimizers.Optimizer.OptimizerException Thrown if the optomizer
     * does not implement checkpoints.
     */
    public void setCheckPointFile(File checkPoint) throws OptimizerException;
    
    /**
     * Sets how often a checkpoint is saved (if a file has been set).
     * @param num The number of time units that should pass between checkpoint
     * writes.
     * @param unit The time unit
     * @throws Optimizers.Optimizer.OptimizerException Thrown if the optomizer
     * does not implement checkpoints.
     */
    public void setCheckPointFrequency(int num, TimeUnit unit) throws OptimizerException;
    
    public void setMaximumRunTime(int num, TimeUnit unit) throws OptimizerException;

    /**
     * Exception for when there is a problem with an optimiser.
     */
    public class OptimizerException extends GeneralException
    {
        /**
         * Default constrcutor
         * @param reason Text describing the problem
         */
        public OptimizerException(String reason)
        {
            super("OptimizerException\n\tReason:" + reason,null);
        }
    }
}
