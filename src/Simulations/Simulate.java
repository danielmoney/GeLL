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

package Simulations;

import Alignments.Site;
import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Ambiguous;
import Exceptions.GeneralException;
import Likelihood.Probabilities;
import Parameters.Parameters;
import Models.RateCategory;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

/**
 * Class for constructing simulated data
 * @author Daniel Money
 * @version 2.0
 */
public class Simulate
{
    /**
     * Creates an object to simulate data for a given model, tree and parameters.
     * Has no unobserved states.
     * @param m The model
     * @param t The tree
     * @param p The parameters
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a required parameter is not present) 
     */
    public Simulate(Model m, Tree t, Parameters p) throws RateException, ModelException, TreeException, ParameterException
    {
        this(m,t,p,null);
    }


    /**
     * Creates an object to simulate data for a given model, tree, parameters
     * and unobserved states.
     * @param m The model
     * @param t The tree
     * @param p The parameters
     * @param unobserved The unobserved states
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a required parameter is not present) 
     */
    public Simulate(Model m, Tree t, Parameters p, Alignment unobserved) throws RateException, ModelException, TreeException, ParameterException
    {
        this.P = new HashMap<>(); 
        P.put(null,new Probabilities(m,t,p));
        this.missing = unobserved;
        
        this.t = new HashMap<>();
        this.t.put(null,t);
        
        random = new Random();
        
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Branch b: t)
	{
            if (!p.hasParam(b.getChild()))
            {
                p.addParameter(Parameter.newFixedParameter(b.getChild(),
                   b.getLength()));
            }
	}
    }
    
    /**
     * Creates an object to simulate data for a given set of models, a tree and parameters.
     * Has no unobserved states.  A different model can be given
     * for each rate class.
     * @param m Map from site class to model
     * @param t The tree
     * @param p The parameters
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a required parameter is not present) 
     */    
    public Simulate(Map<String,Model> m, Tree t, Parameters p) throws RateException, ModelException, TreeException, ParameterException
    {
        this(m,t,p,null);
    }

    /**
     * Creates an object to simulate data for a given set of models, a tree, parameters
     * and unobserved states. A different model can be given
     * for each rate class.
     * @param m Map from site class to model
     * @param t The tree
     * @param p The parameters
     * @param unobserved The unobserved states
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a required parameter is not present) 
     */     
    public Simulate(Map<String,Model> m, Tree t, Parameters p, Alignment unobserved) throws RateException, ModelException, TreeException, ParameterException
    {
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t,p));
        }
        this.missing = unobserved;
        
        this.t = new HashMap<>();
        for (String s: m.keySet())
        {
            this.t.put(s,t);
        }
        
        random = new Random();
        
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Branch b: t)
	{
            if (!p.hasParam(b.getChild()))
            {
                p.addParameter(Parameter.newFixedParameter(b.getChild(),
                   b.getLength()));
            }
	}        
    }

    public Simulate(Model m, Map<String,Tree> t, Parameters p) throws RateException, ModelException, TreeException, ParameterException
    {
        this(m,t,p,null);
    }

    public Simulate(Model m, Map<String,Tree> t, Parameters p, Alignment unobserved) throws RateException, ModelException, TreeException, ParameterException
    {
        for (Entry<String,Tree> e: t.entrySet())
        {
            P.put(e.getKey(),new Probabilities(m,e.getValue(),p));
        }
        this.missing = unobserved;
        
        this.t = t;
        
        random = new Random();
        
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Tree tt: t.values())
        {
            for (Branch b: tt)
            {
                if (!p.hasParam(b.getChild()))
                {
                    p.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));
                }
            }
        }
    }
    
    public Simulate(Map<String,Model> m, Map<String,Tree> t, Parameters p) throws RateException, ModelException, TreeException, ParameterException
    {
        this(m,t,p,null);
    }

    public Simulate(Map<String,Model> m, Map<String,Tree> t, Parameters p, Alignment unobserved) throws RateException, ModelException, TreeException, ParameterException
    {
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t.get(e.getKey()),p));
        }
        this.missing = unobserved;
        
        this.t = t;
        
        random = new Random();
        
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Tree tt: t.values())
        {
            for (Branch b: tt)
            {
                if (!p.hasParam(b.getChild()))
                {
                    p.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));
                }
            }
        }
    }    

    
    /**
     * Gets a simulated site without returning the state of the internal nodes
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite() throws SimulationException, RateException
    {
	return getSite(false, null, null);
    }
    
    /**
     * Gets a simulated site without returning the state of the internal nodes
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite(String siteClass) throws SimulationException, RateException
    {
	return getSite(false, null, siteClass);
    }
    
    /**
     * Gets a simulated site without returning the state of the internal nodes.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param recode Map of recodings
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite(Map<String,String> recode) throws SimulationException, RateException
    {
	return getSite(false, recode, null);
    }
    
    /**
     * Gets a simulated site without returning the state of the internal nodes.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param recode Map of recodings
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite(Map<String,String> recode, String siteClass) throws SimulationException, RateException
    {
	return getSite(false, recode, siteClass);
    }

    /**
     * Gets a simulated site
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite(boolean internal) throws SimulationException, RateException
    {
	return getSite(internal, null, null);
    }
 
    /**
     * Gets a simulated site
     * @param internal Whether to return the state of the internal nodes
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite(boolean internal, String siteClass) throws SimulationException, RateException
    {
	return getSite(internal, null, siteClass);
    }

    /**
     * Gets a simulated site.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param recode Map of recodings
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Site getSite(boolean internal, Map<String, String> recode) throws SimulationException, RateException
    {
        return getSite(internal, recode, null);
    }
    
      /**
     * Gets a simulated site.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param recode Map of recodings
     * @param internal Whether to return the state of the internal nodes
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */  
    public Site getSite(boolean internal, Map<String, String> recode, String siteClass) throws SimulationException, RateException
    {
        if (!P.containsKey(siteClass))
        {
            throw new SimulationException("No model defined for requested class");
        }
        if (!t.containsKey(siteClass))
        {
            throw new SimulationException("No tree defined for requested class");
        }
	Site site, loSite;
        do
        {
            HashMap<String,String> assign = new HashMap<>();

            RateCategory r = getRandomRate(P.get(siteClass).getRateCategory(),siteClass);

            //Assign the root
            assign.put(t.get(siteClass).getRoot(), getRandomStart(r, siteClass));

            //Traverse the tree, assign values to nodes
            for (Branch b: t.get(siteClass).getBranchesReversed())
            {
                assign.put(b.getChild(), getRandomChar(
                        r,b,assign.get(b.getParent()),siteClass));
            }

            //Done like this so things are in a sensible order if written out
            //Keeps a leaf only and all nodes copy.
            LinkedHashMap<String,String> all = new LinkedHashMap<>();
            LinkedHashMap<String,String> lo = new LinkedHashMap<>();

            for (String l: t.get(siteClass).getLeaves())
            {
                all.put(l, assign.get(l));
                lo.put(l, assign.get(l));
            }
            for (String i: t.get(siteClass).getInternal())
            {
                all.put(i, assign.get(i));
            }

            //This deals with recoding as discussed in the javadoc.  If there
            //is none simply ceate the site
            if (recode == null)
            {
                site = new Site(all,siteClass);
                loSite = new Site(lo,siteClass);
            }           
            else
            {
                //Else make an ambiguous data structure
                Map<String,Set<String>> ambig = new HashMap<>();
                //Step through the recodings and add the apropiate date to
                //ambig
                for (Entry<String,String> e: recode.entrySet())
                {
                    if (!ambig.containsKey(e.getValue()))
                    {
                        ambig.put(e.getValue(),new HashSet<String>());
                    }
                    ambig.get(e.getValue()).add(e.getKey());
                }

                //Create the sites
                site = new Site(all, new Ambiguous(ambig), siteClass);
                loSite = new Site(lo, new Ambiguous(ambig), siteClass);

                //Now recode them
                site = site.recode(recode);
                loSite = loSite.recode(recode);
            }            
        }
        //While the site is missing generate another site
        while (isMissing(loSite));
        
        if (internal)
        {
            return site;
        }
        else
        {
            return loSite;
        }
    }

    private boolean isMissing(Site s)
    {
        //Checks whether a site we've generated is missing data
        if (missing == null)
        {
            return false;
        }
        for (Site ms: missing.getUniqueSites())
        {
            boolean match = true;
            for (String taxa: s.getTaxa())
            {
                if (!ms.getCharacter(taxa).containsAll(s.getCharacter(taxa)))
                {
                    match = false;
                    break;
                }
            }
            if (match)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a simulated alignment, not returning the state of internal nodes
     * @param length The length of the alignment
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(int length) throws AlignmentException, SimulationException, RateException
    {
	return getAlignment(length,false,null);
    }
    
    /**
     * Gets a simulated alignment
     * @param length The length of the alignment
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(int length, boolean internal) throws AlignmentException, SimulationException, RateException
    {
        return getAlignment(length,internal,null);
    }

    /**
     * Gets a simulated alignment, not returning the state of internal nodes.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param length The length of the alignment
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(int length, Map<String,String> recode) throws AlignmentException, SimulationException, RateException
    {
	return getAlignment(length,false,recode);
    }

    /**
     * Gets a simulated alignment.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param length The length of the alignment
     * @param internal Whether to return the state of the internal nodes
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(int length, boolean internal, Map<String,String> recode) 
            throws AlignmentException, SimulationException, RateException
    {
	List<Site> data = new ArrayList<>();

	for (int i=0; i < length; i++)
	{
	    data.add(getSite(internal, recode));
	}

	return new Alignment(data);
    }
    
    /**
     * Gets a simulated alignment, not returning the state of the internal nodes.
     * The simulated alignment will be the same length
     * as siteClasses and each site will have been simulated according to the corresponding
     * class in siteClasses.
     * @param siteClasses List of site classes used to generate the alignment
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(List<String> siteClasses) 
            throws AlignmentException, SimulationException, RateException
    {
        return getAlignment(siteClasses,false,null);
    }
    
    /**
     * Gets a simulated alignment.
     * The simulated alignment will be the same length
     * as siteClasses and each site will have been simulated according to the corresponding
     * class in siteClasses.
     * @param siteClasses List of site classes used to generate the alignment
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(List<String> siteClasses, boolean internal) 
            throws AlignmentException, SimulationException, RateException
    {
        return getAlignment(siteClasses,internal, null);
    }
 
    /**
     * Gets a simulated alignment, not returning the state of the internal nodes.
     * The simulated alignment will be the same length
     * as siteClasses and each site will have been simulated according to the corresponding
     * class in siteClasses.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param siteClasses List of site classes used to generate the alignment
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(List<String> siteClasses, Map<String,String> recode) 
            throws AlignmentException, SimulationException, RateException
    {
        return getAlignment(siteClasses,false,recode);
    }
    
    /**
     * Gets a simulated alignment.  The simulated alignment will be the same length
     * as siteClasses and each site will have been simulated according to the corresponding
     * class in siteClasses.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is necessary as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropriate.
     * @param siteClasses List of site classes used to generate the alignment
     * @param internal Whether to return the state of the internal nodes
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model for)
     * @throws Models.RateCategory.RateException if a rate category uses the FitzJohn
     * method at the root as this method requires likelihoods to calculate the frequency
     * and we don't have the likelihoods when simulating.
     */
    public Alignment getAlignment(List<String> siteClasses, boolean internal, Map<String,String> recode) 
            throws AlignmentException, SimulationException, RateException
    {
	List<Site> data = new ArrayList<>();

	for (String c: siteClasses)
	{
	    data.add(getSite(internal, recode, c));
	}

	return new Alignment(data);
    }
    
    /**
     * Seeds the random number generator with the give seed.  Useful for
     * testing or in the rare cases where repeatable results are required.
     * @param seed The seed to be used
     */
    public void setSeed(long seed)
    {
        random.setSeed(seed);
    }

    private String getRandomChar(RateCategory r, Branch b, String start, String siteClass)
    {
        //Gets a random character at the other end of a branch given the rate category,
        //branch and start state
	double tot = 0.0;
	double v = random.nextDouble();
	String ret = null;

        for (String s: P.get(siteClass).getAllStates())
	{
	    if (tot <= v)
	    {
		ret  = s;
	    }
            //As we're traversing the tree the opposite way start is end!
            tot = tot + P.get(siteClass).getP(r).getP(b, s, start);
	}
        
	return ret;
    }

    private RateCategory getRandomRate(Set<RateCategory> rates, String siteClass)
    {
        //Get a random rate category
	double tot = 0.0;
	double v = random.nextDouble();
	RateCategory ret = null;

	for (RateCategory r: rates)
	{
	    if (tot <= v)
	    {
		ret  = r;
	    }
	    tot = tot + P.get(siteClass).getRateP(r);
	}
        
	return ret;
    }

    private String getRandomStart(RateCategory r, String siteClass) throws RateException
    {
        //Get a random root assignment
	double tot = 0.0;
	double v = random.nextDouble();
	String ret = null;

        for (String s: P.get(siteClass).getAllStates())
	{
	    if (tot <= v)
	    {
		ret  = s;
	    }
	    //tot = tot + P.get(siteClass).getFreq(r, s);
            tot = tot + P.get(siteClass).getRoot(r).getFreq(s);
	}
        
	return ret;
    }

    private Map<String,Tree> t;
    private Random random;
    private Alignment missing;
    private Map<String,Probabilities> P;
    
    /**
     * Exception for when there is a problem with the simulation
     */
    public class SimulationException extends GeneralException
    {
        /**
         * Constructor
         * @param msg The cause of the exception
         */
        public SimulationException(String msg)
        {
            super(msg,null);
        }
    }
}
