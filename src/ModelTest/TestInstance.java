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

package ModelTest;

import Alignments.Alignment;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Parameters.Parameters;

/**
 * Represents a particular instance (calculator and parameters) to be tested
 * to find the best performing.  Also used to include an adapter (see Whelan
 * 2015) if required.
 * @author Daniel Money
 * @version 2.0
 */
public class TestInstance
{

    /**
     * Creates an instance
     * @param c The calculator
     * @param p The parameters
     */
    public TestInstance(Calculator<Likelihood> c, Parameters p)
    {
        this(c,p,"",new IdentityAdapter(),null);
    }

    /**
     * Creates a named instance.  The name can be used to ease identification
     * of the best instance found.
     * @param c The calculator
     * @param p The parameters
     * @param name Name for the instance
     */    
    public TestInstance(Calculator<Likelihood> c, Parameters p, String name)
    {
        this(c,p,name,new IdentityAdapter(),null);
    }

    /**
     * Creates a named instance.  The name can be used to ease identification
     * of the best instance found.
     * @param c The calculator
     * @param p The parameters
     * @param adapter The adapter to be used.  See Whelan et al 2015 for more.
     * @param distinct The comparable distinct alignment. See Whelan et al 2015
     * for more
     */    
    public TestInstance(Calculator<Likelihood> c, Parameters p, Adapter adapter,
            Alignment distinct)
    {
        this(c,p,"",adapter,distinct);
    }

    /**
     * Creates a named instance.  The name can be used to ease identification
     * of the best instance found.
     * @param c The calculator
     * @param p The parameters
     * @param name Name for the instance
     * @param adapter The adapter to be used.  See Whelan et al 2015 for more.
     * @param distinct The comparable distinct alignment. See Whelan et al 2015
     * for more
     */        
    public TestInstance(Calculator<Likelihood> c, Parameters p, String name, Adapter adapter,
            Alignment distinct)
    {
        this.c = c;
        this.p = p;
        this.name = name;
        this.adapterL = adapter.likelihood(distinct);
        this.adapterParams = adapter.numberParameters();
    }
    
    /**
     * Get the calculator associated with this instance
     * @return The calculator
     */
    public Calculator<Likelihood> getCalculator()
    {
        return c;
    }

    /**
     * Get the parameters associated with this instance
     * @return The parameters
     */
    public Parameters getParameters()
    {
        return p;
    }

    /**
     * Get the name of this instance
     * @return The name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the number of parameters associated with the adapter for this instance
     * @return The number of parameters
     */
    public int getNumberAdapterParams()
    {
        return adapterParams;
    }

    /**
     * Get the likelihood of the adapter associated with this instance
     * @return The likelihood
     */    
    public double getAdapterL()
    {
        return adapterL;
    }
    
    private double adapterL;
    private int adapterParams;
    private Calculator<Likelihood> c;
    private Parameters p;
    private String name;
}
