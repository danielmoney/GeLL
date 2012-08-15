/*
 * This file is part of GeLL.
 * 
 * GeLL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeLL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeLL.  If not, see <http://www.gnu.org/licenses/>.
 */
package Maths;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a mahematical function, variable or constant
 * @author Daniel Money
 * @version 1.3
 */
public interface CompiledFunction
{
    /**
     * Computes the result of the function
     * @param values Map from variable name to value
     * @return The value of the function
     * @throws NoSuchVariable Thrown if values does not contain a needed variable 
     */
    public double compute(Map<String,Double> values) throws NoSuchVariable;
    /**
     * Returns a list of variable that the function expects to be passed to it
     * @return The list of variable names tyhat need to be passed
     */
    public Set<String> neededParams();
    
    /**
     * Represents a variable - simply returns the value of the variable
     */
    public class Variable implements CompiledFunction
    {
        /**
         * Default constructor
         * @param v The variables name
         */
        public Variable(String v)
        {
            this.v = v;
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
        {
            if (values.containsKey(v))
            {
                return values.get(v);
            }
            else
            {
                throw new NoSuchVariable(v);
            }
        }
        
        public Set<String> neededParams()
        {
            Set<String> ret = new TreeSet<>();
            ret.add(v);
            return ret;
        }
        
        private String v;
    }
    
    /**
     * Represents a constant
     */
    public class Constant implements CompiledFunction
    {
        /**
         * Default constructor
         * @param d The value of the constant
         */
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
    
    /**
     * The addition function
     */
    public class Add implements CompiledFunction
    {
        /**
         * Default constructor
         * @param a a in a + b
         * @param b b in a + b
         */
        public Add(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
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
    
    /**
     * The subtract function
     */    
    public class Subtract implements CompiledFunction
    {
        /**
         * Default constructor
         * @param a a in a - b
         * @param b b in a - b
         */
        public Subtract(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
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

    /**
     * The multiply function
     */    
    public class Multiply implements CompiledFunction
    {
        /**
         * Default constructor
         * @param a a in a * b
         * @param b b in a * b
         */
        public Multiply(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
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

    /**
     * The divide function
     */
    public class Divide implements CompiledFunction
    {
        /**
         * Default constructor
         * @param a a in a / b
         * @param b b in a / b
         */
        public Divide(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
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
    
    /**
     * The power function
     */    
    public class Power implements CompiledFunction
    {
        /**
         * Default constructor
         * @param a a in a ^ b
         * @param b b in a ^ b
         */
        public Power(CompiledFunction a, CompiledFunction b)
        {
            this.a = a;
            this.b = b;
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
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
    
    /**
     * Represents an arbitary function defined in a {@link FunctionParser}
     */
    public class Function implements CompiledFunction
    {
        /**
         * Default constructor
         * @param p The function parser to be used to parse this function
         * @param name The name of the function (which is passed to the function parser)
         * @param inputs The inputs to the function
         * @throws WrongNumberOfVariables If the number of inputs past is not the number expected
         * @throws NoSuchFunction Thrown if values does not contain a needed variable  
         */
        public Function(FunctionParser p, String name, CompiledFunction[] inputs) throws WrongNumberOfVariables, NoSuchFunction
        {
            this.p = p;
            this.name = name;
            this.inputs = inputs;
            if (inputs.length != p.numberInputs(name))
            {
                throw new WrongNumberOfVariables(name,p.numberInputs(name),inputs.length);
            }
        }
        
        public double compute(Map<String,Double> values) throws NoSuchVariable
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
