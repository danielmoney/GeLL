package Utils;

/**
 * Utility class for 2D arrays
 * @author Daniel Money
 * @version 1.0
 */
public class Array2D
{
    private Array2D()
    {
        // This class has all static methods so no need for a constructor.
        // As that's not possible make the only constructor private so it can't
        // be called.
    }
    
    /**
     * Transposes the array
     * @param m Input array
     * @return The transposed array
     */
    public static double[][] transpose(double[][] m)
    {
	double[][] tm = new double[m[0].length][m.length];
	for (int i = 0; i < m.length; i++)
	{
	    for (int j = 0; j < m.length; j++)
	    {
		tm[i][j] = m[j][i];
	    }
	}
	return tm;
    }

    /**
     * Returns a column of the array
     * @param m The 2D array
     * @param c The index of the column to return
     * @return The column given
     */
    public static double[] getColumn(double[][] m, int c)
    {
	double[] col = new double[m.length];
	for (int i = 0; i < m.length; i++)
	{
	    col[i] = m[i][c];
	}
	return col;
    }
}
