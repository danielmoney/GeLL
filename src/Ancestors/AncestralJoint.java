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

package Ancestors;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Site;
import Ancestors.AncestralJointDP.MultipleRatesException;
import Likelihood.Probabilities;
import Likelihood.SiteLikelihood.LikelihoodException;
import Models.Model;
import Parameters.Parameters;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameters.ParameterException;
import Trees.Tree;
import Trees.TreeException;
import java.util.Map;

/**
 * Abstract class that the two different ways of doing ancestral reconstruction
 * extend.  By using the static method in tis class users can avoid worrying
 * about whether their model has a single rate category.
 * @author Daniel Money
 * @version 2.0
 */
public abstract class AncestralJoint
{
    /**
     * Calculates the reconstruction
     * @param params The parameters to be used in the reconstruction
     * @return An alignment based on the original alignment but augmented with
     * the reconstruction
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws AncestralException Thrown if there is ambiguous data as these methods
     * can't currently deasl with it
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Likelihood.SiteLikelihood.LikelihoodException Thrown if there is a problem initialise likelihoods 
     */
    public abstract Alignment calculate(Parameters params) throws RateException, ModelException, AncestralException, TreeException, ParameterException, AlignmentException, LikelihoodException;

    abstract Site calculateSite(Site s, Probabilities P) throws AncestralException, TreeException, LikelihoodException;
    
    /**
     * Returns an object of this class that can be used for joint reconstruction.
     * If the model has a single rate category it returns an object that will do
     * reconstruction using the dynamic pogramming method of Pupko 2000, else
     * an object that will use the branch and bound method of Pupoko 2002.      
     * @param a The alignment
     * @param m The model
     * @param t The tree
     * @return An object that can be used for reconstruction
     */
    public static AncestralJoint newInstance(Model m, Alignment a, Tree t)
    {
        //First try Dynamic Programming method
        try
        {
            return new AncestralJointDP(m,a,t);
        }
        //But if that can't work due to multiple rate categories use Branch
        //and Bound method
        catch (MultipleRatesException ex)
        {
            return new AncestralJointBB(m,a,t);
        }
    }
    
    /**
     * Returns an object of this class that can be used for joint reconstruction.
     * Sites can belong to different classes and so use different models.
     * If all models have a single rate category it returns an object that will do
     * reconstruction using the dynamic pogramming method of Pupko 2000, else
     * an object that will use the branch and bound method of Pupoko 2002. 
     * @param a The alignment
     * @param m Map from site class to model
     * @param t The tree
     * @return An object that can be used for reconstruction
     * @throws AlignmentException Thrown if a model and constrainer isn't given
     * for each site class in the alignment  
     */
    public static AncestralJoint newInstance(Map<String,Model> m, Alignment a, Tree t) throws AlignmentException
    {
        //First try Dynamic Programming method
        try
        {
            return new AncestralJointDP(m,a,t);
        }
        //But if that can't work due to multiple rate categories use Branch
        //and Bound method
        catch (MultipleRatesException ex)
        {
            return new AncestralJointBB(m,a,t);
        }
    }
}
