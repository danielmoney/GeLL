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
import Maths.Real.RealType;
import Maths.RealFactory;
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
 * Class to perform joint ancestral reconstrion using the method of Pupko 2000
 * @author Daniel Money
 * @version 1.2
 */
public class AncestralJointDP extends AncestralJoint
{
    AncestralJointDP(Model m, Alignment a, Tree t) throws MultipleRatesException
    {
	this.a = a;
        this.m = new HashMap<>();
	this.m.put(null,m); 
	this.t = t;
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
    }
    
    public Alignment calculate(Parameters params) throws RateException, ModelException, AncestralException, TreeException, ParameterException, AlignmentException
    {
        //If the parameters setting doesn't include branch lengths parameters then
        //add them from the tree.  The paramter / branch length interaction is a
        //bit counter-inutative and probably needs changing but in the mean time
        //this is here to make errors less likely.
        for (Branch b: t)
	{
            if (!params.hasParam(b.getChild()))
            {
                params.addParameter(Parameter.newFixedParameter(b.getChild(),
                   b.getLength()));

            }
	}
        
        //Calculate probabilities for this model, tree and set of parameters
        Map<String,Probabilities> P = new HashMap<>();
        for (Entry<String,Model> e: m.entrySet())
        {
            P.put(e.getKey(),new Probabilities(e.getValue(),t,params));
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
	
    Site calculateSite(Site s, Probabilities P) throws AncestralException, TreeException
    {
	//HashMap<String,Map<String,Double>> L = new HashMap<>();
        HashMap<String,Map<String,Real>> L = new HashMap<>();
	HashMap<String,Map<String,String>> C = new HashMap<>();
	
        //Ordering of branches returned ensures we start at leaves and visit
        //all children before the parent.
        //This pretty much follows the algorithm of Pupko 2000 exactly 
	for (Branch b: t)
	{
	    Map<String,String> c = new HashMap<>();
	    Map<String,Real> l = new HashMap<>();
	    if (t.isExternal(b))
	    {
		for (String state: P.getAllStatesAsList())
		{                    
                    Set<String> chs = s.getCharacter(b.getChild());
                    try
                    {
                        //At the moment the code can't deal with ambiguous characters,
                        //so get the single possible if there is only one possible state,
                        //else throw an Exception.
                        String ch = SetUtils.getSingleElement(chs);
                        c.put(state,ch);
                        //l.put(state,P.getP(r.get(s.getSiteClass()), b, ch, state));
                        //THIS WILL BE SLOW
                        //l.put(state,P.getP(r.get(s.getSiteClass())).getP(b).getP(ch,state));
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
		for (String i: P.getAllStatesAsList())
		{
		    Real maxL = RealFactory.getSmallestReal(type);//-Double.MAX_VALUE;
		    String maxC = null;

		    for (String j : P.getAllStatesAsList())
		    {
			//double cl = matrices[b].getPosition(j, i);
			//double cl = P.getP(r, b, i, j);//matrices[b].getPosition(i, j);
                        //double cl = P.getP(r.get(s.getSiteClass()), b, j, i);
                        //THIS WILL BE SLOW
                        //double cl = P.getP(r.get(s.getSiteClass())).getP(b).getP(j, i);
                        Real cl = RealFactory.getReal(type, P.getP(r.get(s.getSiteClass())).getP(b, j, i));
			//for (String ch: MapUtils.reverseLookupMulti(t.getBranches(), b+1))
                        for (Branch ch: t.getBranchesByParent(b.getChild()))
			{
			    cl = cl.multiply(L.get(ch.getChild()).get(j));
			}
			//if (cl > maxL)
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
	for (String j: P.getAllStatesAsList())
	{
	    Real cl = RealFactory.getReal(type, P.getFreq(r.get(s.getSiteClass()), j));
	    for (Branch ch: t.getBranchesByParent(t.getRoot()))
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
        site.put(t.getRoot(), maxC);
	
	for (Branch b: t.getBranchesReversed())
	{
            site.put(b.getChild(), C.get(b.getChild()).get(site.get(b.getParent())));
	}
        
        //Done like this so elements are in a sensible order for printing
        LinkedHashMap<String,String> ls = new LinkedHashMap<>();
        for (String l: t.getLeaves())
        {
            ls.put(l,site.get(l));
        }
        for (String i: t.getInternal())
        {
            ls.put(i,site.get(i));
        }

        return new Site(ls);
    }

    private Alignment a;
    private Map<String,Model> m;
    private Tree t;
    private Map<String,RateCategory> r;
    
    //THIS IS FAR FROM IDEAL!!
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
