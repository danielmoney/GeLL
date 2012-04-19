package Simulations;

import Alignments.Site;
import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Ambiguous;
import Constraints.Constrainer;
import Constraints.NoConstraints;
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
 * @version 1.2
 */
public class Simulate
{
    /**
     * Creates an object to simulate data for a given model, tree and parameters.
     * Has no unobserved states or constraints.
     * @param m The model
     * @param t The tree
     * @param p The parameters
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     */
    public Simulate(Model m, Tree t, Parameters p) throws RateException, ModelException, TreeException, ParameterException
    {
	this(m,t,p,null,new NoConstraints(m.getStates()));
    }
    
    /**
     * Creates an object to simulate data for a given model, tree, parameters
     * and unobserved states.  Has no constraints.
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
     * with the parameters (e.g. a requied parameter is not present) 
     */
    public Simulate(Model m, Tree t, Parameters p, Alignment unobserved) throws RateException, ModelException, TreeException, ParameterException
    {
        this(m,t,p,unobserved,new NoConstraints(m.getStates()));
    }

    /**
     * Creates an object to simulate data for a given model, tree, parameters,
     * unobserved states and constraints.  If a site is generated that does not
     * meet the constraints then it is discarded and a new site generated.
     * @param m The model
     * @param t The tree
     * @param p The parameters
     * @param unobserved The unobserved states
     * @param con The constraints
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     */
    public Simulate(Model m, Tree t, Parameters p, Alignment unobserved, Constrainer con) throws RateException, ModelException, TreeException, ParameterException
    {
        this.P = new HashMap<>(); 
        P.put(null,new Probabilities(m,t,p));
        this.missing = unobserved;
        
        this.t = t;
        
        random = new Random();
        
        this.con = new HashMap<>();
        this.con.put(null,con);
        
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
     * Has no unobserved states or constraints.  A different model can be given
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
     * with the parameters (e.g. a requied parameter is not present) 
     */    
    public Simulate(Map<String,Model> m, Tree t, Parameters p) throws RateException, ModelException, TreeException, ParameterException
    {
        this(m,t,p,null);
    }

    /**
     * Creates an object to simulate data for a given set of models, a tree, parameters
     * and unobserved sattes.  Has no constraints.  A different model can be given
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
     * with the parameters (e.g. a requied parameter is not present) 
     */     
    public Simulate(Map<String,Model> m, Tree t, Parameters p, Alignment unobserved) throws RateException, ModelException, TreeException, ParameterException
    {
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t,p));
        }
        this.missing = unobserved;
        
        this.t = t;
        
        random = new Random();
        
        con = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            con.put(e.getKey(),new NoConstraints(e.getValue().getStates()));
        }
        
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
     * Creates an object to simulate data for a given set of models, a tree, parameters,
     * unobserved states and constrainers.  A different model and constrainer can be given
     * for each rate class.
     * @param m Map from site class to model
     * @param t The tree
     * @param p The parameters
     * @param con Map from rate site class to constrainer
     * @param unobserved The unobserved states
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws TreeException Thrown if there is a problem with the tree.
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present) 
     */  
    public Simulate(Map<String,Model> m, Tree t, Parameters p, Alignment unobserved, Map<String,Constrainer> con) throws RateException, ModelException, TreeException, ParameterException
    {
        P = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t,p));
        }
        this.missing = unobserved;
        
        this.t = t;
        
        random = new Random();
        
        this.con = con;
        
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
     * Gets a simulated site without returning the state of the internal nodes
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite() throws TreeException, SimulationException
    {
	return getSite(false, null, null);
    }
    
    /**
     * Gets a simulated site without returning the state of the internal nodes
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite(String siteClass) throws TreeException, SimulationException
    {
	return getSite(false, null, siteClass);
    }
    
    /**
     * Gets a simulated site without returning the state of the internal nodes.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param recode Map of recodings
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite(Map<String,String> recode) throws TreeException, SimulationException
    {
	return getSite(false, recode, null);
    }
    
    /**
     * Gets a simulated site without returning the state of the internal nodes.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param recode Map of recodings
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite(Map<String,String> recode, String siteClass) throws TreeException, SimulationException
    {
	return getSite(false, recode, siteClass);
    }

    /**
     * Gets a simulated site
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite(boolean internal) throws TreeException, SimulationException
    {
	return getSite(internal, null, null);
    }
 
    /**
     * Gets a simulated site
     * @param internal Whether to return the state of the internal nodes
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite(boolean internal, String siteClass) throws TreeException, SimulationException
    {
	return getSite(internal, null, siteClass);
    }

    /**
     * Gets a simulated site.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param recode Map of recodings
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Site getSite(boolean internal, Map<String, String> recode) throws TreeException, SimulationException
    {
        return getSite(internal, recode, null);
    }
    
      /**
     * Gets a simulated site.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param recode Map of recodings
     * @param internal Whether to return the state of the internal nodes
     * @param siteClass The site class to simulate for
     * @return The simulated site
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */  
    public Site getSite(boolean internal, Map<String, String> recode, String siteClass) throws TreeException, SimulationException
    {
        if (!P.containsKey(siteClass))
        {
            throw new SimulationException("No model defined for requested class");
        }
        if (!con.containsKey(siteClass))
        {
            throw new SimulationException("No constraints defineed for requested class");
        }
	Site site, loSite;
	do
	{
            HashMap<String,String> assign = new HashMap<>();

	    RateCategory r = getRandomRate(P.get(siteClass).getRateCategory(),siteClass);

            //Assign the root
            assign.put(t.getRoot(), getRandomStart(r, siteClass));

            //Traverse the tree, assign values to nodes
            for (Branch b: t.getBranchesReversed())
            {
                assign.put(b.getChild(), getRandomChar(
                        r,b,assign.get(b.getParent()),siteClass));
            }
            
            //Done like this so things are in a sensible order if written out
            //Keeps a leaf only and all nodes copy.
            LinkedHashMap<String,String> all = new LinkedHashMap<>();
            LinkedHashMap<String,String> lo = new LinkedHashMap<>();
            
            for (String l: t.getLeaves())
            {
                all.put(l, assign.get(l));
                lo.put(l, assign.get(l));
            }
            for (String i: t.getInternal())
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
                site = new Site(all, new Ambiguous(ambig));
                loSite = new Site(lo, new Ambiguous(ambig));
                
                //Now recode them
                site = site.recode(recode);
                loSite = site.recode(recode);
            }            
	}
        //While the site is missing or it does not meet the constraints generate
        //another site
	while (isMissing(loSite) || !con.get(siteClass).getConstraints(t, loSite).meetsConstrains(site));
        
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
     * Gets a simulated alignment, not returing the state of internal nodes
     * @param length The length of the alignment
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Alignment getAlignment(int length) throws AlignmentException, TreeException, SimulationException
    {
	return getAlignment(length,false,null);
    }
    
    /**
     * Gets a simulated alignment
     * @param length The length of the alignment
     * @param internal Whether to return the state of the internal nodes
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Alignment getAlignment(int length, boolean internal) throws AlignmentException, TreeException, SimulationException
    {
        return getAlignment(length,internal,null);
    }

    /**
     * Gets a simulated alignment, not returing the state of internal nodes.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param length The length of the alignment
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for) 
     */
    public Alignment getAlignment(int length, Map<String,String> recode) throws AlignmentException, TreeException, SimulationException
    {
	return getAlignment(length,false,recode);
    }

    /**
     * Gets a simulated alignment.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param length The length of the alignment
     * @param internal Whether to return the state of the internal nodes
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     */
    public Alignment getAlignment(int length, boolean internal, Map<String,String> recode) 
            throws AlignmentException, TreeException, SimulationException
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
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     */
    public Alignment getAlignment(List<String> siteClasses) 
            throws AlignmentException, TreeException, SimulationException
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
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     */
    public Alignment getAlignment(List<String> siteClasses, boolean internal) 
            throws AlignmentException, TreeException, SimulationException
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
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param siteClasses List of site classes used to generate the alignment
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     */
    public Alignment getAlignment(List<String> siteClasses, Map<String,String> recode) 
            throws AlignmentException, TreeException, SimulationException
    {
        return getAlignment(siteClasses,false,recode);
    }
    
    /**
     * Gets a simulated alignment.  The simulated alignment will be the same length
     * as siteClasses and each site will have been simulated according to the corresponding
     * class in siteClasses.
     * Recodes the simulated data before returning.  For example if both "A" and
     * "B" are unobserved states representing an observed state of "0" then this
     * can be used to change "A" and "B" to zero.  This is neccessay as the simulator
     * generates unobserved states by default.  The returned site will have ambiguous
     * data set as appropiate.
     * @param siteClasses List of site classes used to generate the alignment
     * @param internal Whether to return the state of the internal nodes
     * @param recode Map of recodings
     * @return The simulated alignment
     * @throws AlignmentException Thrown if there is a problem with the alignment
     * @throws TreeException Thrown if the constrainer has a problem with the tree
     * @throws Simulations.Simulate.SimulationException Thrown if there is a problem
     * with the simulation (currently only if attempting to simulate for a site class
     * we don't have a model and / or constraints for)
     */
    public Alignment getAlignment(List<String> siteClasses, boolean internal, Map<String,String> recode) 
            throws AlignmentException, TreeException, SimulationException
    {
	List<Site> data = new ArrayList<>();

	for (String c: siteClasses)
	{
	    data.add(getSite(internal, recode, c));
	}

	return new Alignment(data);
    }

    private String getRandomChar(RateCategory r, Branch b, String start, String siteClass)
    {
        //Gets a random character at the other end of a branch given the rate category,
        //branch and start state
	double tot = 0.0;
	double v = random.nextDouble();
	String ret = null;

        for (String s: P.get(siteClass).getAllStatesAsList())
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

    private String getRandomStart(RateCategory r, String siteClass)
    {
        //Get a random root assignment
	double tot = 0.0;
	double v = random.nextDouble();
	String ret = null;

        for (String s: P.get(siteClass).getAllStatesAsList())
	{
	    if (tot <= v)
	    {
		ret  = s;
	    }
	    tot = tot + P.get(siteClass).getFreq(r, s);
	}
        
	return ret;
    }

    private Tree t;
    private Random random;
    private Alignment missing;
    private Map<String,Probabilities> P;
    private Map<String,Constrainer> con;
    
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
