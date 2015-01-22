package ModelTest;

import Alignments.Alignment;
import Alignments.AlignmentException;
import Alignments.Site;
import Exceptions.UnexpectedError;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EqualAdapter implements Adapter
{
    //Codon: Distinct, AA: Compound
    //Codon: Distinct, Nucelotide: Compound
   
    public EqualAdapter(Map<String,String> mapping)
    {
        this.mapping = mapping;
        
        ctot = new HashMap<>();
        for (Entry<String,String> e: mapping.entrySet())
        {
            String mapped = e.getValue();
            if (!ctot.containsKey(mapped))
            {
                ctot.put(mapped,0);
            }
            ctot.put(mapped,ctot.get(mapped) + 1);
        }
    }
    
    public double likelihood(Alignment distinct)
    {        
        double l = 0.0;
        for (Site s: distinct)
        {
            double sp = 1.0;
            for (String t: distinct.getTaxa())
            {
                Set<String> mappedTo = new HashSet<>();
                double top = 0.0;
                for (String c: s.getCharacter(t))
                {
                    top ++;
                    mappedTo.add(mapping.get(c));
                }
                
                double bot = 0.0;
                
                for (String c: mappedTo)
                {
                    bot += ctot.get(c);
                }
                
                sp = sp * (top / bot);
            }
            l += Math.log(sp);
        }
        return l;
    }
    
    public int numberParameters()
    {
        return mapping.size() - 1;
    }

    private Map<String,String> mapping;
    private Map<String,Integer> ctot;
}
