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
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import java.util.HashMap;

/**
 * A factory for easing the creation of many standard DNA models
 * @author Daniel Money
 * @version 2.0
 */
public class DNAModelFactory
{
    /**
     * Constructor so that calls to {@link #getModel()} and {@link #getParameters()}
     * return the appropiate model or the appropiate parameters.
     * @param model The model
     */
    public DNAModelFactory(DNAModel model)
    {
        this.model = model;
        this.numCats = 1;
    }
    
    /**
     * Constructor so that calls to {@link #getModel()} and {@link #getParameters()}
     * return the appropiate model or the appropiate parameters.
     * @param model The model
     * @param numCats The number of categories for the model
     * @throws Models.Model.ModelException
     */
    public DNAModelFactory(DNAModel model, int numCats) throws ModelException
    {
        this.model = model;
        if (numCats < 1)
        {
            throw new ModelException("Models must have at least one category");
        }
        this.numCats = numCats;
    }
    
    /**
     * Creates an instance of a Jukes-Cantor model
     * @param p Parameters structure to add the model parameters to (none in
     * this case but for consistency this is left here)
     * @return The model
     */
    public static Model JukesCantor(Parameters p)
    {
        return JukesCantor();
    }

    /**
     * Creates an instance of a Jukes-Cantor model
     * @return The model
     */
    public static Model JukesCantor()
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "1.0"; ma[0][2] = "1.0"; ma[0][3] = "1.0";
        ma[1][0] = "1.0"; ma[1][1] = "-"; ma[1][2] = "1.0"; ma[1][3] = "1.0";
        ma[2][0] = "1.0"; ma[2][1] = "1.0"; ma[2][2] = "-"; ma[2][3] = "1.0";
        ma[3][0] = "1.0"; ma[3][1] = "1.0"; ma[3][2] = "1.0"; ma[3][3] = "-";
        
        String[] freq = {"0.25", "0.25", "0.25", "0.25"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        Model m = null;
        try
        {
            m = new Model(new RateCategory(ma,freq,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
        return m;
    }
    
    /**
     * Creates an instance of a Jukes-Cantor model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to 
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model JukesCantor_Gamma(Parameters p, int numCats)
    {
        p.addParameters(JukesCantor_Gamma_Parameters());
        return JukesCantor_Gamma(numCats);
    }

    /**
     * Creates an instance of a Jukes-Cantor model with gamma-distributed rate
     * across sites
     * @param numCats The number of gamma categories to use
     * @return The model
     */    
    public static Model JukesCantor_Gamma(int numCats)
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "1.0"; ma[0][2] = "1.0"; ma[0][3] = "1.0";
        ma[1][0] = "1.0"; ma[1][1] = "-"; ma[1][2] = "1.0"; ma[1][3] = "1.0";
        ma[2][0] = "1.0"; ma[2][1] = "1.0"; ma[2][2] = "-"; ma[2][3] = "1.0";
        ma[3][0] = "1.0"; ma[3][1] = "1.0"; ma[3][2] = "1.0"; ma[3][3] = "-";
        
        String[] freq = {"0.25", "0.25", "0.25", "0.25"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        try
        {
            return Model.gammaRates(new RateCategory(ma,freq,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    private static Parameters JukesCantor_Parameters()
    {
        return new Parameters();
    }
    
    private static Parameters JukesCantor_Gamma_Parameters()
    {
        Parameters p = JukesCantor_Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        return p;
    }
    
    /**
     * Creates an instance of a Kimura 2-paramter model
     * @param p Parameters structure to add the model parameters to 
     * @return The model
     */
    public static Model Kimura(Parameters p)
    {
        p.addParameters(Kimura_Parameters());
        return Kimura();
    }
    
    /**
     * Creates an instance of a Kimura 2-paramter model
     * @return The model
     */
    public static Model Kimura()
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "k"; ma[0][2] = "1.0"; ma[0][3] = "1.0";
        ma[1][0] = "k"; ma[1][1] = "-"; ma[1][2] = "1.0"; ma[1][3] = "1.0";
        ma[2][0] = "1.0"; ma[2][1] = "1.0"; ma[2][2] = "-"; ma[2][3] = "k";
        ma[3][0] = "1.0"; ma[3][1] = "1.0"; ma[3][2] = "k"; ma[3][3] = "-";
        
        String[] freq = {"0.25", "0.25", "0.25", "0.25"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);
        
        try
        {
            return new Model(new RateCategory(ma,freq,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Creates an instance of a Kimura 2-parameter model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to 
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model Kimura_Gamma(Parameters p, int numCats)
    {
        p.addParameters(Kimura_Gamma_Parameters());
        return Kimura_Gamma(numCats);
    }
    
    /**
     * Creates an instance of a Kimura 2-parameter model with gamma-distributed rate
     * across sites
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model Kimura_Gamma(int numCats)
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "k"; ma[0][2] = "1.0"; ma[0][3] = "1.0";
        ma[1][0] = "k"; ma[1][1] = "-"; ma[1][2] = "1.0"; ma[1][3] = "1.0";
        ma[2][0] = "1.0"; ma[2][1] = "1.0"; ma[2][2] = "-"; ma[2][3] = "k";
        ma[3][0] = "1.0"; ma[3][1] = "1.0"; ma[3][2] = "k"; ma[3][3] = "-";
        
        String[] freq = {"0.25", "0.25", "0.25", "0.25"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);        

        try
        {
            return Model.gammaRates(new RateCategory(ma,freq,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    private static Parameters Kimura_Parameters()
    {
        Parameters p = new Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("k"));
        return p;
    }
    
    private static Parameters Kimura_Gamma_Parameters()
    {
        Parameters p = Kimura_Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        return p;
    }
    
    /**
     * Creates an instance of a Felsenstein 81 model
     * @param p Parameters structure to add the model parameters to 
     * @return The model
     */
    public static Model Felsenstein81(Parameters p)
    {
        p.addParameters(Felsenstein81_Parameters());
        return Felsenstein81();
    }
    
    /**
     * Creates an instance of a Felsenstein 81 model
     * @return The model
     */
    public static Model Felsenstein81()
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "pC"; ma[0][2] = "pA"; ma[0][3] = "pG";
        ma[1][0] = "pT"; ma[1][1] = "-"; ma[1][2] = "pA"; ma[1][3] = "pG";
        ma[2][0] = "pT"; ma[2][1] = "pC"; ma[2][2] = "-"; ma[2][3] = "pG";
        ma[3][0] = "pT"; ma[3][1] = "pC"; ma[3][2] = "pA"; ma[3][3] = "-";
        
        String[] freq = {"pT", "pC", "pA", "pG"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);
        
        try
        {
            return new Model(new RateCategory(ma,freq,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Creates an instance of a JFelsenstein 81 model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to 
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model Felsenstein81_Gamma(Parameters p, int numCats)
    {
        p.addParameters(Felsenstein81_Gamma_Parameters());
        return Felsenstein81_Gamma(numCats);
    }

    /**
     * Creates an instance of a JFelsenstein 81 model with gamma-distributed rate
     * across sites
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model Felsenstein81_Gamma(int numCats)
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "pC"; ma[0][2] = "pA"; ma[0][3] = "pG";
        ma[1][0] = "pT"; ma[1][1] = "-"; ma[1][2] = "pA"; ma[1][3] = "pG";
        ma[2][0] = "pT"; ma[2][1] = "pC"; ma[2][2] = "-"; ma[2][3] = "pG";
        ma[3][0] = "pT"; ma[3][1] = "pC"; ma[3][2] = "pA"; ma[3][3] = "-";
        
        String[] freq = {"pT", "pC", "pA", "pG"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);
        
        try
        {
            return Model.gammaRates(new RateCategory(ma,freq,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    private static Parameters Felsenstein81_Parameters()
    {
        Parameters p = new Parameters();
        p.addParameter(Parameter.newFixedParameter("pT",1.0));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pC"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pA"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pG"));
        return p;
    }
    
    private static Parameters Felsenstein81_Gamma_Parameters()
    {
        Parameters p = Felsenstein81_Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        return p;
    }
    
    /**
     * Creates an instance of a HKY model
     * @param p Parameters structure to add the model parameters to 
     * @return The model
     */
    public static Model HKY(Parameters p)
    {
        p.addParameters(HKY_Parameters());
        return HKY();
    }            
            
    /**
     * Creates an instance of a HKY model
     * @return The model
     */
    public static Model HKY()
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "k*pC"; ma[0][2] = "pA"; ma[0][3] = "pG";
        ma[1][0] = "k*pT"; ma[1][1] = "-"; ma[1][2] = "pA"; ma[1][3] = "pG";
        ma[2][0] = "pT"; ma[2][1] = "pC"; ma[2][2] = "-"; ma[2][3] = "k*pG";
        ma[3][0] = "pT"; ma[3][1] = "pC"; ma[3][2] = "k*pA"; ma[3][3] = "-";
        
        String[] freq = {"pT", "pC", "pA", "pG"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);        

        try
        {
            return new Model(new RateCategory(ma,freq,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    /**
     * Creates an instance of a HKY model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to 
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model HKY_Gamma(Parameters p, int numCats)
    {
        p.addParameters(HKY_Gamma_Parameters());
        return HKY_Gamma(numCats);
    }
    
    /**
     * Creates an instance of a HKY model with gamma-distributed rate
     * across sites
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model HKY_Gamma(int numCats)
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "k*pC"; ma[0][2] = "pA"; ma[0][3] = "pG";
        ma[1][0] = "k*pT"; ma[1][1] = "-"; ma[1][2] = "pA"; ma[1][3] = "pG";
        ma[2][0] = "pT"; ma[2][1] = "pC"; ma[2][2] = "-"; ma[2][3] = "k*pG";
        ma[3][0] = "pT"; ma[3][1] = "pC"; ma[3][2] = "k*pA"; ma[3][3] = "-";
        
        String[] freq = {"pT", "pC", "pA", "pG"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);

        try
        {
            return Model.gammaRates(new RateCategory(ma,freq,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    private static Parameters HKY_Parameters()
    {
        Parameters p = new Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("k"));
        
        p.addParameter(Parameter.newFixedParameter("pT",1.0));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pC"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pA"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pG"));
        return p;
    }
    
    private static Parameters HKY_Gamma_Parameters()
    {
        Parameters p = HKY_Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        return p;
    }

    /**
     * Creates an instance of a General Time Reversable model
     * @param p Parameters structure to add the model parameters to 
     * @return The model
     */
    public static Model GTR(Parameters p)
    {
        p.addParameters(GTR_Parameters());
        return GTR();
    }
    
    /**
     * Creates an instance of a General Time Reversable model
     * @return The model
     */
    public static Model GTR()
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "a*pC"; ma[0][2] = "b*pA"; ma[0][3] = "c*pG";
        ma[1][0] = "a*pT"; ma[1][1] = "-"; ma[1][2] = "d*pA"; ma[1][3] = "e*pG";
        ma[2][0] = "b*pT"; ma[2][1] = "d*pC"; ma[2][2] = "-"; ma[2][3] = "pG";
        ma[3][0] = "c*pT"; ma[3][1] = "e*pC"; ma[3][2] = "pA"; ma[3][3] = "-";
        
        String[] freq = {"pT", "pC", "pA", "pG"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);
        
        try
        {
            return new Model(new RateCategory(ma,freq,map));
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }    
    
    /**
     * Creates an instance of a General Time Reversable model with gamma-distributed rate
     * across sites
     * @param p Parameters structure to add the model parameters to 
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model GTR_Gamma(Parameters p, int numCats)
    {
        p.addParameters(GTR_Gamma_Parameters());
        return GTR_Gamma(numCats);
    }
    
    /**
     * Creates an instance of a General Time Reversable model with gamma-distributed rate
     * across sites
     * @param numCats The number of gamma categories to use
     * @return The model
     */
    public static Model GTR_Gamma(int numCats)
    {
        String[][] ma = new String[4][4];
        ma[0][0] = "-"; ma[0][1] = "a*pC"; ma[0][2] = "b*pA"; ma[0][3] = "c*pG";
        ma[1][0] = "a*pT"; ma[1][1] = "-"; ma[1][2] = "d*pA"; ma[1][3] = "e*pG";
        ma[2][0] = "b*pT"; ma[2][1] = "d*pC"; ma[2][2] = "-"; ma[2][3] = "f*pG";
        ma[3][0] = "c*pT"; ma[3][1] = "e*pC"; ma[3][2] = "f*pA"; ma[3][3] = "-";
        
        String[] freq = {"pT", "pC", "pA", "pG"};

        HashMap<String,Integer> map = new HashMap<>();
        map.put("T",0);
        map.put("C",1);
        map.put("A",2);
        map.put("G",3);
        
        try
        {
            return Model.gammaRates(new RateCategory(ma,freq,map),"g",numCats);
        }
        catch (RateException ex)
        {
            //Shouldn't get here as we've cretaed the rate category but just in
            //case...
            throw new UnexpectedError(ex);
        }
    }
    
    private static Parameters GTR_Gamma_Parameters()
    {
        Parameters p = GTR_Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("g"));
        return p;
    }
    
    private static Parameters GTR_Parameters()
    {
        Parameters p = new Parameters();
        p.addParameter(Parameter.newEstimatedPositiveParameter("a"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("b"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("c"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("d"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("e"));
        p.addParameter(Parameter.newFixedParameter("f",1.0));
        
        p.addParameter(Parameter.newFixedParameter("pT",1.0));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pC"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pA"));
        p.addParameter(Parameter.newEstimatedPositiveParameter("pG"));

        return p;
    }
    
    /**
     * Returns a new instance of the relevant model
     * @return The new model
     */
    public Model getModel()
    {
        if (numCats > 1)
        {
            switch (model)
            {
                case F81:
                    return Felsenstein81_Gamma(numCats);
                case GTR:
                    return GTR_Gamma(numCats);
                case HKY:
                    return HKY_Gamma(numCats);
                case JC:
                    return JukesCantor_Gamma(numCats);
                case K2P:
                    return Kimura_Gamma(numCats);
            }
        }
        else
        {
            switch (model)
            {
                case F81:
                    return Felsenstein81();
                case GTR:
                    return GTR();
                case HKY:
                    return HKY();
                case JC:
                    return JukesCantor();
                case K2P:
                    return Kimura();
            }
        }
        throw new UnexpectedError();
    }
    
    /**
     * Gets the parameters for the relevant model
     * @return The parameters
     */
    public Parameters getParameters()
    {
        if (numCats > 1)
        {
            switch (model)
            {
                case F81:
                    return Felsenstein81_Gamma_Parameters();
                case GTR:
                    return GTR_Gamma_Parameters();
                case HKY:
                    return HKY_Gamma_Parameters();
                case JC:
                    return JukesCantor_Gamma_Parameters();
                case K2P:
                    return Kimura_Gamma_Parameters();
            }
        }
        else
        {
            switch (model)
            {
                case F81:
                    return Felsenstein81_Parameters();
                case GTR:
                    return GTR_Parameters();
                case HKY:
                    return HKY_Parameters();
                case JC:
                    return JukesCantor_Parameters();
                case K2P:
                    return Kimura_Parameters();
            }
        }
        throw new UnexpectedError();
    }
    
    private DNAModel model;
    private int numCats;
    
    /**
     * Represents the various types of DNA models
     */
    public enum DNAModel
    {
        /**
         * Felsenstein 81 model
         */
        F81,
        /**
         * General Time Reversible (GTR) model
         */
        GTR,
        /**
         * The HKY model
         */
        HKY,
        /**
         * The Jukes-Cantor model
         */
        JC,
        /**
         * The Kimura 2-parameter model
         */
        K2P
    }
}
