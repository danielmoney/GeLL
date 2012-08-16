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

import Alignments.Site;
import Likelihood.SiteLikelihood.LikelihoodException;
import Parameters.Parameters;
import Utils.ArrayMap;
import java.io.Serializable;

/**
 * Stores the results of a likelihood calculation.  As well as the overall
 * likelihood it stores the likelihood of each site and also of each missing
 * site.
 * @author Daniel Money
 * @version 1.3
 */

public class Likelihood implements Serializable, HasLikelihood
{
    Likelihood(double l, ArrayMap<Site,SiteLikelihood> siteLikelihoods,
            ArrayMap<Site,SiteLikelihood> missingLikelihoods,
            Parameters p)
    {
        this.l = l;
        this.siteLikelihoods = siteLikelihoods;
        this.missingLikelihoods = missingLikelihoods;
        this.p = p;
    }
    
    /**
     * Gets the total likelihood
     * @return The total likelihood
     */
    public double getLikelihood()
    {
        return l;
    }
    
    /**
     * Gets the likelihood result for a given site
     * @param s The site to return the likelihood results for
     * @return The likelihood results for the given site
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
     * has been calculated for the given site
     */
    public SiteLikelihood getSiteLikelihood(Site s) throws LikelihoodException
    {
        if (siteLikelihoods.containsKey(s))
        {
            return siteLikelihoods.get(s);
        }
        else
        {
            throw new LikelihoodException("No result for site: " + s);
        }
    }
    
    /**
     * Gets the likelihood result for a given missing site
     * @param s The missing site to return the likelihood results for
     * @return The likelihood results for the given missing site
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if no likelihood
     * has been calculated for the given site
     */
    public SiteLikelihood getMissingLikelihood(Site s) throws LikelihoodException
    {
        if (missingLikelihoods.containsKey(s))
        {
            return missingLikelihoods.get(s);
        }
        else
        {
            throw new LikelihoodException("No result for site: " + s);
        }
    }
    
    /**
     * Gets the parameters used to calculate this likelihood
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
    private ArrayMap<Site,SiteLikelihood> siteLikelihoods;
    private ArrayMap<Site,SiteLikelihood> missingLikelihoods;
    private Parameters p;
    
    private static final long serialVersionUID = 1;
}
