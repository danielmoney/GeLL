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

package Trees;

import Exceptions.InputException;
import Exceptions.OutputException;
import Exceptions.UnexpectedError;
import Parameters.Parameter;
import Parameters.Parameters;
import Parameters.Parameters.ParameterException;
import Utils.SetUtils;
import Utils.SetUtils.SetHasMultipleElementsException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a phylogenetic tree.  Trees are defined as a list of {@link Branch}.
 * Nodes are defined by a String.  This entire class deals with rooted and unrooted
 * trees the same.  Traditionally a rooted tree is detected by a node of degree two
 * but if we allow multifurcations at the root then a rooted tree need not have any
 * degree two nodes.  Users should be aware of the type of tree they're using and
 * any consequences.
 * @author Daniel Money
 * @version 2.0
 */
public class Tree implements Iterable<Branch>
{
    /**
     * Creates a tree from a list of branches
     * @param branches A list of branches
     * @throws Trees.TreeException Thrown if the list of branches passed do not represent
     * a tree
     */
    public Tree(List<Branch> branches) throws TreeException
    {
	internal = new ArrayList<>();
        leaves = new ArrayList<>();
        Set<String> posroot = new HashSet<>();
        for (Branch b: branches)
        {
            posroot.add(b.getParent());
            leaves.add(b.getChild());
        }
        for (Branch b: branches)
        {
            posroot.remove(b.getChild());
            leaves.remove(b.getParent());
        }
        
        try
        {
            root = SetUtils.getSingleElement(posroot);
        }
        catch (SetHasMultipleElementsException e)
        {
            throw new TreeException("Tree appears to be disjoint or a network");
        }
        
        this.branches = new ArrayList<>();
        LinkedList<String> todo = new LinkedList<>();
        todo.add(root);
        while (todo.size() > 0)
        {
            String cur = todo.pollFirst();
            for (Branch b: branches)
            {
                if (b.getParent().equals(cur))
                {
                    this.branches.add(b);
                    if (!internal.contains(b.getParent()))
                    {
                        internal.add(b.getParent());
                    }
                    if (todo.contains(b.getChild()))
                    {
                        throw new TreeException("Tree contains internal nodes with the same name");
                    }
                    todo.add(b.getChild());
                }
            }
        }
        Collections.reverse(this.branches);
        Collections.reverse(this.internal);
    }

    /**
     * Duplicates a tree topology while replacing branch lengths using
     * the appropiate parameter
     * @param old The old tree
     * @param p The parameters used for the new branch lengths
     * @throws Parameters.Parameters.ParameterException Thrown if there is a problem
     * with the parameters, e.g. a required parameter not existing.
     */
    public Tree(Tree old, Parameters p) throws ParameterException
    {
        branches = new ArrayList<>();
        for (Branch b: old)
        {
            branches.add(new Branch(b.getParent(), b.getChild(), p.getValue(b.getChild())));
        }
        internal = old.internal;
        leaves = old.leaves;
        root = old.root;
    }
    
    /**
     * Gets a list of branches.  Returned in a order that ensures external 
     * branches are visited first and then work towars the root.
     * @return A list of branches
     */
    public List<Branch> getBranches()
    {
	return branches;
    }
    
    public Iterator<Branch> iterator()
    {
        return branches.iterator();
    }
    
    /**
     * Gets a list of internal nodes.  Nodes are returned in such a way that
     * all child nodes are returned before their parent is.
     * @return A list of internal nodes
     */
    
    public List<String> getInternal()
    {
        return internal;
    }
    
    /**
     * Gets a list of leaves
     * @return A list of leaves
     */
    public List<String> getLeaves()
    {
        return leaves;
    }
    
    /**
     * Get the root node
     * @return The root node
     */
    public String getRoot()
    {
        return root;
    }
    
    /**
     * Gets the length of the tree
     * @return The length of the tree
     * @throws Trees.TreeException Thrown if the tree does not have branch lengths
     * associated with it
     */
    public double getLength() throws TreeException
    {
        double l = 0.0;
        for (Branch b: branches)
        {
            l += b.getLength();
        }
        return l;
    }

    /**
     * Returns a Parameters object containing a parameter for each branch length.
     * Parameters are fixed at the relevance branch length
     * @return Parameters object containiing branch lengths
     * @throws Trees.TreeException Thrown if the tree does not have branch lengths
     */
    public Parameters getParameters() throws TreeException
    {
	Parameters p = new Parameters();
	for (Branch b: branches)
	{
            p.addParameter(Parameter.newFixedParameter(b.getChild(),
		   b.getLength()));
	}
	return p;
    }

    /**
     * Returns a Parameters object containing a parameter for each branch length.
     * Parameters are estimation parameters.
     * @return Parameters object containiing branch lengths
     */
    public Parameters getParametersForEstimation()
    {
	Parameters p = new Parameters();
	for (Branch b: branches)
	{
	    p.addParameter(Parameter.newEstimatedPositiveParameter(b.getChild()));
	}
	return p;
    }

    /**
     * Gets the size of the tree, that is the number of taxa
     * @return The size of the tree
     */
    public int getSize()
    {
	return leaves.size();
    }

    /**
     * Gets the number of branches
     * @return The number of branches
     */
    public int getNumberBranches()
    {
	return branches.size();
    }
    
    /**
     * Gets the branch which has the given node as the child node
     * @param child The child node
     * @return The branch with the given child
     * @throws Trees.TreeException Thrown if the node does not exist or the root node
     * is passed
     */
    public Branch getBranchByChild(String child) throws TreeException
    {
        if (child.equals(root))
        {
            throw new TreeException("The root node is not a child on any branch");
        }
        for (Branch b: branches)
        {
            if (b.getChild().equals(child))
            {
                return b;
            }
        }
        throw new TreeException("Node does not exist");
    }
    
    /**
     * Gets the set of branches which have the passed node as a parent
     * @param parent The parent node
     * @return The set of banches with the given parent
     * @throws Trees.TreeException If the node does not exist in the tree or is a leaf
     */
    public Set<Branch> getBranchesByParent(String parent) throws TreeException
    {
        Set<Branch> ret = new HashSet<>();
        for (Branch b: branches)
        {
            if (b.getParent().equals(parent))
            {
                ret.add(b);
            }
        }
        if (ret.isEmpty())
        {
            throw new TreeException("Node does not exist or is a lead");
        }
        return ret;
    }
    
    /**
     * Gets the parent node of the given node
     * @param child The child node
     * @return The parent of the child
     * @throws Trees.TreeException If the node does not exist or is the root
     */
    public String getParent(String child) throws TreeException
    {
        return getBranchByChild(child).getParent();
    }

    /**
     * Returns a new tree where the lengths are scales so the total length is
     * different
     * @param length The new total length
     * @return The scaled tree
     * @throws Trees.TreeException Thrown if the tree does not have branch lengths
     */
    public Tree scaledTo(double length) throws TreeException
    {
	List<Branch> newBranches = new ArrayList<>();
	double s = length / getLength();
	for (Branch b: branches)
	{
	    newBranches.add(new Branch(b.getParent(), b.getChild(), b.getLength() * s));
	}
	return new Tree(newBranches);
    }
    
    /**
     * Mid point roots the tree and returns a new tree
     * @param newRootName The name of the new root node
     * @return A new tree which is midpoint rooted
     * @throws Trees.TreeException If there is a problem with the tree, e.g. no branch
     * lengths
     */
    public Tree midPointRoot(String newRootName) throws TreeException
    {
        //Find the maximum distance between any pair of leaves
	double maxDist = 0.0;
	String taxa1 = null;
	String taxa2 = null;
        for (String leave1: leaves)
	{
            for (String leave2 : leaves)
	    {
		double d = taxaDistance(leave1,leave2);
		if (d > maxDist)
		{
		    maxDist = d;
		    taxa1 = leave1;
		    taxa2 = leave2;
		}
	    }
	}
        
        //Find the most common recent ancestor of the two taxa as the path
        //between the the two taxa will pass through it.
        List<String> taxa = new ArrayList<>();
        taxa.add(taxa1);
        taxa.add(taxa2);
        String p = MRCA(taxa);

        //Find the branch to split (br) and how far along it to split
	String br = null;
	double brd = 0.0;
        //Keeps track of how far along the path we've gone so far
	double total = 0.0;
        
        //Start at one taxa and traverse to the root, storing the midway point and
        //branch if we find it
	String child = taxa1;
	while (!child.equals(p))
	{
            Branch bi = getBranchByChild(child);
            //If we've found the midway point...
            if (((maxDist / 2) >= total) && ((maxDist/2) < (total + bi.getLength())))
	    {
		br = child;
		brd = (maxDist/2) - total;
	    }
	    total += bi.getLength();
	    child = bi.getParent();
	}

        //Now do the same starting from the other taxa - only one of these should
        //find the middle!
	total = 0.0;
	child = taxa2;
	while (!child.equals(p))
	{
            Branch bi = getBranchByChild(child);
            if (((maxDist / 2) >= total) && ((maxDist/2) < (total + bi.getLength())))
	    {
		br = child;
		brd = (maxDist/2) - total;
	    }
	    total += bi.getLength();
	    child = bi.getParent();
	}
        
        //If we haven't found the branch the midpoint is on it must be exactly
        //at the current root
        if (br == null)
        {
            br = root;
            brd = 0.0;
        }

        //Create the new branches as a copy of the old ones
        List<Branch> nb = new ArrayList<>(branches);

        //If the midpoint is at a node we don't need to add a node
        if (brd != 0.0)
        {
            //Remove the branch we're splitting
            nb.remove(getBranchByChild(br));

            //And add the two new ones that will be formed
            nb.add(new Branch(newRootName,br,brd));

            nb.add(new Branch(newRootName,getBranchByChild(br).getParent(),brd));
        }

        
        //Work out which branches need swapping direction, that is the parent
        //becomes the child and vice verse, due to the repositioned root.
        //This is the branches between the odl and new roots.
        LinkedList<String> dealWith = new LinkedList<>();

        //If the midpoint is at the root we don't need to do anything, setting cur
        //to the root ensures this.
        String cur;
        if (br.equals(root))
        {
            cur = root;
        }
        else
        {
            cur = getBranchByChild(br).getParent();
        }
	dealWith.addFirst(cur);
        while (!cur.equals(root))
	{
            String par = getBranchByChild(cur).getParent();
            dealWith.addFirst(par);
	    cur = par;
	}

        //Now remove all such branches and add the reversed branch
        String start = dealWith.pollFirst();
        for (String j: dealWith)
	{
            nb.remove(getBranchByChild(j));
            nb.add(new Branch(j,start,getBranchByChild(j).getLength()));
	    start = j;
	}
        return new Tree(nb);
    }

    private double taxaDistance(String taxa1, String taxa2) throws TreeException
    {
        //Calculates the minimum distance between two taxa
	LinkedList<String> p1 = new LinkedList<>();
	LinkedList<String> p2 = new LinkedList<>();

        //Calculate the path from each taxa to the root
	String i = taxa1;
	while (!i.equals(root))
	{
            String p = getBranchByChild(i).getParent();
            p1.add(p);
            i = p;
	}
	i = taxa2;
	while (!i.equals(root))
	{
            String p = getBranchByChild(i).getParent();
            p2.add(p);
            i = p;
	}
        //Find the earliest common "ancestor" of the two taxa by starting at
        //one and working towards the root until we find a node which is in the
        //path from the other taxa to the root (which will be the root if nothing
        //was found earlier.
	String p = "";
        for (String j: p1)
	{
	    if ((p.equals("")) && (p2.contains(j)))
	    {
		p = j;
	    }
	}
        
        //Calculate the distance from each taxa to this common "ancestor" the sum
        //of which is the minimum distance
	double total = 0.0;
	i = taxa1;
	while (!i.equals(p))
	{
            Branch b = getBranchByChild(i);
            total += b.getLength();
            i = b.getParent();
	}
	i = taxa2;
	while (!i.equals(p))
	{
            Branch b = getBranchByChild(i);
            total += b.getLength();
            i = b.getParent();
	}
	return total;
    }

    /**
     * Returns the most recent common ancestor of a set of leaves.
     * Although this only mkaes sense for rooted trees it will produce a result
     * for all trees per the explanation in the class description.
     * @param leaves Set of leaves to calculate the MRCA for
     * @return The MRCA
     * @throws Trees.TreeException Thrown if a leave does not exist
     */
    public String MRCA(List<String> leaves) throws TreeException
    {
        //If only one taxa passed it's the MRCA of itself!
        if (leaves.size() == 1)
        {
            return leaves.get(0);
        }
        
        //The current "path" of possible MRCA
        LinkedList<String> path = new LinkedList<>();
        
        //Remove the first leaf
        String node = leaves.remove(0);
        
        //Calculate the path from it to the root, which is the initial path of
        //possible MCRAs
        while (!node.equals(root))
        {
            node = getBranchByChild(node).getParent();
            path.add(node);
        }
        
        //mrca keeps track of the current MRCA
        String mrca = node;
        //For evry other leaf...
        for (String node2: leaves)
        {
            //Find the point at which it's path to the root intersects the path
            //of possible MCRAs
            while (!node2.equals(root))
            {
                node2 = getBranchByChild(node2).getParent();
                if (path.contains(node2))
                {
                    mrca = node2;
                    break;
                }
            }
            
            //Remove any nodes below this from the possible path of MCRAs
            LinkedList<String> newPath = new LinkedList<>(path);            
            for (String r: path)
            {
                if (!r.equals(mrca))
                {
                    newPath.remove(r);
                }
                else
                {
                    break;
                }
            }            
            path = newPath;
        }
    
        //return the first element of the possible MCRAs as this is the MCRA
        return path.peek();
    }
    
    /**
     * Tests whether the given branch is an external branch (i.e. the child is a
     * leaf)
     * @param b The branch to test
     * @return Whether it is an external branch
     */
    public boolean isExternal(Branch b)
    {
        return leaves.contains(b.getChild());
    }
    
    /**
     * Gets the branches in the reverse order to that returned by {@link #getBranches()}.
     * @return The branches in reverse order
     */
    public List<Branch> getBranchesReversed()
    {
        ArrayList<Branch> revBranches = new ArrayList<>(branches);
        Collections.reverse(revBranches);
        return revBranches;
    }
    
    /**
     * Gets the splits that represent the tree
     * @return A set of splites
     * @throws TreeException If the tree is invalid
     */
    public Set<Split> getSplits() throws TreeException
    {
        Map<String, TreeSet<String>> working = new HashMap<>();
        for (String l: getLeaves())
        {
            working.put(l,new TreeSet<String>());
        }
        for (String i: getInternal())
        {
            if (!i.equals(root))
            {
                working.put(i,new TreeSet<String>());
            }
        }
        for (String s: getLeaves())
        {
            String c = s;
            while (!c.equals(root))
            {
                working.get(c).add(s);
                c = getParent(c);
            }
        }
        
        Set<Split> ret = new HashSet<>();
        for (Entry<String,TreeSet<String>> e: working.entrySet())
        {
            ret.add(new Split(e.getValue(),getInverse(e.getValue()),getBranchByChild(e.getKey()).getLength()));
        }
        return ret;
    }
    
    private TreeSet<String> getInverse(Set<String> members)
    {
        TreeSet<String> inverse = new TreeSet<>();
        for (String l: getLeaves())
        {
            if (!members.contains(l))
            {
                inverse.add(l);
            }
        }
        return inverse;
    }
    
    /**
     * Calculates the RF distance between this tree and another tree
     * @param t The other tree
     * @return The RF distance
     * @throws TreeException If one tree or the other is not valud
     */
    public int RF(Tree t) throws TreeException
    {
        int rf = 0;
        for (Split s: getSplits())
        {
            Split es = t.getEquivilantSplit(s);
            rf = (es == null) ? rf + 1 : rf;
        }
        for (Split s: t.getSplits())
        {
            Split es = getEquivilantSplit(s);
            rf = (es == null) ? rf + 1 : rf;
        }
        return rf;
    }
    
    /**
     * Calculates the weighted (by branch length) RF distance between this tree and another tree
     * @param t The other tree
     * @return The weighted RF distance
     * @throws TreeException If one tree or the other is not valud
     */
    public double weightedRF(Tree t) throws TreeException
    {
        double rf = 0.0;
        for (Split s: getSplits())
        {
            Split es = t.getEquivilantSplit(s);
            rf = (es == null) ? rf + s.getLength() : rf + Math.abs(s.getLength() - es.getLength());
        }
        for (Split s: t.getSplits())
        {
            Split es = getEquivilantSplit(s);
            rf = (es == null) ? rf + s.getLength() : rf + Math.abs(s.getLength() - es.getLength());
        }
        return rf;
    }
    
    /**
     * Calculates the branch score distance between this tree and another tree
     * @param t The other tree
     * @return The branch score distance
     * @throws TreeException If one tree or the other is not valud
     */
    public double branchScore(Tree t) throws TreeException
    {
        double bs = 0.0;
        for (Split s: getSplits())
        {
            Split es = t.getEquivilantSplit(s);
            bs = (es == null) ? bs + Math.pow(s.getLength(),2) : bs + Math.pow(Math.abs(s.getLength() - es.getLength()),2);
        }
        for (Split s: t.getSplits())
        {
            Split es = getEquivilantSplit(s);
            bs = (es == null) ? bs + Math.pow(s.getLength(),2) : bs + Math.pow(Math.abs(s.getLength() - es.getLength()),2);
        }
        return Math.sqrt(bs);
    }    
    
    private Split getEquivilantSplit(Split s) throws TreeException
    {
        for (Split os: getSplits())
        {
            if (s.equalExceptLength(os))
            {
                return os;
            }
        }
        return null;
    }

    public String toString()
    {
	return toString(false);
    }

    /**
     * Returns a textual representation of the tree in Newick format
     * @param nameInternal Whether internal branches should be named
     * @return A Newick string representing the tree
     */
    public String toString(boolean nameInternal)
    {
        return toString(nameInternal,Integer.MAX_VALUE);
    }
            
    private String toString(boolean nameInternal, int limit)     
    {
        String text = "$" + root + "$;";
        
        List<String> revin = new ArrayList<>(internal);
        Collections.reverse(revin);
        
        try
        {
            for (String s: revin)
            {
                Set<Branch> bs = getBranchesByParent(s);
                StringBuilder inner = new StringBuilder();
                inner.append("(");
                for (Branch b: bs)
                {
                    if (isExternal(b))
                    {
                        inner.append(b.getChild().substring(0, Math.min(limit,b.getChild().length())));
                    }
                    else
                    {
                        inner.append("$");
                        inner.append(b.getChild());
                        inner.append("$");
                    }
                    if (b.hasLength())
                    {
                        inner.append(":");
                        inner.append(b.getLength());
                    }
                    inner.append(",");
                }
                inner.replace(inner.length()-1, inner.length(), ")");
                if (nameInternal)
                {
                    inner.append(s);
                }
                text = text.replace("$" + s + "$", inner.toString());
            }
        }
        catch (TreeException ex)
        {
            //As we know what we're dealing with we shouldn't get here but
            //just in case...
            throw new UnexpectedError(ex);
        }
        
        return text;
    }

    /**
     * Writes the tree to a file in Newick format.  This will not work correctly
     * if any internal node names have a $ sign as both first and large character.
     * Does not name internal nodes.
     * @param f The file to write the tree to
     * @throws OutputException Thrown if there is a problem writing the file
     */
    public void toFile(File f) throws OutputException
    {
	toFile(f,false);
    }

    /**
     * Write the tree to a file in Newick format.  This will not work correctly
     * if any internal node names have a $ sign as both first and large character.
     * @param f The file to write the tree to
     * @param nameInternal Whether internal branches should be named
     * @throws OutputException Thrown if there is a problem writing the file
     */
    public void toFile(File f, boolean nameInternal) throws OutputException
    {
	PrintStream out;
	try
	{
	    out = new PrintStream(new FileOutputStream(f));
	}
	catch (FileNotFoundException e)
	{
	    throw new OutputException("File can not be created", f.getAbsolutePath(),e);
	}
	out.println(toString(nameInternal));
	out.close();
    }
    
    /**
     * Wrtites the tree to a file with taxa names limited to 25 characters
     * for use in PAML.  At the moment no checking is done for any duplicated
     * taxa names that may be created.
     * @param f The file to write the tree to
     * @throws OutputException Thrown if there is a problem writing the file
     */
    public void toFilePAML(File f) throws OutputException
    {
	PrintStream out;
	try
	{
	    out = new PrintStream(new FileOutputStream(f));
	}
	catch (FileNotFoundException e)
	{
	    throw new OutputException("File can not be created", f.getAbsolutePath(),e);
	}
	out.print(toString(false,25));
	out.close();        
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Tree))
        {
            return false;
        }
        
        Tree t = (Tree) o;
        if (branches.size() != t.branches.size())
        {
            return false;
        }
        
        for (Branch b: branches)
        {
            if (!t.branches.contains(b))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        int hashCode = 0;
        for (Branch b: branches)
        {
            hashCode = hashCode * 31 + b.hashCode();
        }
        return hashCode;
    }
    
    private List<Branch> branches;
    private String root;
    private List<String> leaves;
    private List<String> internal;

    /**
     * Creates a tree from a newick string
     * @param newick The newick string
     * @return The tree
     * @throws Trees.TreeException Thrown if the tree cannot be created (e.g. incorrectly
     * formatted string)
     */
    public static Tree fromNewickString(String newick) throws TreeException
    {
        newick = newick.replaceAll("\\s", "");
        //Make sure all internal nodes are named;
        int nextAvaliable = 1;
        while (newick.contains("):"))
        {
            newick = newick.replaceFirst("\\):", ")_" + nextAvaliable + ":");
            nextAvaliable++;
        }
        while (newick.contains("),"))
        {
            newick = newick.replaceFirst("\\),", ")_" + nextAvaliable + ",");
            nextAvaliable++;
        }
        while (newick.contains("))"))
        {
            newick = newick.replaceFirst("\\)\\)", ")_" + nextAvaliable + ")");
            nextAvaliable++;
        }
        if (newick.contains(");"))
        {
            newick = newick.replaceFirst("\\);", ")_" + nextAvaliable + ";");
        }
        
	List<Branch> branches = new ArrayList<>();
        
	String s = newick.substring(0,newick.length()-1);
        
	while (s.indexOf(')') != -1)
	{
	    int i = 0;
	    int start = 0;
	    while (s.charAt(i) != ')')
	    {
		if (s.charAt(i) == '(')
		{
		    start = i;
		}
		i++;
	    }
            
	    String tail = s.substring(i+1);
            String parent = "";
	    int ci = tail.length();
	    if (tail.indexOf(":") > -1)
	    {
		ci = Math.min(ci,tail.indexOf(":"));
	    }
	    if (tail.indexOf(",") > -1)
	    {
		ci = Math.min(ci,tail.indexOf(","));
	    }
	    if (tail.indexOf(")") > -1)
	    {
		ci = Math.min(ci,tail.indexOf(")"));
	    }
            parent = tail.substring(0,ci);
	               
	    String ss = s.substring(start+1,i);
	    String[] cc = ss.split(",");
	    for (String c : cc)
	    {
		String[] pp = c.split(":");
		String child = pp[0];
                double length = Double.NaN;
		if (pp.length > 1)
		{
		    try
		    {
			length = Double.valueOf(pp[1]);
		    }
		    catch (NumberFormatException e)
		    {
			throw new TreeException("Length is not a number");
		    }
		}
                branches.add(new Branch(parent,child,length));
	    }
            
            s = s.substring(0,start) + tail;
	}
        
        if (s.matches("[;\\(\\),]"))
        {
            throw new TreeException("Tree does not appear to be formatted correctly");
        }
            
        if (s.matches(":"))
        {
            throw new TreeException("Root node can not have a length associated with it");
        }
        
        return new Tree(branches);
    }

    /**
     * Creates a tree from a file containing a Newick string
     * @param f The input file
     * @return The tree
     * @throws InputException If there is a problem reading from the input file
     */
    public static Tree fromFile(File f) throws InputException
    {
	String line = null;
	try
	{
	    BufferedReader in = new BufferedReader(new FileReader(f));
	    line = in.readLine();
	    in.close();
	    Tree t = Tree.fromNewickString(line);
	    return t;
	}
	catch (FileNotFoundException e)
 	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","File does not exist",e);
	}
	catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
	catch (TreeException e)
	{
	    throw new InputException(f.getAbsolutePath(),line,"Not a valid tree",e);
	}
    }
    
    public static Tree randomTree(List<String> taxa, boolean rooted) throws TreeException
    {
        if ( (rooted && (taxa.size() <= 1)) ||
                (!rooted && (taxa.size() <= 2)))
        {
            throw new TreeException("Not enough taxa to create a tree");
        }
        
        Random random = new Random();
        
        int i = 1;
        List<Branch> b = new ArrayList();
        LinkedList<String> t = new LinkedList<>(taxa);
        
        b.add(new Branch("_" + Integer.toString(i), t.pollFirst()));
        b.add(new Branch("_" + Integer.toString(i), t.pollFirst()));
        if (!rooted)
        {
            b.add(new Branch("_" + Integer.toString(i), t.pollFirst()));
        }
        
        while (!t.isEmpty())
        {
            i++;
            Branch rb = b.remove(random.nextInt(b.size()));
            b.add(new Branch(rb.getParent(), "_" + Integer.toString(i)));
            b.add(new Branch("_" + Integer.toString(i), rb.getChild()));
            b.add(new Branch("_" + Integer.toString(i), t.pollFirst()));
        }
        
        return new Tree(b);
    }
}
