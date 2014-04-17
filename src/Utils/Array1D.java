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

package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Utility class for 1D arrays
 * @author Daniel Money
 * @version 2.0
 */
public class Array1D
{
    private Array1D()
    {
        // This class has all static methods so no need for a constructor.
        // As that's not possible make the only constructor private so it can't
        // be called.
    }
    
    /**
     * Returns indexes from the input array in rank order.  That is the output
     * array will contain an index to the largest element in the original array,
     * then the second largest etc.
     * @param a The input array
     * @return An array of indexes in rank order
     */
    public static int[] index(double[] a)
    {
	int[] ranks = rank(a);
	int[] indexes = new int[ranks.length];
	for (int i = 0; i < ranks.length; i++)
	{
	    indexes[ranks[i]-1] = i;
	}
	return indexes;
    }

    /**
     * Returns the rank of each element in an array.  In the case of a tie
     * between elements in the array the element that occurs first will have the
     * lowest rank.
     * @param a The input array
     * @return The rank of each element
     */
    public static int[] rank(double[] a)
    {
	int[] ranks = new int[a.length];
	for (int i = 0; i < a.length; i++)
	{
	    int r = 0;
	    double d = a[i];
	    for (double d2:a)
	    {
		if (d <= d2)
		{
		    r++;
		}
	    }
	    ranks[i] = r;
	}
	return ranks;
    }
    
    /**
     * Write an array to a file
     * @param a The array
     * @param f The file
     * @throws FileNotFoundException thrown if this error is encountered while
     * writing the file.
     */
    public static void writeFile(double[] a, File f) throws FileNotFoundException
    {
        PrintStream out = new PrintStream(new FileOutputStream(f));
        for (int i = 0; i < a.length; i++)
        {
            out.println(i + "\t" + a[i]);
        }
        out.close();
    }
}
