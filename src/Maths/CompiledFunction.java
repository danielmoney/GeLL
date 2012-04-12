package Maths;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public interface CompiledFunction
{
    public double compute(Map<String,Double> values);
    public Set<String> neededParams();
    
    public class Variable implements CompiledFunction
    {
        public Variable(String v)
        {
            this.v = v;
        }
        
        public double compute(Map<String,Double> values)
        {
            return values.get(v);
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = new TreeSet<>();
            ret.add(v);
            return ret;
        }
        
        private String v;
    }
    
    public class Constant implements CompiledFunction
    {
        public Constant(double d)
        {
            this.d = d;
        }
        
        public double compute(Map<String,Double> values)
        {
            return d;
        }
        
        public Set<String> neededParams()
        {
            return new TreeSet<>();
        }
        
        private double d;
    }
    
    public class Add implements CompiledFunction
    {
        public Add(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values)
        {
            return a.compute(values) + b.compute(values);
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = a.neededParams();
            ret.addAll(b.neededParams());
            return ret;
        }
        
        private CompiledFunction a;
        private CompiledFunction b;
    }
    
    public class Subtract implements CompiledFunction
    {
        public Subtract(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values)
        {
            return a.compute(values) - b.compute(values);
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = a.neededParams();
            ret.addAll(b.neededParams());
            return ret;
        }
        
        private CompiledFunction a;
        private CompiledFunction b;
    }
    
    public class Multiply implements CompiledFunction
    {
        public Multiply(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values)
        {
            return a.compute(values) * b.compute(values);
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = a.neededParams();
            ret.addAll(b.neededParams());
            return ret;
        }
        
        private CompiledFunction a;
        private CompiledFunction b;
    }
    
    public class Divide implements CompiledFunction
    {
        public Divide(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values)
        {
            return a.compute(values) / b.compute(values);
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = a.neededParams();
            ret.addAll(b.neededParams());
            return ret;
        }
        
        private CompiledFunction a;
        private CompiledFunction b;
    }
    
    public class Power implements CompiledFunction
    {
        public Power(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values)
        {
            return Math.pow(a.compute(values), b.compute(values));
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = a.neededParams();
            ret.addAll(b.neededParams());
            return ret;
        }
        
        private CompiledFunction a;
        private CompiledFunction b;
    }
    
    public class Function implements CompiledFunction
    {
        public Function(FunctionParser p, String name, CompiledFunction[] inputs) throws WrongNumberOfVariables
        {
            this.p = p;
            this.name = name;
            this.inputs = inputs;
            try
            {
                if (inputs.length != p.numberInputs(name))
                {
                    throw new WrongNumberOfVariables(name,p.numberInputs(name),inputs.length);
                }
            }
            catch (NoSuchFunction e)
            {
                //Shouldn't get here!
            }
        }
        
        public double compute(Map<String,Double> values)
        {
            Double[] vars = new Double[inputs.length];
            for (int i = 0; i < inputs.length; i++)
            {
                vars[i] = inputs[i].compute(values);
            }
            try
            {
                return p.evaluate(name, vars);
            }
            catch (WrongNumberOfVariables | NoSuchFunction e)
            {
                //Should never get here
                return 0.0;
            }
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = new TreeSet<>();
            for (int i = 0; i < inputs.length; i++)
            {
                ret.addAll(inputs[i].neededParams());
            }
            return ret;
        }
        
        private FunctionParser p;
        private String name;
        private CompiledFunction[] inputs;
    }
}
