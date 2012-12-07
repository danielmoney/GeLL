package Alignments;

/**
 * Used to represent a unique site in an alignment.  Augments the normal site
 * class with a count of how often the site occurs
 */
public class UniqueSite extends Site
{
    /**
     * Default constructor
     * @param s The site
     * @param c How often the site occurs
     */
    public UniqueSite(Site s, int c)
    {
        super(s);
        this.c = c;
    }

    /**
     * Get the number of times the site occurs in the related alignment
     * @return The number of times the site occurs
     */
    public int getCount()
    {
        return c;
    }

    public String toString()
    {
        return super.toString() + "\t" + c;
    }

    private int c;
}
