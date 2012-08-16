package Likelihood;

import Parameters.Parameters;

public interface HasLikelihood
{
    public double getLikelihood();
    
    public Parameters getParameters();
}
