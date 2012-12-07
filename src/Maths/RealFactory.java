package Maths;

import Maths.Real.RealType;

public class RealFactory
{
    public static Real getReal(RealType type, double d)
    {
        switch (type)
        {
            case SMALL_DOUBLE:
                return new SmallDouble(d);
            default:
                return new StandardDouble(d);
        }
    }
    
    public static Real getSmallestReal(RealType type)
    {
        switch (type)
        {
            case SMALL_DOUBLE:
                return SmallDouble.SMALLEST;
            default:
                return StandardDouble.SMALLEST;
        }
    }
}
