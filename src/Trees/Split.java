package Trees;

import java.util.TreeSet;

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
    
    public TreeSet<String> getMembers()
    {
        return members;
    }
    
    public TreeSet<String> getInverseMembers()
    {
        return inverseMembers;
    }
    
    public double getLength()
    {
        return length;
    }
    
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
