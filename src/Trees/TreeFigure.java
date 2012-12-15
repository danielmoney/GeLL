package Trees;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class for visualising trees.  Produces a SVG file.<br>
 * <b>Note this class is still being developed.  It works but may not have all
 * appropiate error checking nor have all needed methods been written.
 * I make no guarantees that future versions will have the same methods.</b>
 * @author Daniel Money
 * @version 2.0
 */
public class TreeFigure
{
    /**
     * Constructor
     * @param t The tree to visualize
     * @throws TreeException If the tree does not have branch lengths
     */
    public TreeFigure(Tree t) throws TreeException
    {
        this.t = t;
        
        nf.setMinimumFractionDigits(2);
	nf.setMaximumFractionDigits(2);

        v = vs * t.getSize();
 
        Map<String, Double> maxdists = new HashMap<>();
        for (String n: t.getInternal())
        {
            maxdists.put(n,0.0);
        }
        for (String l: t.getLeaves())
        {
            maxdists.put(l,0.0);
        }


        lengths = new HashMap<>();
	for(Branch b: t.getBranches())
	{
            maxdists.put(b.getParent(), Math.max(
                    maxdists.get(b.getChild()) + b.getLength(),
                    maxdists.get(b.getParent())));
            lengths.put(b.getChild(), b.getLength());
	}

	lengths.put(t.getRoot(),0.02*maxdists.get(t.getRoot()));

	scale = (h-100) / (maxdists.get(t.getRoot()) *1.02);
	       
        names = new HashMap<>();
        leafColors = new HashMap<>();
        for (String n: t.getLeaves())
        {
            names.put(n,n);
            leafColors.put(n, "black");
        }
        
        branchColors = new HashMap<>();
        for (Branch b: t.getBranches())
        {
            branchColors.put(b.getChild(), "black");
        }
        branchColors.put(t.getRoot(),"black");
    }
    
    /**
     * Sets the color of branches.  Default is black, either if this method
     * is not called at all or it is not bassed a color for a branch.  
     * Colors should be defined as in a SVG file.
     * @param colors Map from branch to color
     */
    public void setBranchColors(Map<Branch, String> colors)
    {
        for (Branch b: colors.keySet())
        {
            branchColors.put(b.getChild(), colors.get(b));
        }
    }
    
    /**
     * Sets the color of the leaf text.  Default is black, either if this method
     * is not called at all or it is not bassed a color for a branch
     * @param colors Map from leaf name to color.  Colors should be defined as
     * in a SVG file.
     */
    public void setLeafColor(Map<String,String> colors)
    {
        leafColors = colors;
    }
    
    /**
     * Sets the color of the root branch.  Defaults to black if this method is
     * not called
     * @param color The color of the branch.  Color should be defined as
     * in a SVG file.
     */
    public void setRootBranchColor(String color)
    {
        branchColors.put(t.getRoot(), color);
    }
    
    /**
     * Overwrites the default leaf text (the taxa names).
     * @param names Map from taxa name to string where the string is the new text
     * for each leaf.  Should include new text for every leaf.
     */
    public void setLeavesText(Map<String, String> names)
    {
        this.names = names;
    }
    
    /**
     * Sets the spacing (in co-ordinate terms) between the horizontal lines
     * @param spacing The spacing to use
     */
    public void setVerticalSpacing(int spacing)
    {
        vs = spacing;
    }
    
    /**
     * Prints the tree to the given file
     * @param f The file to print to
     * @param showLengths Display branch lengths on output
     * @throws IOException Thrown if there is a problem writing the file
     * @throws TreeException Thrown if there is a problem with the tree
     */
    public void printSVG(File f, boolean showLengths) throws IOException, TreeException
    {
        next = 0;
        out = new PrintWriter(new FileWriter(f));
        
        topMatter(out);

	node(t.getRoot(), 0, showLengths);

	out.println("</svg>");
	out.close();
    }

    private int node(String n, int start, boolean showLengths) throws TreeException
    {
	if (t.getLeaves().contains(n))
	{
	    int end = start + (int)(scale * lengths.get(n));
	    int y = next*vs+(vs/2);
	    next++;
	    int x1 = start;
	    int x2 = end;
	    line(x1,x2,y,y,branchColors.get(n),"Branch " + n);
	    ttext(x2 + 10, y, names.get(n), leafColors.get(n), "Name " + n);
	    if (showLengths && (lengths.get(n) >= 0.001))
	    {
		ltext((start + end) / 2, y, nf.format(lengths.get(n)), "Length " + n);
	    }
	    return y;
	}
	else
	{
	    int min = Integer.MAX_VALUE;
	    int max = Integer.MIN_VALUE;
	    double avg = 0.0;
	    Set<Branch> children = t.getBranchesByParent(n);
	    int end = (int)(start + scale*lengths.get(n));
	    for (Branch c: children)
	    {
		int ret = node(c.getChild(),end,showLengths);
		min = Math.min(min,ret);
		max = Math.max(max,ret);
		avg = avg + ret / children.size();
	    }
	    line(start,end,(int)avg,(int)(avg), branchColors.get(n),
		    "Branch " + n);
	    line(end,end,min,max,branchColors.get(n),"Vert " + n);
	    if (showLengths && (lengths.get(n) >= 0.001) && !n.equals(t.getRoot()))
	    {
		ltext((start + end) / 2, (int) avg, nf.format(lengths.get(n)), "Length " + n);
	    }
	    return (int) avg;
	}
    }

    private void topMatter(PrintWriter out)
    {
	out.println("<?xml version=\"1.0\" standalone=\"no\"?>");
	out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
	out.println("  \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">");
	out.print("<svg width=\"");
	out.print(h/dpcm);
	out.print("cm\" height=\"");
	out.print(v/dpcm);
	out.print("cm\" viewBox=\"0 0 ");
	out.print(h);
	out.print(" ");
	out.print(v);
	out.print("\"\n");
	out.println("     xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">");
    }

    private void line(int x1, int x2, int y1, int y2, String color, String comment)
    {
	out.print("<line ");
	//out.print(f);
	out.print(lf);
	out.print(" x1=\"");
	out.print(x1);
	out.print("\" x2=\"");
	out.print(x2);
	out.print("\" y1=\"");
	out.print(y1);
	out.print("\" y2=\"");
	out.print(y2);
        out.print("\" stroke=\"");
        out.print(color);
	out.print("\" />");
	out.print("<!--");
	out.print(comment);
	out.print("-->\n");
    }

    private void ttext(int x, int yy, String text, String color, String comment)
    {
	int y = yy + (int)(tfs*0.4);
	out.print("<text ");
	//out.print(f);
	out.print("font-size=\"" + tfs + "\"");
	out.print(" x=\"");
	out.print(x);
	out.print("\" y=\"");
	out.print(y);
	out.print("\" fill=\"");
        out.print(color);
	out.print("\" >");
	out.print(text);
	out.print("</text>");
	out.print("<!--");
	out.print(comment);
	out.print("-->\n");
    }

    private void ltext(int x, int yy, String text, String comment)
    {
	int y = yy - (lfs / 2);
	out.print("<text ");
	//out.print(f);
	out.print("font-size=\"" + lfs + "\" text-anchor=\"middle\"");
	out.print(" x=\"");
	out.print(x);
	out.print("\" y=\"");
	out.print(y);
	out.print("\" ");
	out.print(">");
	out.print(text);
	out.print("</text>");
	out.print("<!--");
	out.print(comment);
	out.print("-->\n");
    }

    int h = 1600;
    int v;
    int tfs = 24;
    int lfs = 16;
    int vs = 60;
    int dpcm = 50;
    String lf = "stroke-width=\"2\"";

    NumberFormat nf = NumberFormat.getNumberInstance();

    int next = 0;
    Map<String,Double> lengths;
    Map<String,String> names;
    Map<String,String> branchColors;
    Map<String,String> leafColors;
    Tree t;
    PrintWriter out;
    double scale;
}
