package ModelTest;

import Alignments.Alignment;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Parameters.Parameters;

public class TestInstance
{
    public TestInstance(Calculator<Likelihood> c, Parameters p)
    {
        this(c,p,"",new IdentityAdapter(),null);
    }
    
    public TestInstance(Calculator<Likelihood> c, Parameters p, String name)
    {
        this(c,p,name,new IdentityAdapter(),null);
    }
    
    public TestInstance(Calculator<Likelihood> c, Parameters p, Adapter adapter,
            Alignment distinct)
    {
        this(c,p,"",adapter,distinct);
    }
        
    public TestInstance(Calculator<Likelihood> c, Parameters p, String name, Adapter adapter,
            Alignment distinct)
    {
        this.c = c;
        this.p = p;
        this.name = name;
        this.adapterL = adapter.likelihood(distinct);
        this.adapterParams = adapter.numberParameters();
    }
    
    public Calculator<Likelihood> getCalculator()
    {
        return c;
    }
    
    public Parameters getParameters()
    {
        return p;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getNumberAdapterParams()
    {
        return adapterParams;
    }
    
    public double getAdapterL()
    {
        return adapterL;
    }
    
    private double adapterL;
    private int adapterParams;
    private Calculator<Likelihood> c;
    private Parameters p;
    private String name;
}
