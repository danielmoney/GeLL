package Models;

import Exceptions.UnexpectedError;
import Parameters.Parameter;
import Parameters.Parameters;
import java.util.HashMap;

public class RYModelFactory
{
    public static Model RY(Parameters p)
    {
        p.addParameters(RY_Parameters());
        return RY();
    }

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
    
    public static Model RY_Gamma(Parameters p, int numCats)
    {
        p.addParameters(RY_Gamma_Parameters());
        return RY_Gamma(numCats);
    }

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
        p.addParameter(Parameter.newFixedParameter("pR",1.0));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pY"));
        return p;
    }
    
    private static Parameters RY_Gamma_Parameters()
    {
        Parameters p = RY_Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        return p;
    }    
}
