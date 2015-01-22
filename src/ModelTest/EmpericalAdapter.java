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

public class EmpericalAdapter implements Adapter
{
    public EmpericalAdapter(Map<String,String> mapping)
    {
        this(mapping, new HashSet<String>());
    }
        
    //Codon: Distinct, AA: Compound
    //Codon: Distinct, Nucelotide: Compound
   
    public EmpericalAdapter(Map<String,String> mapping, Set<String> ignore)
    {
        this.mapping = mapping;
        this.ignore = ignore;
    }
    
    public double likelihood(Alignment distinct)
    {
        Map<String,Double> counts = new HashMap<>();
        int total = 0;
        
        for (Site s: distinct)
        {
            for (String t: distinct.getTaxa())
            {
                try
                {
                    if (!ignore.contains(s.getRawCharacter(t)))
                    {
                        Set<String> cc = s.getCharacter(t);
                        for (String c: cc)
                        {
                            if (!counts.containsKey(c))
                            {
                                counts.put(c,0.0);
                            }
                            counts.put(c, counts.get(c) + 1.0/ (double) cc.size());
                        }
                        total ++;
                    }
                }
                catch (AlignmentException ex)
                {
                    throw new UnexpectedError(ex);
                }
            }
        }
        
        Map<String,Double> ctot = new HashMap<>();
        for (Entry<String,Double> e: counts.entrySet())
        {
            String mapped = mapping.get(e.getKey());
            if (!ctot.containsKey(mapped))
            {
                ctot.put(mapped,0.0);
            }
            ctot.put(mapped,ctot.get(mapped) + 
                    (e.getValue() / (double) total));
        }
        
        Map<String,Double> probs = new HashMap<>();
        for (Entry<String,Double> e: counts.entrySet())
        {
            probs.put(e.getKey(),
                    (e.getValue() / (double) total));
        }
        
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
                    top += probs.get(c);
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
    private Set<String> ignore;
}
