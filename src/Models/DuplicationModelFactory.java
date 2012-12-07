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

    public static Model Parsimony(Parameters p, int num)
    {
        return Parsimony(p,num,false);
    }
    
    /**
     * Creates a simple parsimony-style model
     * @param p Parameters structure to add the model parameters to (none in
     * this case but for consistency this is left here)
     * @param num The maximum family size
     * @return The model
     */
    public static Model Parsimony(Parameters p, int num, boolean fixed)
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
                    matrix[i][j] = "r";
                }
                else
                {
                    matrix[i][j] = "0.0";
                }
            }
        }
        
        Model m = null;
        try
        {
            m = new Model(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        if (fixed)
        {
            p.addParameter(Parameter.newEstimatedBoundedParameter("r",1e-4,Double.MAX_VALUE));
            m.setRescale(false);
        }
        else
        {
            p.addParameter(Parameter.newFixedParameter("r", 1.0));
        }       
        
        return m;
    }

    public static Model Parsimony_Gamma(Parameters p, int num, int numCats)
    {
        return Parsimony_Gamma(p,num,numCats,false);
    }
    
    /**
     * Creates a simple parsimony-style model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to
     * @param num The maximum family size
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model Parsimony_Gamma(Parameters p, int num, int numCats, boolean fixed)
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
                    matrix[i][j] = "r";
                }
                else
                {
                    matrix[i][j] = "0.0";
                }
            }
        }
        
        Model m = null;
        try
        {
            m = Model.gammaRates(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }

        if (fixed)
        {
            p.addParameter(Parameter.newEstimatedBoundedParameter("r",1e-4,Double.MAX_VALUE));
            m.setRescale(false);
        }
        else
        {
            p.addParameter(Parameter.newFixedParameter("r", 1.0));
        }
        
        p.addParameter(Parameter.newEstimatedBoundedParameter("g", 0.2, 10.0));
        
        return m;
    }

    public static Model BDI(Parameters p, int num)
    {
        return BDI(p,num,false);
    }
    
    /**
     * Creates a simple BDI model
     * @param p Parameters structure to add the model parameters to
     * @param num The maximum family size
     * @return The model
     */
    public static Model BDI(Parameters p, int num, boolean fixed)
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
        
        Model m = null;
        try
        {
            m = new Model(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        if (fixed)
        {
            p.addParameter(Parameter.newEstimatedBoundedParameter("b",1e-4,Double.MAX_VALUE));
            m.setRescale(false);
        }
        else
        {
            p.addParameter(Parameter.newFixedParameter("b", 1.0));
        }
        //Bound parameters so they can't go to zero as this would create a sink state model
        p.addParameter(Parameter.newEstimatedBoundedParameter("d",1e-4,Double.MAX_VALUE));
        //Further bound the innovation parameter, otherwise can go to infinity
        //on some simulated datasets with very little change.  This is due
        //to there being zero probability of being in state zero at the root
        //and very little chance of changing to zero over the tree.  If the
        //simulated dataset has no zero innovation will go to infinity if
        //not bound.
        p.addParameter(Parameter.newEstimatedBoundedParameter("i",1e-4,10.0));
        
        return m;
    }
    
    public static Model BDI_Gamma(Parameters p, int num, int numCats)
    {
        return BDI_Gamma(p,num,numCats,false);
    }
    
    /**
     * Creates a simple BDI model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to
     * @param num The maximum family size
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model BDI_Gamma(Parameters p, int num, int numCats, boolean fixed)
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
        
        Model m = null;
        try
        {
            m = Model.gammaRates(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        if (fixed)
        {
            p.addParameter(Parameter.newEstimatedBoundedParameter("b",1e-4,Double.MAX_VALUE));
            m.setRescale(false);
        }
        else
        {
            p.addParameter(Parameter.newFixedParameter("b", 1.0));
        }
        //Bound parameters so they can't go to zero as this would create a sink state model
        p.addParameter(Parameter.newEstimatedBoundedParameter("d",1e-4,Double.MAX_VALUE));
        //Further bound the innovation parameter, otherwise can go to infinity
        //on some simulated datasets with very little change.  This is due
        //to there being zero probability of being in state zero at the root
        //and very little chance of changing to zero over the tree.  If the
        //simulated dataset has no zero innovation will go to infinity if
        //not bound.
        p.addParameter(Parameter.newEstimatedBoundedParameter("i",1e-4,10.0));        
        p.addParameter(Parameter.newEstimatedBoundedParameter("g", 0.2, 10.0));
        
        return m;
    }
    
    public static Model BD_NoZero(Parameters p, int num)
    {
        return BD_NoZero(p,num,false);
    }
    
    public static Model BD_NoZero(Parameters p, int num, boolean fixed)
    {
        String[][] matrix = new String[num][num];
        HashMap<String,Integer> map = new HashMap<>();
        for (int i = 0; i < num; i++)
        {
            map.put(Integer.toString(i+1), i);
        }
        for (int i = 0; i < num; i++)
        {
            for (int j = 0; j < num; j++)
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
            }
        }
        
        Model m = null;
        
        try
        {
            m = new Model(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        if (fixed)
        {
            p.addParameter(Parameter.newEstimatedBoundedParameter("b",1e-4,Double.MAX_VALUE));
            m.setRescale(false);
        }
        else
        {
            p.addParameter(Parameter.newFixedParameter("b", 1.0));
        }
        //Bound parameters so they can't go to zero as this would create a sink state model
        p.addParameter(Parameter.newEstimatedBoundedParameter("d",1e-2,Double.MAX_VALUE));
        
        return m;
    }
    
    public static Model BD_NoZero_Gamma(Parameters p, int num, int numCats)
    {
        return BD_NoZero_Gamma(p,num,numCats,false);
    }
    
    public static Model BD_NoZero_Gamma(Parameters p, int num, int numCats, boolean fixed)
    {
        String[][] matrix = new String[num][num];
        HashMap<String,Integer> map = new HashMap<>();
        for (int i = 0; i < num; i++)
        {
            map.put(Integer.toString(i+1), i);
        }
        for (int i = 0; i < num; i++)
        {
            for (int j = 0; j < num; j++)
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
            }
        }
        
        Model m = null;        
        try
        {
            m = Model.gammaRates(new RateCategory(matrix,RateCategory.FrequencyType.STATIONARY,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        
        if (fixed)
        {
            p.addParameter(Parameter.newEstimatedBoundedParameter("b",1e-4,Double.MAX_VALUE));
            m.setRescale(false);
        }
        else
        {
            p.addParameter(Parameter.newFixedParameter("b", 1.0));
        }
        //Bound parameters so they can't go to zero as this would create a sink state model
        p.addParameter(Parameter.newEstimatedBoundedParameter("d",1e-4,Double.MAX_VALUE));
    
        p.addParameter(Parameter.newEstimatedBoundedParameter("g", 0.2, 10.0));
        
        return m;
    }

}
