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

import Constraints.SiteConstraints;
import Alignments.Alignment;
import Alignments.Alignment.UniqueSite;
import Alignments.AlignmentException;
import Alignments.Site;
import Constraints.Constrainer;
import Constraints.NoConstraints;
import Likelihood.BasicCalculator.CalculatorException;
import Likelihood.SiteLikelihood.LikelihoodException;
import Likelihood.SiteLikelihood.NodeLikelihood;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import Utils.ArrayMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Calculates the likelihood for different parameter values.  Succesive calls
 * to an instance of this class are used to maximise the parameters values for
 * one case.  Uses the pruning technique of Felenstein 1981 and can account for
 * unobserved states using Felsenstein 1992.
 * @author Daniel Money
 * @version 1.3
 */
public class Calculator extends BasicCalculator<Likelihood> // implements CalculatesLikelihood<Likelihood>
{  
    /**
     * Creates an object to calculate the likelihood for a given model, alignment,
     * tree.  Has no unobserved data or constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */
    public Calculator(Model m, Alignment a, Tree t) throws TreeException, LikelihoodException, AlignmentException
    {
        this(m,a,t,null,new NoConstraints(m.getStates()));
    }

    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree, missing data and constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */    
    public Calculator(Model m, Alignment a, Tree t, Alignment unobserved) throws TreeException, LikelihoodException, AlignmentException
    {
        this(m,a,t,unobserved,new NoConstraints(m.getStates()));
    }
 
    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree and constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param con Any constraints
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */
    public Calculator(Model m, Alignment a, Tree t, Constrainer con) throws TreeException, LikelihoodException, AlignmentException
    {
        this(m,a,t,null,con);
    }
    
   
    /**
     * Creates a class to calculate the likelihood for a given model, alignment,
     * tree, unobserved data and constraints
     * @param m The model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @param con Any constraints
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model).
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */
    public Calculator(Model m, Alignment a, Tree t, Alignment unobserved, Constrainer con) throws TreeException, LikelihoodException, AlignmentException
    {
        this(makeModelMap(m),a,t,unobserved,makeConstrainerMap(con));
        /*this.m = new HashMap<>();
        this.m.put(null,m);
        this.a = a;
        this.t = t;
        this.missing = unobserved;
        this.con = new HashMap<>();
        this.con.put(null,con);
        
        //The initial node likelihoods will stay the same for every call to
        //calculator.  As initalising them is slow compared to copying a pre-exsisting
        //one, pre-create initalised node likelihoods and copy them each time
        //we do a calculation.  Results in a significant speed increase when
        //optimising.
        this.snl = getInitialNodeLikelihoods(this.m,a,t,missing,this.con); */
    }

    /**
     * Creates a class to calculate the likelihood for a given set of models, an alignment,
     * and a tree.  There should be one model per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @throws AlignmentException Thrown if a model isn't given for each site class
     * in the alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model). 
     */
    public Calculator(Map<String,Model> m, Alignment a, Tree t) throws AlignmentException, TreeException, LikelihoodException
    {
        this(m,a,t,null,makeNoConstraintsMap(m));
        /*this.m = m;
        this.a = a;
        this.t = t;
        missing = null;
        con = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            con.put(e.getKey(),new NoConstraints(e.getValue().getStates()));
        }
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
        
        this.snl = getInitialNodeLikelihoods(m,a,t,missing,con); */
    }

    /**
     * Creates a class to calculate the likelihood for a given set of models, an alignment,
     * a tree, and unobserved data.  There should be one model per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @throws AlignmentException Thrown if a model isn't given for each site class
     * in the alignment 
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model). 
     */
    public Calculator(Map<String,Model> m, Alignment a, Tree t, Alignment unobserved) throws AlignmentException, TreeException, LikelihoodException
    {
        this(m,a,t,null,makeNoConstraintsMap(m));
        /*this.m = m;
        this.a = a;
        this.t = t;
        missing = unobserved;
        con = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            con.put(e.getKey(),new NoConstraints(e.getValue().getStates()));
        }
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
        this.snl = getInitialNodeLikelihoods(m,a,t,missing,con); */
    }

    /**
     * Creates a class to calculate the likelihood for a given set of models, an alignment,
     * a tree and a set of constraints.  There should be one model and one
     * constrainer per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @param con Map from site class to constrainer
     * @throws AlignmentException Thrown if a model and constrainer isn't given
     * for each site class in the alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model). 
     */
    public Calculator(Map<String,Model> m, Alignment a, Tree t, Map<String,Constrainer> con) throws AlignmentException, TreeException, LikelihoodException
    {
        this(m,a,t,null,con);
    }

    /**
     * Creates a class to calculate the likelihood for a given set of models, an alignment,
     * a tree, unobserved data and a set of constraints.  There should be one model and one
     * constrainer per site class in the alignment
     * @param m Map from site class to model
     * @param a The alignment
     * @param t The tree
     * @param unobserved Unobserved data given as another alignment
     * @param con Map from site class to constrainer
     * @throws AlignmentException Thrown if a model and constrainer isn't given
     * for each site class in the alignment
     * @throws TreeException If there is a problem with the tree
     * @throws Likelihood.Likelihood.LikelihoodException Thrown if a node is initalised to every state having zero probability
     *      (most probably due to the state at the node not being in the model). 
     */
    public Calculator(Map<String,Model> m, Alignment a, Tree t, Alignment unobserved, Map<String,Constrainer> con) throws AlignmentException, TreeException, LikelihoodException
    {
        super(m,t,getInitialNodeLikelihoods(m,a,t,unobserved,con));
        this.a = a;
        this.t = t;
        this.missing = unobserved;
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
        if (!a.check(con))
        {
            throw new AlignmentException("Alignment contains classes for which no constrainer has been defined");
        }
        this.snl = getInitialNodeLikelihoods(m,a,t,missing,con); 
    }
    
    /**
     * Calculates the likelihood for a given set of parameters
     * @param p The parameters to be used in the calculation
     * @return A Likelihood object which contains the likelihood as well as
     * likelihoods for each site.
     * @throws TreeException Thrown if there is a problem with the Tree (e.g. if
     * there is a branch with no length given in parameters)
     * @throws Models.RateCategory.RateException Thrown if there is an issue with
     * a rate category in the model (e.g. a badly formatted rate).
     * @throws Models.Model.ModelException Thrown if there is a problem with the
     * model (e.g. the rate categories differ in their states)
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters (e.g. a requied parameter is not present)
     * @throws Likelihood.Calculator.CalculatorException If an unexpected (i.e. positive or NaN) log likelihood is calculated 
     */
    public Likelihood calculate(Parameters p) throws TreeException, RateException, ModelException, ParameterException, CalculatorException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.  If the branch has a length add
        //it as a fixed parameter, else as an estimated parameter.
        for (Branch b: t)
	{
            if (!p.hasParam(b.getChild()))
            {
                if (b.hasLength())
                {
                    p.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));
                }
                else
                {
                    p.addParameter(Parameter.newEstimatedPositiveParameter(b.getChild(),false));
                }
            }
	}
        
        //The total ikelihood
        double l = 0.0;
        //Stores the likelihood of sites in the alignment
        ArrayMap<Site,SiteLikelihood> siteLikelihoods = new ArrayMap<>(Site.class,SiteLikelihood.class,a.getUniqueSites().size());
        //Stores the likelihood of unobserved states
        ArrayMap<Site,SiteLikelihood> missingLikelihoods;
        if (missing != null)
        {
            missingLikelihoods = new ArrayMap<>(Site.class,SiteLikelihood.class,missing.getUniqueSites().size());
        }
        else
        {
            missingLikelihoods = null;
        }
        
        
        
        //Doing threaded calculation can be slower in small cases due to the
        //overhead in creating threads.  However haven't tested when this is the
        //case and is likely to depend on both tree and rate matrix size so for
        //now always doing it threaded.
        //try
        //{
            /*//Calculate all the probabilites associated with this model, tree and
            //set of parameters
            ArrayMap<String,Probabilities> tp = new ArrayMap<>(String.class,Probabilities.class,m.size());
            for (Entry<String,Model> e: m.entrySet())
            {
                tp.put(e.getKey(), new Probabilities(e.getValue(),t,p));
            }
            
            //For each unique site in both the alignment and unobserved sites
            //create a callable object to calculate it and send it to
            // be executed.
            ArrayMap<UniqueSite, SiteCalculator> sites = new ArrayMap<>(UniqueSite.class,SiteCalculator.class,a.getUniqueSites().size());
            ArrayMap<UniqueSite, SiteCalculator> miss;
            if (missing != null)
            {
                miss = new ArrayMap<>(UniqueSite.class,SiteCalculator.class,missing.getUniqueSites().size());
            }
            else
            {
                miss = new ArrayMap<>(UniqueSite.class,SiteCalculator.class,0);
            }
            
            List<SiteCalculator> scs = new ArrayList<>();
            for (UniqueSite s: a.getUniqueSites())
            {
                SiteCalculator temp = new SiteCalculator(t, 
                        tp.get(s.getSiteClass()),
                        snl.get(s));
                scs.add(temp);
                sites.put(s, temp);
            }
            if (missing != null)
            {
                for (UniqueSite s: missing.getUniqueSites())
                {
                    SiteCalculator temp = new SiteCalculator(t, 
                            tp.get(s.getSiteClass()),
                            snl.get(s));
                    scs.add(temp);
                    miss.put(s,temp);
                }                
            }
            
            es.invokeAll(scs);*/
            
            ArrayMap<Site,SiteLikelihood> sites = siteCalculate(p);
                        
            //Get the result for each site and calculate the total likelihood (l)
            //of the alignemnt taking into account how often each unique site occurs
            //for (Entry<Site, SiteCalculator> e: sites.entrySet())
            //for (int i = 0; i < sites.size(); i++)
            //{
            for (UniqueSite us: a.getUniqueSites())
            {
                //Entry<UniqueSite,SiteCalculator> e = sites.getEntry(i);
                //try
                //{
                    SiteLikelihood sl = sites.get(us);//e.getValue().getResult();
                    siteLikelihoods.put(us,sl);//(e.getKey(),sl);
                    //l += a.getCount(e.getKey()) * Math.log(sl.getLikelihood());
                    l += us.getCount() * Math.log(sl.getLikelihood());//e.getKey().getCount() * Math.log(sl.getLikelihood());
                //}
                //catch(ResultNotComputed ex)
                //{
                    //Shouldn't get here as the call to shutdown should ensure
                    //everythings finished before we do.
                //    throw new UnexpectedError(ex);
                //}
            }
            
            //Get the result for each site and calculate the total likelihood (m)
            //of the unobserved data.  Follows Felsenstein 1992.
            //double ml = 0.0;
            HashMap<String, Double> ml = new HashMap<>();
            /*for (int i = 0; i < miss.size(); i++)
            {*/
            if (missing != null)
            {
                for (UniqueSite us: missing.getUniqueSites())
                {
                    //Entry<UniqueSite,SiteCalculator> e = miss.getEntry(i);
                    //try
                    //{
                        SiteLikelihood sl = sites.get(us);//e.getValue().getResult();
                        missingLikelihoods.put(us,sl);//(e.getKey(),sl);
                        String sc = us.getSiteClass();//e.getKey().getSiteClass();
                        if (ml.containsKey(sc))
                        {
                            ml.put(sc, ml.get(sc) + sl.getLikelihood());
                        }
                        else
                        {
                            ml.put(sc, sl.getLikelihood());
                        }
                    //}
                    //catch(ResultNotComputed ex)
                    //{
                        //Shouldn't get here as the call to shutdown should ensure
                        //everythings finished before we do.
                    //    throw new UnexpectedError(ex);
                    //}
                }
                //Now modify the alignment likelihood to account for unobserved data,
                //again per Felsenstein 1992
                for (String sc: ml.keySet())
                {
                    l = l - (a.getClassSize(sc) * Math.log(1 - ml.get(sc)));
                }
            }
            if (l > 0)
            {
                throw new CalculatorException("Positive Log Likelihood");
            }
            if (Double.isNaN(l))
            {
                throw new CalculatorException("NaN Log Likelihood");
            }
        //}
        //catch(InterruptedException ex)
        //{
            //Don't think this should happen but in case it does...
        //    throw new UnexpectedError(ex);
        //}
        return new Likelihood(l,siteLikelihoods,missingLikelihoods,p);
    }
    
    private Alignment a;
    private Tree t;
    private Alignment missing;
    
    private HashMap<Site,ArrayMap<String,NodeLikelihood>> snl;
    
    private static HashMap<Site,ArrayMap<String,NodeLikelihood>> getInitialNodeLikelihoods(Map<String,Model> m, Alignment a, Tree t, Alignment missing, Map<String,Constrainer> con) 
            throws TreeException, LikelihoodException, AlignmentException
    {
        HashMap<Site,ArrayMap<String,NodeLikelihood>> snl = new HashMap<>();
        for (UniqueSite s: a.getUniqueSites())
        {
            SiteConstraints scon = con.get(s.getSiteClass()).getConstraints(t, s);
            //See the constructor Calculator(Model m, Alignment a, Tree t, Alignment unobserved, Constrainer con)
            //for why this code is here
            ArrayMap<String, NodeLikelihood> nodeLikelihoods = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
            for (String l: t.getLeaves())
            {
                //nodeLikelihoods.put(l, new NodeLikelihood(tp.getAllStates(), s.getCharacter(l)));
                nodeLikelihoods.put(l, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap(), s.getCharacter(l)));
            }

            //And now internal nodes using any constraints
            for (String i: t.getInternal())
            {
                //nodeLikelihoods.put(i, new NodeLikelihood(tp.getAllStates(), con.getConstraint(i)));
                nodeLikelihoods.put(i, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap(), scon.getConstraint(i)));
            }
            snl.put(s, nodeLikelihoods);
        }
        if (missing != null)
        {
            for (UniqueSite s: missing.getUniqueSites())
            {
                SiteConstraints scon = con.get(s.getSiteClass()).getConstraints(t, s);
            //See the constructor Calculator(Model m, Alignment a, Tree t, Alignment unobserved, Constrainer con)
            //for why this code is here
                ArrayMap<String, NodeLikelihood> nodeLikelihoods = new ArrayMap<>(String.class,NodeLikelihood.class,t.getNumberBranches() + 1);
                for (String l: t.getLeaves())
                {
                    //nodeLikelihoods.put(l, new NodeLikelihood(tp.getAllStates(), s.getCharacter(l)));
                    nodeLikelihoods.put(l, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap(), s.getCharacter(l)));
                }

                //And now internal nodes using any constraints
                for (String i: t.getInternal())
                {
                    //nodeLikelihoods.put(i, new NodeLikelihood(tp.getAllStates(), con.getConstraint(i)));
                    nodeLikelihoods.put(i, new NodeLikelihood(m.get(s.getSiteClass()).getArrayMap(), scon.getConstraint(i)));
                }
                snl.put(s, nodeLikelihoods);
            }
        }
        return snl;
    }
    
    private static Map<String,Model> makeModelMap(Model m)
    {
        HashMap<String, Model> mm = new HashMap<>();
        mm.put(null,m);
        return mm;
    }
    
    private static Map<String,Constrainer> makeConstrainerMap(Constrainer c)
    {
        HashMap<String, Constrainer> cm = new HashMap<>();
        cm.put(null,c);
        return cm;
    }
    
    private static Map<String,Constrainer> makeNoConstraintsMap(Map<String,Model> m)
    {
        HashMap<String, Constrainer> cm = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            cm.put(e.getKey(),new NoConstraints(e.getValue().getStates()));
        }
        return cm;
    }
}
