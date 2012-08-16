package Likelihood;

import Likelihood.Calculator.CalculatorException;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.TreeException;

public interface CalculatesLikelihood<R extends HasLikelihood>
{
    public R calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException;
}
