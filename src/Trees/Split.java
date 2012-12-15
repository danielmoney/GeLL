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

import java.util.TreeSet;

/**
 * Represents a split
 * @author Daniel Money
 * @version 2.0
 */
public class Split
{
    Split(TreeSet<String> members, TreeSet<String> inverseMembers, double length)
    {
        if (members.first().compareTo(inverseMembers.first()) < 0)
        {
            this.members = members;
            this.inverseMembers = inverseMembers;
        }
        else
        {
            this.members = inverseMembers;
            this.inverseMembers = members;
        }
        this.length = length;
    }
    
    /**
     * Returns the members of one "half" of the split.  This will always return
     * the half with the smaller valued (by string comparision) member
     * @return The members of the split
     */
    public TreeSet<String> getMembers()
    {
        return members;
    }
    
    /**
     * Returns the other "half" of the split to {@link #getMembers() }
     * @return The inverse members
     */
    public TreeSet<String> getInverseMembers()
    {
        return inverseMembers;
    }
    
    /**
     * Gets the length of the branch associated with this split
     * @return The branch length
     */
    public double getLength()
    {
        return length;
    }
    
    /**
     * Tests whether two splits are equal except for lentgth
     * @param s The other split
     * @return Whether the two splits are equal except for length
     */
    public boolean equalExceptLength(Split s)
    {
        return (members.equals(s.members) && inverseMembers.equals(s.inverseMembers));
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof Split)
        {
            Split s = (Split) o;
            return (members.equals(s.members) && inverseMembers.equals(s.inverseMembers) && (length == s.length));
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        return members.hashCode();
    }
    
    public String toString()
    {
        String ret = "";
        for (String s: members)
        {
            ret = ret + s + ",";
        }
        ret = ret.substring(0, ret.length()-1);
        ret = ret + " | ";
        for (String s: inverseMembers)
        {
            ret = ret + s + ",";
        }
        ret = ret.substring(0, ret.length()-1);
        return ret;
    }
    
    private TreeSet<String> members;
    private TreeSet<String> inverseMembers;
    private double length;
}
