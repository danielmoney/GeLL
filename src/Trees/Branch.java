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

/**
 * Represents a branch in a tree
 * @author Daniel Money
 * @version 1.0
 */
public class Branch
{
    /**
     * Constructor for a branch of unknown length
     * @param parent The parent node, that is the node nearer the root
     * @param child The child node
     */
    public Branch(String parent, String child)
    {
        this(parent,child,Double.NaN);
    }
    
    /**
     * Constructor for a branch of known length
     * @param parent The parent node, that is the node nearer the root
     * @param child The child node
     * @param length The length of the branch
     */
    public Branch(String parent, String child, double length)
    {
        this.parent = parent;
        this.child = child;
        this.length = length;
    }
    
    /**
     * Gets the parent node of the branch
     * @return The parent node
     */
    public String getParent()
    {
        return parent;
    }
    
    /**
     * Get the child node of the branch
     * @return The child node
     */
    public String getChild()
    {
        return child;
    }
    
    /**
     * Get the length of the branch
     * @return The length of the branch
     * @throws Trees.TreeException Thrown if the branch is of unknown length
     */
    public double getLength() throws TreeException
    {
        if (!Double.isNaN(length))
        {
            return length;
        }
        else
        {
            throw new TreeException("Branch Length is undefined");
        }
    }
    
    /**
     * Tests whether this branch has a length associated with it
     * @return Whether this branch has a length
     */
    public boolean hasLength()
    {
        return !Double.isNaN(length);
    }
    
    public int hashCode()
    {
        return child.hashCode() + parent.hashCode() * 961;
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof Branch)
        {
            Branch b = (Branch) o;
            if ((b.child.equals(child)) && (b.parent.equals(parent)))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    public String toString()
    {
        return (parent + "\t->" + child + "\t:" + length);
    }
    
    private String parent;
    private String child;
    private double length;
}
