package Likelihood;

import Parameters.Parameters;

public class BasicLikelihood
{
    public BasicLikelihood(double likelihood, Parameters p)
    {
        this.l = likelihood;
        this.p = p;
    }
    
    public double getLikelihood()
    {
        return l;
    }
    
    public Parameters getParameters()
    {
        return p;
    }    
        
    public String toString()
    {
        return Double.toString(l);
    }
    
    private double l;
    private Parameters p;
}
