package ModelTest;

import Alignments.Alignment;

public class IdentityAdapter implements Adapter
{
    public IdentityAdapter()
    {
        
    }
    
    public double likelihood(Alignment distinct)
    {
        return 0.0;
    }
    
    public int numberParameters()
    {
        return 0;
    }
}
