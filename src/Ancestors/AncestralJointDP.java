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

import Alignments.Site;
import Alignments.Alignment;
import Alignments.AlignmentException;
import Likelihood.Probabilities;
import Maths.Real;
import Maths.RealFactory;
import Maths.RealFactory.RealType;
import Maths.SmallDouble;
import Maths.StandardDouble;
import Parameters.Parameters;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory;
import Models.RateCategory.RateException;
import Parameters.Parameter;
import Parameters.Parameters.ParameterException;
import Trees.Branch;
import Trees.Tree;
import Trees.TreeException;
import Utils.SetUtils;
import Utils.SetUtils.SetHasMultipleElementsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class to perform joint ancestral reconstruction using the method of Pupko 2000
 * @author Daniel Money
 * @version 2.0
 */
public class AncestralJointDP extends AncestralJoint
{
    AncestralJointDP(Model m, Alignment a, Tree t) throws MultipleRatesException
    {
	this.a = a;
        this.m = new HashMap<>();
	this.m.put(null,m); 
	this.t = new HashMap<>();
        this.t.put(null,t);
        if (!m.hasSingleRate())
        {
            throw new MultipleRatesException();
        }
        r = new HashMap<>();
        for (RateCategory rc: m)
        {
            r.put(null,rc);
        }
    }
    
    AncestralJointDP(Map<String,Model> m, Alignment a, Tree t) throws MultipleRatesException, AlignmentException
    {
        this.a = a;
        this.m = m;
        this.t = new HashMap<>();
        for (String s: m.keySet())
        {
            this.t.put(s,t);
        }    
        for (Entry<String,Model> e: m.entrySet())
        {
            if (!e.getValue().hasSingleRate())
            {
                throw new MultipleRatesException();
            }
            r = new HashMap<>();
            for (RateCategory rc: e.getValue())
            {
                r.put(e.getKey(),rc);
            }
        }
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
    }

    AncestralJointDP(Model m, Alignment a, Map<String,Tree> t) throws MultipleRatesException, AlignmentException
    {
        this.a = a;
        this.m = new HashMap<>();
        for (String s: t.keySet())
        {
            this.m.put(s,m);
        }
        this.t = new HashMap<>();
        for (Entry<String,Model> e: this.m.entrySet())
        {
            if (!e.getValue().hasSingleRate())
            {
                throw new MultipleRatesException();
            }
            r = new HashMap<>();
            for (RateCategory rc: e.getValue())
            {
                r.put(e.getKey(),rc);
            }
        }
        if (!a.check(t))
        {
            throw new AlignmentException("Alignment contains classes for which no tree has been defined");
        }
    }
    
    AncestralJointDP(Map<String,Model> m, Alignment a, Map<String,Tree> t) throws MultipleRatesException, AlignmentException
    {
        this.a = a;
        this.m = m;
        this.t = t;    
        for (Entry<String,Model> e: m.entrySet())
        {
            if (!e.getValue().hasSingleRate())
            {
                throw new MultipleRatesException();
            }
            r = new HashMap<>();
            for (RateCategory rc: e.getValue())
            {
                r.put(e.getKey(),rc);
            }
        }
        if (!a.check(m))
        {
            throw new AlignmentException("Alignment contains classes for which no model has been defined");
        }
        if (!a.check(t))
        {
            throw new AlignmentException("Alignment contains classes for which no tree has been defined");
        }
    }
    
    public Alignment calculate(Parameters params) throws RateException, ModelException, AncestralException, TreeException, ParameterException, AlignmentException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Tree tt: t.values())
        {
            for (Branch b: tt)
            {
                if (!params.hasParam(b.getChild()))
                {
                    params.addParameter(Parameter.newFixedParameter(b.getChild(),
                       b.getLength()));

                }
            }
        }
        //Calculate probabilities for this model, tree and set of parameters
        Map<String,Probabilities> P = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t.get(e.getKey()),params));
        }
 
        //Get unqiue sites in the alignment and calculator a reconstuction for each
        Map<Site,Site> ret = new HashMap<>();
        for (Site s: a.getUniqueSites())
	{
            ret.put(s,calculateSite(s,P.get(s.getSiteClass())));
	}

        //Make a new alignment using the result for each unique site
        List<Site> alignment = new ArrayList<>();
        for (Site s: a)
        {
            alignment.add(ret.get(s));
        }

	return new Alignment(alignment);
    }
	
    Site calculateSite(Site s, Probabilities P) throws AncestralException, TreeException, RateException
    {
        HashMap<String,Map<String,Real>> L = new HashMap<>();
	HashMap<String,Map<String,String>> C = new HashMap<>();
	
        //Ordering of branches returned ensures we start at leaves and visit
        //all children before the parent.
        //This pretty much follows the algorithm of Pupko 2000 exactly 
	for (Branch b: t.get(s.getSiteClass()))
	{
	    Map<String,String> c = new HashMap<>();
	    Map<String,Real> l = new HashMap<>();
	    if (t.get(s.getSiteClass()).isExternal(b))
	    {
                for (String state: P.getAllStates())
		{                    
                    Set<String> chs = s.getCharacter(b.getChild());
                    try
                    {
                        //At the moment the code can't deal with ambiguous characters,
                        //so get the single possible if there is only one possible state,
                        //else throw an Exception.
                        String ch = SetUtils.getSingleElement(chs);
                        c.put(state,ch);
                        //THIS WILL BE SLOW
                        l.put(state,RealFactory.getReal(type,P.getP(r.get(s.getSiteClass())).getP(b,ch,state)));
                    }
                    catch (SetHasMultipleElementsException e)
                    {
                        throw new AncestralException("Can't deal with ambiguous characters",e);
                    }
		}
	    }
	    else
	    {
                for (String i: P.getAllStates())    
		{
		    Real maxL = RealFactory.getSmallestReal(type);//-Double.MAX_VALUE;
		    String maxC = null;

                    for (String j : P.getAllStates())
		    {
                        //THIS WILL BE SLOW
                        Real cl = RealFactory.getReal(type, P.getP(r.get(s.getSiteClass())).getP(b, j, i));
                        for (Branch ch: t.get(s.getSiteClass()).getBranchesByParent(b.getChild()))
			{
			    cl = cl.multiply(L.get(ch.getChild()).get(j));
			}
                        if (cl.greaterThan(maxL))
			{
			    maxL = cl;
			    maxC = j;
			}
		    }
		    l.put(i, maxL);
		    c.put(i, maxC);
		}
	    }
	    C.put(b.getChild(),c);
	    L.put(b.getChild(),l);
	}

	Real maxL = RealFactory.getSmallestReal(type);
	String maxC = null;
        for (String j: P.getAllStates())
	{
            Real cl = RealFactory.getReal(type, P.getRoot(r.get(s.getSiteClass())).getFreq(j));
	    for (Branch ch: t.get(s.getSiteClass()).getBranchesByParent(t.get(s.getSiteClass()).getRoot()))
	    {
		cl = cl.multiply(L.get(ch.getChild()).get(j));
	    }
	    if (cl.greaterThan(maxL))
	    {
		maxL = cl;
		maxC = j;
	    }
	}

        HashMap<String,String> site = new HashMap<>();        
        site.put(t.get(s.getSiteClass()).getRoot(), maxC);
	
	for (Branch b: t.get(s.getSiteClass()).getBranchesReversed())
	{
            site.put(b.getChild(), C.get(b.getChild()).get(site.get(b.getParent())));
	}
        
        //Done like this so elements are in a sensible order for printing
        LinkedHashMap<String,String> ls = new LinkedHashMap<>();
        for (String l: t.get(s.getSiteClass()).getLeaves())
        {
            ls.put(l,site.get(l));
        }
        for (String i: t.get(s.getSiteClass()).getInternal())
        {
            ls.put(i,site.get(i));
        }

        return new Site(ls);
    }

    private Alignment a;
    private Map<String,Model> m;
    private Map<String,Tree> t;
    private Map<String,RateCategory> r;
    
    /**
     * Sets the real type to be used during calculations, either {@link SmallDouble}
     * or {@link StandardDouble}
     * @param type The double type to use
     * @see Likelihood.SiteLikelihood#realType(Maths.RealFactory.RealType) 
     */
    public static void realType(RealType type)
    {
        AncestralJointDP.type = type;
    }
    
    private static RealType type = RealType.STANDARD_DOUBLE;
    
    /**
     * Thrown if the model has multiple rate categories as this methodology
     * doesn't work on models with multiple rate categories.
     */
    static class MultipleRatesException extends Throwable
    {
        private MultipleRatesException()
        {
            super("Joint ancestral reconstruction using the dynamic programming"
                    + " method only works with a single rate category");
        }
    }
}
