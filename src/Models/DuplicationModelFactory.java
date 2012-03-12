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

import Exceptions.UnexpectedError;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import java.util.HashMap;

/**
 * A factory for easing the creation of some simple gene-family models
 * @author Daniel Money
 * @version 1.0
 */
public class DuplicationModelFactory
{
    private DuplicationModelFactory()
    {
        // This class has all static methods so no need for a constructor.
        // As that's not possible make the only constructor private so it can't
        // be called.  See Ames et al 2012 for a fuller description of these models.
    }
    
    /**
     * Creates a simple parsimony-style model
     * @param p Parameters structure to add the model parameters to (none in
     * this case but for consistency this is left here)
     * @param num The maximum family size
     * @return The model
     */
    public static Model Parsimony(Parameters p, int num)
    {
        String[][] matrix = new String[num+1][num+1];
        HashMap<String,Integer> map = new HashMap<>();
        for (int i = 0; i <= num; i++)
        {
            map.put(Integer.toString(i), i);
        }
        for (int i = 0; i <= num; i++)
        {
            for (int j = 0; j <= num; j++)
            {
                if (Math.abs(i-j) == 1)
                {
                    matrix[i][j] = "1.0";
                }
                else
                {
                    matrix[i][j] = "0.0";
                }
            }
        }
        try
        {
            return new Model(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Creates a simple parsimony-style model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to
     * @param num The maximum family size
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model Parsimony_Gamma(Parameters p, int num, int numCats)
    {
        String[][] matrix = new String[num+1][num+1];
        HashMap<String,Integer> map = new HashMap<>();
        for (int i = 0; i <= num; i++)
        {
            map.put(Integer.toString(i), i);
        }
        for (int i = 0; i <= num; i++)
        {
            for (int j = 0; j <= num; j++)
            {
                if (Math.abs(i-j) == 1)
                {
                    matrix[i][j] = "1.0";
                }
                else
                {
                    matrix[i][j] = "0.0";
                }
            }
        }
        
        p.addParameter(Parameter.newEstimatedBoundedParameter("g", 0.2, 10.0));
        
        try
        {
            return Model.gammaRates(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Creates a simple BDI model
     * @param p Parameters structure to add the model parameters to
     * @param num The maximum family size
     * @return The model
     */
    public static Model BDI(Parameters p, int num)
    {
        String[][] matrix = new String[num+1][num+1];
        HashMap<String,Integer> map = new HashMap<>();
        for (int i = 0; i <= num; i++)
        {
            map.put(Integer.toString(i), i);
        }
        for (int i = 0; i <= num; i++)
        {
            for (int j = 0; j <= num; j++)
            {
                matrix[i][j] = "0.0";
                if (i-j == 1)
                {
                    matrix[i][j] = "d";
                }
                if (j-i == 1)
                {
                    matrix[i][j] = "b";
                }
                if ((i == 0) && (j == 1))
                {
                    matrix[i][j] = "i";
                }
            }
        }
        
        p.addParameter(Parameter.newFixedParameter("b", 1.0));
        p.addParameter(Parameter.newEstimatedPositiveParameter("d"));
        p.addParameter(Parameter.newEstimatedBoundedParameter("i",0.0,10.0));
        
        try
        {
            return new Model(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Creates a simple BDI model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to
     * @param num The maximum family size
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model BDI_Gamma(Parameters p, int num, int numCats)
    {
        String[][] matrix = new String[num+1][num+1];
        HashMap<String,Integer> map = new HashMap<>();
        for (int i = 0; i <= num; i++)
        {
            map.put(Integer.toString(i), i);
        }
        for (int i = 0; i <= num; i++)
        {
            for (int j = 0; j <= num; j++)
            {
                matrix[i][j] = "0.0";
                if (i-j == 1)
                {
                    matrix[i][j] = "d";
                }
                if (j-i == 1)
                {
                    matrix[i][j] = "b";
                }
                if ((i == 0) && (j == 1))
                {
                    matrix[i][j] = "i";
                }
            }
        }
        
        p.addParameter(Parameter.newFixedParameter("b", 1.0));
        p.addParameter(Parameter.newEstimatedPositiveParameter("d"));
        p.addParameter(Parameter.newEstimatedBoundedParameter("i",0.0,10.0));
        
        p.addParameter(Parameter.newEstimatedBoundedParameter("g", 0.2, 10.0));
        
        try
        {
            return Model.gammaRates(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
}
