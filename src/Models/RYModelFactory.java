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
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import java.util.HashMap;

/**
 * A factory for easing the creation of the standard RY model
 * @author Daniel Money
 * @version 2.0
 */
public class RYModelFactory
{    
    /**
     * Creates an instance of a RY model
     * @param p Parameters structure to add the model parameters to (none in
     * this case but for consistency this is left here)
     * @return The model
     * @throws Parameters.Parameters.ParameterException Thrown if the name of one
     * of the parameters to be added by this method is already in use.
     */
    public static Model RY(Parameters p) throws ParameterException
    {
        p.addParameters(RY_Parameters());
        return RY();
    }

    /**
     * Creates an instance of a RY model
     * @return The model
     */
    public static Model RY()
    {
        String[][] ma = new String[2][2];
        ma[0][0] = "-"; ma[0][1] = "pY";
        ma[1][0] = "pR"; ma[1][1] = "-"; 
        
        String[] freq = {"pR", "pY"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("R",0);
        map.put("Y",1);

        try
        {
            return new Model(new RateCategory(ma,freq,map));
        }
        catch (RateCategory.RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }

    /**
     * Creates an instance of a RY model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to 
     * @param numCats The number of gamma categories to use
     * @return The model
     * @throws Parameters.Parameters.ParameterException Thrown if the name of one
     * of the parameters to be added by this method is already in use.
     */
    public static Model RY_Gamma(Parameters p, int numCats) throws ParameterException
    {
        p.addParameters(RY_Gamma_Parameters());
        return RY_Gamma(numCats);
    }

    /**
     * Creates an instance of a RY model with gamma-distributed rate
     * across sites
     * @param numCats The number of gamma categories to use
     * @return The model
     */      
    public static Model RY_Gamma(int numCats)
    {
        String[][] ma = new String[2][2];
        ma[0][0] = "-"; ma[0][1] = "pY";
        ma[1][0] = "pR"; ma[1][1] = "-";
        
        String[] freq = {"pR", "pY"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("R",0);
        map.put("Y",1);

        try
        {
            return Model.gammaRates(new RateCategory(ma,freq,map),"g",numCats);
        }
        catch (RateCategory.RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    private static Parameters RY_Parameters()
    {
        Parameters p = new Parameters();
        try
        {
            p.addParameter(Parameter.newFixedParameter("pR",1.0));
            p.addParameter(Parameter.newEstimatedPositiveParameter("pY"));
        }
        catch (ParameterException ex)
        {
            throw new UnexpectedError(ex);
        }
        return p;
    }
    
    private static Parameters RY_Gamma_Parameters()
    {
        Parameters p = RY_Parameters();
        try
        {
            p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        }
        catch (ParameterException ex)
        {
            throw new UnexpectedError(ex);
        }
        return p;
    }    
}
