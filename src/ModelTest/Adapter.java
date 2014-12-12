package ModelTest;

import Alignments.Alignment;

public interface Adapter
{
    public double likelihood(Alignment distinct);
    public int numberParameters();
}
