package Optimizers;

import Exceptions.GeneralException;
import Likelihood.Likelihood;
import Parameters.Parameters;

/**
 * Interface for classes that represent optimisable likelihood functions.
 * @author Daniel
 * @version 2.0
 * @param <R> The class that the function returns 
*/
public interface Optimizable<R extends Likelihood>
{

    /**
     * The optimisable likelihood function.  When passed a set of parameters
     * returns the likelihood result.  The result is returned as an object that
     * extends {@link Likelihood} so that other information, such as intermediate
     * calculations can also be returned.
     * @param p The value of the parameters to be optimised
     * @return The calculated likelihood
     * @throws GeneralException Thrown if there is an error calculating the
     * likelihood
     */
    public R calculate(Parameters p) throws GeneralException;
}
