package Optimizers;

import Exceptions.GeneralException;
import Likelihood.Likelihood;
import Parameters.Parameters;

public interface Optimizable<R extends Likelihood>
{
    public R calculate(Parameters p) throws GeneralException;
}
