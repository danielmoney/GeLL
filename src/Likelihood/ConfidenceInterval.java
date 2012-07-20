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

package Likelihood;

import Likelihood.Calculator.CalculatorException;
import Maths.Gamma;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.TreeException;
import Utils.Range;

/**
 * Used to calculate congfidence intervals for a parameter.  Due to computational
 * constraints this is not done properly.  When searching for the edges of the
 * confidence interval we only update the parameter of interest rather than
 * optomising every parameter at each step.
 * @author Daniel Money
 * @version 1.0
 */
public class ConfidenceInterval
{
    /**
     * Default constructor
     * @param l The likelihod calculator to be used in constrcuting confidence
     * intervals
     * @param p The parameters to construct confidence intervals for
     */
    public ConfidenceInterval(Calculator l, Parameters p)
    {
	this.l = l;
	this.p = p;
    }

    /**
     * Calculates the confidence interval for a single parameter
     * @param param The parameter to construct the interval for
     * @param conf The confidence level
     * @return The confidence interval
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     */
    public Range getCI(Parameter param, double conf) throws
	    RateException, ModelException, TreeException, ParameterException, CalculatorException
    {
        //Calculate the difference in likelihood that is related to the given
        //confidence value
	double ld = Gamma.chi2inv(1-conf,1) / 2;

        //Store the original value and it's likelihood
	double o = p.getValue(param.getName());
	double m = l.calculate(p).getLikelihood();
	
        //Calculate the bounds
	double upper = upper(param, o, m, ld);
	double lower = lower(param, o, m, ld);

        //Restore the original value
	p.setValue(param, o);
	return new Range(lower,upper);
    }
    
    private double lower(Parameter param, double o, double m, double ld) throws
	    RateException, ModelException, TreeException, ParameterException, CalculatorException
    {
        //diff is the current step size
        //Think this will breakj if the confidence bound is more than 1 away
	double diff = 1;
        //nv is our current guess at where the lower bound is
	double nv = Math.max(o-diff,param.getLowerBound());
	p.setValue(param, nv);
        //Calculate the likelihood for that guess
	double cl = l.calculate(p).getLikelihood();
	
        //If we've reached the lower bound on the parameter then return that
	if ((nv == param.getLowerBound()) && ((m - cl) < ld))
	{
	    return nv;
	}
	
        //Bound the search area
	while (cl < m - ld)
	{
	    diff = diff / 10;
	    nv = Math.max(o-diff,param.getLowerBound());
	    p.setValue(param, nv);
	    cl = l.calculate(p).getLikelihood();
	}

        //Do a golden section search and return the result
	return single(param, m-ld, o, Math.max(o - diff*10,param.getLowerBound()));
    }

    private double upper(Parameter param, double o, double m, double ld) throws 
	    RateException, ModelException, TreeException, ParameterException, CalculatorException
    {
        //Same as for lwoer except the obvious difference!
	double diff = 1;
	double nv = Math.min(o+diff,param.getUpperBound());
	p.setValue(param, nv);
	double cl = l.calculate(p).getLikelihood();

        if ((nv == param.getUpperBound()) && ((m - cl) < ld))
	{
	    return nv;
	}

	while (cl < m - ld)
	{
	    diff = diff / 10;
	    nv = Math.min(o+diff,param.getUpperBound());
	    p.setValue(param, nv);
	    cl = l.calculate(p).getLikelihood();
	}

	return single(param, m-ld, o, Math.min(o + diff*10, param.getUpperBound()));
    }

    private double single(Parameter param, double target, double a, double b) throws
	    RateException, ModelException, TreeException, ParameterException, CalculatorException
    {
        //Do a golden section search for the likelihood value (max - diff) that we
        //are looking for in the bounds given by a and b
	double x1 = a + R * (b - a);
	double x2 = b - R * (b - a);

	p.setValue(param,x1);
	double x1val = l.calculate(p).getLikelihood();
	p.setValue(param,x2);
	double x2val = l.calculate(p).getLikelihood();

	while (Math.abs(x1val - target) > Math.pow(10,-7))
	{
	    if (x1val > target)
	    {
		a = x2;
		x2 = x1;
		x2val = x1val;
		x1 = a + R * (b - a);
		p.setValue(param,x1);
		x1val = l.calculate(p).getLikelihood();
		System.out.println("\t" + x1 + "\t" + x1val);
	    }
	    else
	    {
		b = x1;
		x1 = x2;
		x1val = x2val;
		x2 = b - R * (b - a);
		p.setValue(param,x2);
		x2val = l.calculate(p).getLikelihood();
		System.out.println("\t" + x2 + "\t" + x2val);
	    }
	}
	return x1;
    }

    private Calculator l;
    private Parameters p;

    private static final double R = (Math.sqrt(5.0) - 1) / 2;
}
