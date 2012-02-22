package Utils;

/**
 * Used to represent a range (using doubkes)
 * @author Daniel Money
 * @version 1.0
 */
public class Range
{
    /**
     * Constructor.  If lower > upper automatically swaps.
     * @param lower The lower value of the range
     * @param upper The upper value of the range
     */
    public Range(double lower, double upper)
    {
	l = Math.min(lower,upper);
	u = Math.max(lower,upper);
    }

    /**
     * Gets the lower value
     * @return The lower value
     */
    public double lower()
    {
	return l;
    }

    /**
     * Gets the upper value
     * @return The upper value
     */
    public double upper()
    {
	return u;
    }

    private double l;
    private double u;
}
