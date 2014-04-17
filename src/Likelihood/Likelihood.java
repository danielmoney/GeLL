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

package Likelihood;

import Parameters.Parameters;

/**
 * Represents the simplest possible likelihood result, i.e. the likelihood
 * and the parameters used
 * @author Daniel Money
 * @version 2.0
 */
public class Likelihood
{
    /**
     * Creates a simple likelihood result
     * @param likelihood The likelihood
     * @param p The parameters used to calculate the likelihood
     */
    public Likelihood(double likelihood, Parameters p)
    {
        this.l = likelihood;
        this.p = p;
    }
    
    /**
     * Gets the likelihood
     * @return The likelihood
     */
    public double getLikelihood()
    {
        return l;
    }
    
    /**
     * Gets the parameters used to calculate the likelihood
     * @return The parameters
     */
    public Parameters getParameters()
    {
        return p;
    }    
        
    public String toString()
    {
        return Double.toString(l);
    }
    
    private double l;
    private Parameters p;
}
