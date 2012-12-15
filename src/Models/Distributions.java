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

package Models;

import Maths.EigenvalueDecomposition.ConvergenceException;
import Maths.SquareMatrix;
import Maths.SquareMatrix.SquareMatrixException;
import Utils.Array1D;
import Utils.Array2D;

/**
 * Class to calculate stationary and quasi-stationary distributions
 * @author Daniel Money
 * @version 2.0
 */

public class Distributions
{
    /**
     * Calculates a quasi-stationary distribution from a given rate matrix.
     * Uses the method set by the last call to {@link #setMethod(Models.Distributions.Calculation)}.
     * The state in posiiton zero is assumed to be the sink state
     * @param m The rate matrix
     * @return The quasi-statonary distribution
     * @throws DistributionsException If the distibution can't be calculated
     */
    public static double[] quasiStationary(SquareMatrix m) throws DistributionsException
    {
	switch (method)
	{
	    case EIGEN:
		return quasiStationary_Eigen(m);
	    case REPEAT:
	    default:
		return quasiStationary_Repeat(m);
	}
    }

    private static double[] quasiStationary_Eigen(SquareMatrix m) throws DistributionsException
    {
        //Calculates the quasi-statinary distribution using the eigendecomposition mehtod.
        //The distribution is the eigenvector with the second biggest eigenvalue scaled to
        //one
	double[][] eVe;
	double[] eVa;
	try
	{
	    eVe = m.eigVectors().getArray();
	    eVa = m.eigValues();
	}
	catch (ConvergenceException ex)
	{
	    throw new DistributionsException("Cannot not calculate quasi-stationary distribution - eigenvalue calculation does not converge");
	}

        //Get the index (i.e. rank) of each eigenvalue
	int[] ind = Array1D.index(eVa);
        
        //Get the eigenvector related to the second highest eigenvalue
	double[] v = Array2D.getColumn(eVe, ind[1]);
        //Work out the sum of that vector
	double total = 0.0;
	for (int i = 1; i < v.length; i++)
	{
	    total += v[i];
	}
        
        //Rescale the vector to one and return
	double[] qs = new double[v.length];
	qs[0] = 0.0;
	for (int i = 1; i < v.length; i++)
	{
	    qs[i] = v[i] / total;
	}

	return qs;
    }

    private static double[] quasiStationary_Repeat(SquareMatrix m) throws DistributionsException
    {
        //Calculates the quasi-stationary distribution by repeatedly applying
        //a P-matrix to a distribution
        
        //Calculate a P-matrix
	double[][] p;
	try
	{
	    p = m.scalarMultiply(8.0).exp().getArray();
	}
	catch (SquareMatrixException e)
	{
	    throw new DistributionsException("Cannot calculate quasi-staitinary distribution - cann't exponentiate matrix");
	}

        //Generate a start distribution (every state apart from the sink state is
        //equally likely
	double[] nf = new double[p.length];
	for (int i = 0; i < nf.length; i++)
	{
	    nf[i] = 1.0/((double) (nf.length-1));
	}
	nf[0] = 0.0;
	double[] res;

        //Keep count of how many reps we've done in case we don't converge
	int reps = 0;
        //Do until we converge
	do
	{
            //Throw error if we've been going too long without convergence
	    if (reps > 20000000)
	    {
		throw new DistributionsException("Cannot calculate quasi-stationary distribution - no convergence");
	    }
	    res = nf;
	    nf = new double[res.length];
            //Do one round of application to the distribution
	    for (int i = 0; i < res.length; i++)
	    {
		double tot = 0.0;
		for (int j = 0; j < res.length; j++)
		{
		    tot += (p[j][i] * res[j]);
		}
		nf[i] = tot;
	    }
            //Reset the sink state to zero and rescale the rest of the distibution
            //to one
	    nf[0] = 0.0;
	    double t = 0.0;
	    for (int i=1; i < nf.length; i++)
	    {
		t += nf[i];
	    }
	    for (int i=1; i < nf.length; i++)
	    {
		nf[i] = nf[i] / t;
	    }
	    reps++;
	}
	while (test(res,nf,1));

        return nf;
    }

    /**
     * Calculates a stationary distribution from a given rate matrix.
     * Uses the method set by the last call to {@link #setMethod(Models.Distributions.Calculation)}.
     * @param m The rate matrix
     * @return The stationary distribution
     * @throws DistributionsException If the distibution can't be calculated
     */
    public static double[] stationary(SquareMatrix m) throws DistributionsException
    {
	switch (method)
	{
	    case EIGEN:
		return stationary_Eigen(m);
	    case REPEAT:
	    default:
		return stationary_Repeat(m);
	}
    }
    
    private static double[] stationary_Eigen(SquareMatrix m) throws DistributionsException
    {
        //Similar to the quasi-0stationary distribution except we user the eigenvector
        //with the largest eigenvalue
	m = m.transpose();

	double[][] eVe;
	double[] eVa;
	try
	{
	    eVe = m.eigVectors().getArray();
	    eVa = m.eigValues();
	}
	catch (ConvergenceException ex)
	{
	    throw new DistributionsException("Cannot not calculate stationary distribution - eigenvalue calculation does not converge");
	}

	int[] ind = Array1D.index(eVa);

	double[] v = Array2D.getColumn(eVe, ind[0]);
	double total = 0.0;
	for (int i = 0; i < v.length; i++)
	{
	    total += v[i];
	}

	double[] s = new double[v.length];
	for (int i = 0; i < v.length; i++)
	{
	    s[i] = v[i] / total;
	}

	return s;
    }
    
    private static double[] stationary_Repeat(SquareMatrix m) throws DistributionsException
    {
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < m.size(); i++)
        {
            for (int j = 0; j < m.size(); j++)
            {
                max = Math.max(max, m.getPosition(i, j));
            }
        }
        
        //Similar to quasi-stationary except the rescaling step is unneccessary.
	double[][] p;
	try
	{
	    p = m.scalarMultiply(1/max).exp().getArray();
	}
	catch (SquareMatrixException e)
	{
	    throw new DistributionsException("Cannot calculate stationary distribution - can't exponentiate matrix");
	}


	double[] nf = new double[p.length];
	for (int i = 0; i < nf.length; i++)
	{
	    nf[i] = 1.0/((double) nf.length);
	}
	double[] res;

	int reps = 0;
	do
	{
	    if (reps > 2000000)
	    {
 		throw new DistributionsException("Cannot calculate stationary distribution - no convergence");
	    }
	    res = nf;
	    nf = new double[res.length];
	    for (int i = 0; i < res.length; i++)
	    {
		double tot = 0.0;
		for (int j = 0; j < res.length; j++)
		{
		    tot += (p[j][i] * res[j]);
		}
		nf[i] = tot;
	    }
	    reps++;            
	}
	while (test(res,nf,0));

	return nf;
    }

    /**
     * Sets the calculation method to be used when calculating distributions
     * @param m The calculation method to be used
     */
    public static void setMethod(Calculation m)
    {
	method = m;
    }

    private static boolean test(double[] a, double[] b, int start)
    {
        //Tests whether the difference between two distributions is small
        //in relative terms for each state.
        boolean test = true;
        for (int i = start; i < a.length; i++)
	{
            test = test && (Math.abs(Math.log(a[i]/b[i])) < Math.pow(10,-10));
        }
        return (!test);
    }

    private static Calculation method = Calculation.EIGEN;
    
    /**
     * Enumeration of the different methods of calculating a distribution
     */
    public enum Calculation
    {
        /**
         * Used the Eigendecomposition to calculate the distribution
         */
        EIGEN,
        /**
         * Use the limiting value of applying a probability matrix (generated
         * from the rate matrix) multiple times to an arbitary distribution.
         */
        REPEAT
    };
    
    /**
     * Exception for use there is a problem calculating a distribution
     */
    public static class DistributionsException extends Exception
    {
        /**
         * Default constructor
         * @param msg Message describing the problem
         */
        public DistributionsException(String msg)
        {
            super(msg);
        }
    }
}
