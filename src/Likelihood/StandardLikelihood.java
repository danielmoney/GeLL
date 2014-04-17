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
import java.io.Serializable;
import java.util.Map;

/**
 * Stores the results of a likelihood calculation.  As well as the overall
 * likelihood it stores the likelihood of each site and also of each missing
 * site.
 * @author Daniel Money
 * @version 2.0
 */

public class StandardLikelihood extends Likelihood implements Serializable
{
    StandardLikelihood(double l, Map<Site,SiteLikelihood> siteLikelihoods,
            Map<Site,SiteLikelihood> missingLikelihoods,
            Parameters p)
    {
        super(l,p);
        this.siteLikelihoods = siteLikelihoods;
        this.missingLikelihoods = missingLikelihoods;
    }
    
    /**
     * Gets the likelihood result for a given site
     * @param s The site to return the likelihood results for
     * @return The likelihood results for the given site
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if no likelihood
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
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if no likelihood
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
    
    private Map<Site,SiteLikelihood> siteLikelihoods;
    private Map<Site,SiteLikelihood> missingLikelihoods;
    
    private static final long serialVersionUID = 1;
}
