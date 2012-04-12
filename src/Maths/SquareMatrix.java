package Maths;

import Exceptions.UnexpectedError;
import Maths.EigenvalueDecomposition.ConvergenceException;
import Utils.Array2D;
import Utils.DaemonThreadFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Represents a square matrix.  
 * @author Daniel Money
 * @version 1.0
 */

/*
 * The matrix is represnted internally as a 2D array of doubles and all
 * claculations are done on arrays.  The basic add, multiply, scalar divide and
 * scalar multiply are done by the private functions aAdd, aMultiply, aSDivide
 * and aSMutiply respectively.  This is done as these operations are often done
 * internally in the more complex functions and without these functions the
 * array would need to be converted to a SquareNatrix and then the array retrived
 * from the result.  The equivilant public functions are simple wrappers around
 * these functions.
 */

public class SquareMatrix implements Serializable
{
    /**
     * Default constructor
     * @param m Array representing the matrix
     * @throws SquareMatrixException Thrown if the input array isn't square
     */
    public SquareMatrix(double[][] m) throws SquareMatrixException
    {
        for (int i = 0; i < m.length; i++)
        {
            if (m[i].length != m.length)
            {
                throw new SquareMatrixException("Matrix not square");
            }
        }
        this.m = m;
	this.dim = m.length;
        //this.cached = 0;
        //this.cache = this.m[0];
    }

    /**
     * Sqaures the matrix and returns a new Matrix that is the result
     * @return The resulting matrix
     */
    public SquareMatrix square()
    {
	return SquareMatrix.newWithoutCheck(aMultiply(m, m));
    }

    private double[][] aMultiply(double[][] a, double[][] b) //throws SquareMatrixException
    {
        double[][] r = new double[dim][dim];
        
        //If the matrix is small then the overhead in doing multithread coputation
        //is greater than the time saved so only use mulithreaded for large matrices
        //10 is a random guess and should probably be tuned
        if (dim > 10)
        {
            //Parallel computation of matrix multiplication using the
            //number of thread given by noThreads which defaults to the number
            //of processors as reported by Java
            try
            {
                /*ExecutorService es = Executors.newFixedThreadPool(noThreads);

                List<Future<double[]>> list = new ArrayList<>();

                //Does the computation row-by-row.  Each row is treated as a seperate
                //caluclation and passed to the ExecutorService.
                for (int i=0; i < dim; i++)
                {
                    list.add(es.submit(
                            new ThreadedMult(a, b, i)));
                }

                //Wait for the ExecutorService to finish
                es.shutdown();*/
                
                List<ThreadedMult> tasks = new ArrayList<>();
                for (int i=0; i < dim; i++)
                {
                    tasks.add(new ThreadedMult(a, b, i));
                }
                List<Future<double[]>> list = es.invokeAll(tasks);

                //Retrieve and combine the results
                for (int i=0; i < dim; i++)
                {
                    try
                    {
                        r[i] = list.get(i).get();
                    }
                    catch(ExecutionException ex)
                    {
                        Throwable cause = ex.getCause();
                        if (cause instanceof RuntimeException)
                        {
                            throw (RuntimeException) cause;
                        }
                        if (cause instanceof Error)
                        {
                            throw (Error) cause;
                        }
                        //As call() does not throw any exceptions that must be caught
                        //we should only have RuntimeExceptions or Errors
                        //and so never reach here!
                        //But just in case...
                        throw new UnexpectedError(ex);
                    }
                }
            }
            catch(InterruptedException ex)
            {
                //Don't think this should happen but in case it does...
                throw new UnexpectedError(ex);
            }
        }
        else
        {
            for (int i = 0; i < dim; i++)
            {
                for (int j = 0; j < dim; j++)
                {
                    r[i][j] = 0;
                    for (int k = 0; k < dim; k++)
                    {
                        r[i][j] = r[i][j] + a[i][k] * b[k][j];
                    }
                }
            }
        }
        
        return r;
    }

    private double[][] aAdd(double[][] M, double[][] N)
    {
	/*double[][] r = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		r[i][j] = M[i][j] + N[i][j];
	    }
	}
	return r;*/
        double[][] r = new double[dim][];
        for (int i = 0; i < r.length; i ++)
        {
            double[] ri = Arrays.copyOf(M[i], M[i].length);
            double[] ni = N[i];
            for (int j = 0; j < ri.length; j++)
            {
                ri[j] = ri[j] + ni[j];
            }
            r[i] = ri;
        }
        return r;
    }

    private double[][] aSDivide(double[][] M, double n)
    {
	double[][] r = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		r[i][j] = M[i][j] / n;
	    }
	}
	return r;
    }

    private double[][] aSMultiply(double[][] M, double n)
    {
        /*double[][] r = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		r[i][j] = M[i][j] * n;
	    }
	}
	return r;*/
	//double[][] r = new double[dim][dim];
        double[][] r = new double[dim][];
	//for (int i = 0; i < dim; i++)
        for (int i = 0; i < r.length; i++)
	{
            double[] ri = Arrays.copyOf(M[i], M[i].length);
	    //for (int j = 0; j < dim; j++)
            for (int j = 0; j < ri.length; j++)
	    {
		//r[i][j] = M[i][j] * n;
                ri[j] = ri[j] * n;
	    }
            r[i] = ri;
	}
	return r;
    }

    /**
     * Multiplies the matrix by a scalar and returns a new matrix as the result
     * @param n The scalar to multiply by
     * @return The resulting matrix
     */
    public SquareMatrix scalarMultiply(double n)
    {
	return SquareMatrix.newWithoutCheck(aSMultiply(m, n));
    }

    /**
     * Divides the matrix by a scalar and returns a new matrix as the result
     * @param n The scalar to divide by
     * @return The resulting matrix
     */
    public SquareMatrix scalarDivide(double n)
    {
	return SquareMatrix.newWithoutCheck(aSDivide(m, n));
    }

    /**
     * Adds a matrix to this one and returns a new matrix as the result
     * @param o The matrix to add
     * @return The resulting matrix
     * @throws SquareMatrixException Thrown if the matrices are not the same size
     */
    public SquareMatrix add(SquareMatrix o) throws SquareMatrixException
    {
	if (this.dim != o.dim)
	{
	    throw new SquareMatrixException("Matrices can not be added together - different sizes");
	}
	return SquareMatrix.newWithoutCheck(aAdd(m, o.getArray()));
    }

    /**
     * Multiplies this matrix by another (this &times; o) and returns a new
     * matrix as a result
     * @param o The matrix to multiply by
     * @return The resulting matrix
     * @throws SquareMatrixException Thrown if the matrices are not the same size
     */
    public SquareMatrix multiply(SquareMatrix o) throws SquareMatrixException
    {
	if (this.dim != o.dim)
	{
	    throw new SquareMatrixException("Matrices can not be added together - different sizes");
	}
	return SquareMatrix.newWithoutCheck(aMultiply(m, o.getArray()));
    }

    /**
     * Calculates e^Ax where A is this matrix and returns a new matrix as the result.
     * Method used depends on the last call to {@link #setExpMethod(Maths.SquareMatrix.Calculation)}.
     * @param x x in the above equation
     * @return The resulting matrix
     * @throws SquareMatrixException Thrown if the calculation can't be performed
     *      as the Eigenvalues can't be computed
     */
    public SquareMatrix expMult(double x) throws SquareMatrixException
    {
        //Detect the case where x == 0 and if so return the identity matrix
	if (x == 0.0)
	{
	    double[][] R = new double[dim][dim];
	    for (int i = 0; i < dim; i++)
	    {
		for (int j = 0; j < dim; j++)
		{
		    if (i == j)
		    {
			R[i][j] = 1.0;
		    }
		    else
		    {
			R[i][j] = 0.0;
		    }
		}
	    }
	    return SquareMatrix.newWithoutCheck(R);
	}

        // If x != 0 call the appropiate method
	switch (expMethod)
	{
	    case TAYLOR:
		return expMult_Taylor(x);
	    case EIGEN:
		return expMult_Eigen(x);
	    case TAYLOR_NC:
	    default:
		return scalarMultiply(x).exp();
	}
    }

    private SquareMatrix expMult_Taylor(double mult)
    {
        //Powers are cached to save computation time while computing
        //the exponentional.  If we don't have cached results saved then compute them...
	if (powers == null)
	{
	    cacheSquared();
	}

        //Work out how many repeated squaring steps we should use to ensure stability
	int t = 0;
	double imult = mult * Math.pow(2.0,pdiv);
	for (; (imult * (norm()/Math.pow(2.0,pdiv))) > 1.0 ; imult = imult / 2)
	{
	    t++;
	}

        //If the number is less than the minimum then up to the minimum
	while (t < force)
	{
	    imult = imult / 2;
	    t++;
	}

        //Create the result matrix with the identity matrix since e^Ax = I + A + A^2/2...
	double[][] R = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    /*for (int j = 0; j < dim; j++)
	    {
		if (i == j)
		{
		    R[i][j] = 1.0;
		}
		else
		{
		    R[i][j] = 0.0;
		}
	    } */
            R[i][i] = 1.0;
	}

        //Calculate the Taylor expansion using the already stored values of A, A^2,
        //A^3 etc.  imult is the factor neccessary for the repeated squaring step.
        //imult only accounts for the number of extra steps need to meet the minimum
        //as other steps were taken into account at the cachuing stage.
	for (int i = numIt; i > 0; i--)
	{
	    R = aAdd(R, aSMultiply(powers[i], (ifac(i)*Math.pow(imult,i))));
	}

        //Do the repeated squaring
	for (int i = 0; i < t; i++)
	{
	    R = aMultiply(R,R);
	}

	return SquareMatrix.newWithoutCheck(R);
    }

    private SquareMatrix expMult_Eigen(double mult) throws SquareMatrixException
    {
        //Compute the matrix exponentation using the standard eigenvalue method
        //caches the eigen decomposition and relate dpropertied (p, pi, d) and
        //only claculates them if they've not already been.
	if (p == null)
	{
	    try
	    {
		p = eigVectors();
		pi = p.inverse();
		d = eigValuesDiag();
	    }
	    catch (ConvergenceException e)
	    {
		throw new SquareMatrixException("Cannot calculate eigenvector - no convergence");
	    }
	}

	SquareMatrix de = d.diagExp(mult);

	SquareMatrix res;
	try
	{
	     res = p.multiply(de).multiply(pi);
	}
	catch (SquareMatrixException e)
	{
	    throw new InternalError();
	}
	return res;
    }


    /**
     * Calculates e^A where A is the current matrix and returns a new matrix as
     * the result
     * @return The reuslting matrix
     * @throws SquareMatrixException Thrown if the calculation can't be performed
     *      as the Eigenvalues can't be computed
     */
    public SquareMatrix exp() throws SquareMatrixException
    {
	switch (expMethod)
	{
	    case TAYLOR:
		return exp_Taylor();
	    case EIGEN:
		return exp_Eigen();
	    case TAYLOR_NC:
	    default:
		return exp_TaylorNoCache();
	}
    }

    private void cacheSquared()
    {
        // Work out how many repeated suaring steps will be neccessary for
        // stable claculation then cache pwers of that matrix.
	int t = 0;
	for (double norm = norm(); norm > 1.0; norm = norm / 2)
	{
	    t++;
	}

	pdiv = t;

	powers = new double[numIt+1][][];

	double[][] P = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		P[i][j] = m[i][j] * Math.pow(2.0,-t);
	    }
	}
        
        //double[][] P = aSMultiply(m, Math.pow(2.0,-t));

	powers[1] = P;

	for (int i = 2; i <= numIt; i++)
	{
	    powers[i] = aMultiply(powers[i-1],powers[1]);
	}
    }

    private SquareMatrix exp_Taylor()
    {
        //Normal matrix exponentation using the Taylor method with caching
	if (powers == null)
	{
	    cacheSquared();
	}

	int t = 0;
	for (double norm = norm(); norm > 1.0; norm = norm / 2)
	{
	    t++;
	}
	//System.out.println(t);

	double[][] R = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		if (i == j)
		{
		    R[i][j] = 1.0;
		}
		else
		{
		    R[i][j] = 0.0;
		}
	    }
	}

	for (int i = numIt; i > 0; i--)
	{
	    R = aAdd(R, aSMultiply(powers[i], ifac(i)));
	}

	for (int i = 0; i < t; i++)
	{
	    R = aMultiply(R,R);
	}

	return SquareMatrix.newWithoutCheck(R);
    }

    private double ifac(int n)
    {
        //Calculate sthe inverse factorial, that is 1/n!  Used in the Taylor
        //expansion
	int t = 1;
	for (int i = 2; i <=n; i++)
	{
	    t = t * i;
	}
	return 1.0 / (double) t;
    }

    private SquareMatrix exp_Eigen() throws SquareMatrixException
    {
        //Compute the matrix exponentation using the standard eigenvalue method,
        //again with caching
        if (p == null)
	{
	    try
	    {
		p = eigVectors();
		pi = p.inverse();
		d = eigValuesDiag();
	    }
	    catch (ConvergenceException e)
	    {
		throw new SquareMatrixException("Cannot calculate eigenvector - no convergence");
	    }
	}

	SquareMatrix de = d.diagExp(1.0);

	SquareMatrix res;
	try
	{
	     res = p.multiply(de).multiply(pi);
	}
	catch (SquareMatrixException e)
	{
	    throw new InternalError();
	}
	return res;
    }

    private SquareMatrix exp_TaylorNoCache()
    {
        //Normal, simple taylor expansion with no caching.  Can be useful for debugging.
	double e = 0;
	double[][] M = new double[dim][dim];
	double[][] R = new double[dim][dim];
	double[][] P = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		if (i == j)
		{
		    R[i][j] = 1.0;
		}
		else
		{
		    R[i][j] = 0.0;
		}
		M[i][j] = m[i][j];
	    }
	}

	for (double norm = norm(); norm > 1; norm = norm / 2)
	{
	    M = aSDivide(M, 2);
	    e++;
	}

	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		P[i][j] = M[i][j];
	    }
	}

	int t = 1;
	for (int i = 1; i <= numIt; i++)
	{
	    t = t * i;
	    R = aAdd(R, aSDivide(P, t));
	    P = aMultiply(P, M);
	}

	for (int i = 0; i < e; i++)
	{
	    R = aMultiply(R, R);
	}

	return SquareMatrix.newWithoutCheck(R);
    }

    private SquareMatrix diagExp(double mult)
    {
        //Raises the values on the diagonal to a power.  Used to raise the
        //eigenvalues to a power when they're estored on the diagonal and used
        //when calculating matrix exponentation using eigendecomposition methods
        double[][] ed = new double[m.length][m.length];
	for (int i = 0; i < m.length; i++)
	{
	    for (int j = 0; j < m.length; j++)
	    {
		ed[i][j] = 0.0;
	    }
	    ed[i][i] = Math.exp(m[i][i] * mult);
	}
	return SquareMatrix.newWithoutCheck(ed);
    }

    /**
     * Calculates the maximum absolute column sum norm
     * @return The maximum absolute column sum norm
     */
    public double norm()
    {
	double max = 0.0;
	for (int i = 0; i < dim; i++)
	{
	    double cur = 0.0;
	    for (int j = 0; j < dim; j++)
	    {
		cur = cur + Math.abs(m[j][i]);
	    }
	    if (cur > max)
	    {
		max = cur;
	    }
	}
	return max;
    }

    /**
     * Calculates the inverse of this matrix and returns a new matrix as the result.
     * Uses the technique of augmenting the original matrix (L) with the identity 
     * matrix (I) and then reducing the original to the identity doing the same
     * operation on both.  What I has become is the inverse.
     * @return The resulting matrix
     */
    public SquareMatrix inverse()
    {
        double[][] L = new double[m.length][];
	for (int i = 0; i < m.length; i++)
	{
	    L[i] = Arrays.copyOf(m[i], m.length);
	}
	double[][] I = new double[dim][dim];
	for (int i = 0; i < dim; i++)
	{
	    for (int j = 0; j < dim; j++)
	    {
		if (i == j)
		{
		    I[i][j] = 1.0;
		}
		else
		{
		    I[i][j] = 0.0;
		}
	    }
	}
        
        //L is the original, I is the idenity.  Now by using standard matrix
        //techniques on both at the same time reduce L to the idenity matrix.
        //What I has become is the inverse.

	for (int i = 0; i < dim; i++)
	{
	    for (int j = i + 1; j < dim; j++)
	    {
		double f = -L[j][i] / L[i][i];
		for (int k = 0; k < dim; k++)
		{
		    L[j][k] = L[j][k] + f * L[i][k];
		    I[j][k] = I[j][k] + f * I[i][k];
		}
	    }
	}

	for (int i = dim - 1; i >= 0; i--)
	{

	    for (int j = 0; j < i; j++)
	    {
		double f = -L[j][i] / L[i][i];
		for (int k = 0; k < dim; k++)
		{
		    L[j][k] = L[j][k] + f * L[i][k];
		    I[j][k] = I[j][k] + f * I[i][k];
		}
	    }
	}

	for (int i = 0; i < dim; i++)
	{
	    double f = L[i][i];
	    for (int k = 0; k < dim; k++)
	    {
		L[i][k] = L[i][k] / f;
		I[i][k] = I[i][k] / f;
	    }
	}

	return SquareMatrix.newWithoutCheck(I);
    }

    /**
     * Returns the value in position (i,j) of the matrix
     * @param i The row position
     * @param j The column position
     * @return The value at (i,j)
     */
    public double getPosition(int i, int j)
    {
        /*if (i != cached)
        {
            cached = i;
            cache = m[i];
        }
        return cache[j];*/
	return m[i][j];
    }

    /**
     * Gets the size of the matrix.
     * @return The size of the matrix
     */
    public int size()
    {
	return m.length;
    }

    /**
     * Gets the matrix as an array of doubles
     * @return the matrix as an array
     */
    public double[][] getArray()
    {
	return m;
    }

    /**
     * Sets the method to be used for Exponentiation
     * @param i The method to be used
     */
    public static void setExpMethod(Calculation i)
    {
	expMethod = i;
    }

    /**
     * Sets the minimum number of repeating squaring that will be performed
     * when using the Taylor method
     * @param f
     */
    public static void setForce(int f)
    {
	force = f;
    }
    
    /**
     * Sets the number of threads to be used when doing matrix multiplication
     * @param number
     */
    public static void setNoThreads(int number)
    {
        //noThreads = number;
        es = Executors.newFixedThreadPool(number, new DaemonThreadFactory());
    }
    
    public boolean equals(Object ob)
    {
        if (!(ob instanceof SquareMatrix))
        {
            return false;
        }
        
        SquareMatrix b = (SquareMatrix) ob;
        
        if (b.size() != dim)
        {
            return false;
        }
        
        for (int i = 0; i < dim; i ++)
        {
            for (int j = 0; j < dim; j ++)
            {
                if (m[i][j] != b.m[i][j])
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        double hc = 0.0;        
                
        for (int i = 0; i < dim; i ++)
        {
            for (int j = 0; j < dim; j ++)
            {
                hc += m[i][j];
            }
        }
        return (int) Math.round(hc);
    }

    /**
     * Calculates the Eigenvectors.  The actual decompisition is cahced so only
     * performed once for each matrix.
     * @return The eigenvectors as a square matrix
     * @throws ConvergenceException Thrown if the eigendeompisition does not converge
     */
    public SquareMatrix eigVectors() throws ConvergenceException
    {
	if (ed == null)
	{
	    ed = new EigenvalueDecomposition(this);
	}
	//return ed.getV().getArray();
	return ed.getV();
    }

    /**
     * Calculates the Eigenvalues. The actual decompisition is cahced so only
     * performed once for each matrix.
     * @return The eigenvalues as a array of doubles
     * @throws ConvergenceException Thrown if the eigendeompisition does not converge
     */
    public double[] eigValues() throws ConvergenceException
    {
	if (ed == null)
	{
	    ed = new EigenvalueDecomposition(this);
	}
	return ed.getRealEigenvalues();
    }

    /**
     * Calculates the Eigenvalues. The actual decompisition is cahced so only
     * performed once for each matrix.
     * @return A SquareMatrix with the eigenvalues on the diagonal.
     * @throws ConvergenceException
     */
    public SquareMatrix eigValuesDiag() throws ConvergenceException
    {
	if (ed == null)
	{
	    ed = new EigenvalueDecomposition(this);
	}
	return ed.getD();
    }

    /**
     * Transposes the matrix and returns a new matrix as the result
     * @return The resulting matrix
     */
    public SquareMatrix transpose()
    {
	return SquareMatrix.newWithoutCheck(Array2D.transpose(m));
    }

    SquareMatrix permutate(int[] r)
    {
        //Permuates the matrix based on r.  Column i in the original matrix
        //will become column r[i] in the new one.  No error checking and only
        //used, and indeed intended to be used, by LUDecompostition
	double[][] B = new double[m.length][m[0].length];

	for (int i = 0; i < r.length; i++)
	{
	    for (int j = 0; j < m[0].length; j++)
	    {
		B[i][j] = m[r[i]][j];
	    }
	}

	return SquareMatrix.newWithoutCheck(B);
    }

    private LUDecomposition getInverseLU()
    {
	if (lu == null)
	{
	    lu = new LUDecomposition(this.inverse());
	}
	return lu;
    }

    /**
     * Divides this matrix by another (this / B) and returns a new matrix as the
     * result
     * @param B The matrix to divide by
     * @return The resulting matrix
     */
    public SquareMatrix divide(SquareMatrix B)
    {
	LUDecomposition l = B.getInverseLU();
	SquareMatrix s = l.solve(this.inverse());
	return s.inverse();
    }

    public String toString()
    {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < m.length; i++)
	{
	    for (int j = 0; j < m[0].length; j++)
	    {
		sb.append(m[i][j]);
		if (j != m[0].length - 1)
		{
		    sb.append("\t");
		}
	    }
	    sb.append("\n");
	}
	return sb.toString();
    }

    private double[][] m;

    private int dim;

    private SquareMatrix p;

    private SquareMatrix pi;

    private SquareMatrix d;

    private EigenvalueDecomposition ed;

    private LUDecomposition lu;

    private double[][][] powers;

    private int pdiv;
    
    //private double[] cache;
    //private int cached;
    
    //private static int noThreads = Runtime.getRuntime().availableProcessors();

    private static final int numIt = 12;

    private static Calculation expMethod = Calculation.TAYLOR;

    private static int force = 0;
    
    private static ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new DaemonThreadFactory());
    
    /**
     * Enumeration of the possible ways of calculating the matrix exponential
     */
    public enum Calculation
    {
        /**
         * Use the eigendecompositin method
         */
        EIGEN,
        /**
         * Use the Taylor expansion mehtod
         */
        TAYLOR,
        /**
         * Use the Taylor expansion method without caching (useful for debugging)
         */
        TAYLOR_NC
    }

    /**
     * Creates a new matrix without throwing an exception.  Throws an error
     * if a non-square matrix is passed to it.  Used internally when we know
     * the matrix is square
     * @param m Array representing the matrix
     * @return New SquareMatrix object
     */
    static SquareMatrix newWithoutCheck(double[][] m)
    {
        try
        {
            return new SquareMatrix(m);
        }
        catch (SquareMatrixException ex)
        {
            //As this function should only be called when we know the matrix
            //is square we should never get here but in case we do...
            throw new UnexpectedError(ex);
        }
    }
    
    private static final long serialVersionUID = 1;
    
    private class ThreadedMult implements Callable<double[]>
    {
        private ThreadedMult(double[][] a, double[][] b, int i)
        {
            this.a = a;
            this.b = b;
            this.i = i;
        }
        
        public double[] call()
        {
            int n = a.length;
            double[] r = new double[n];


            for (int j = 0; j < n; j++)
            {
                r[j] = 0;

                for (int k = 0; k < n; k++)
                {
                    r[j] += a[i][k] * b[k][j];
                }
            }
            
            return r;
        }
        
        private double[][] a;
        private double[][] b;
        private int i;
    }
    
    /**
     * Exception thrown when there is a problem with a SquareMatrix operation
     */
    public class SquareMatrixException extends Exception
    {
        /**
         * Default constructor
         * @param msg The cause of the problem
         */
        public SquareMatrixException(String msg)
        {
            super(msg);
        }
    }
}
