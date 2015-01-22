package ModelTest;

import Alignments.Alignment;
import Alignments.Ambiguous;
import Alignments.PhylipAlignment;
import Likelihood.Calculator;
import Likelihood.Likelihood;
import Likelihood.StandardCalculator;
import Likelihood.StandardLikelihood;
import Models.Model;
import Models.RYModelFactory;
import Optimizers.GoldenSection;
import Optimizers.Optimizer;
import Parameters.Parameters;
import Trees.Tree;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class AdapterTest
{
    @Test
    public void adapterTest() throws Exception
    {
        Tree t = Tree.fromFile(new File("C:\\Users\\Daniel\\Documents\\GeLL\\Modelomatic\\my_output.trim.tree"));
        
        Set<String> any = new HashSet<>();
        any.add("A");
        any.add("C");
        any.add("T");
        any.add("G");
        Map<String,Set<String>> am = new HashMap<>();
        am.put("-",any);
        am.put("N",any);
        Ambiguous ambig = new Ambiguous(am);
        
        Alignment a = PhylipAlignment.fromFile(new File("C:\\Users\\Daniel\\Documents\\GeLL\\Modelomatic\\my_output.trim.data"),ambig);
        
        Map<String,String> recode = new HashMap<>();
        recode.put("C","Y");
        recode.put("T","Y");
        recode.put("A","R");
        recode.put("G","R");
        
        Set<String> rany = new HashSet<>();
        rany.add("R");
        rany.add("Y");
        Map<String,Set<String>> ram = new HashMap<>();
        ram.put("-",rany);
        ram.put("N",rany);
        Ambiguous rambig = new Ambiguous(ram);
        
        Alignment ra = a.recode(recode,rambig);
        
        Parameters p = t.getParametersForEstimation();
        Model ry = RYModelFactory.RY(p);
        
        Calculator<StandardLikelihood> c = new StandardCalculator(ry,ra,t);
        
        Optimizer o = new GoldenSection();
        
        Likelihood l = o.maximise(c, p);
        
        Set<String> ignore = new HashSet<>();
        ignore.add("-");
        Adapter ema = new EmpericalAdapter(recode,ignore);        
        double emla = ema.likelihood(a);
        
        Adapter eqa = new EqualAdapter(recode);        
        double eqla = eqa.likelihood(a);
        
        assertTrue(Math.abs(emla + l.getLikelihood() - (-4424.63)) < 0.005);
        assertTrue(Math.abs(eqla - (Math.log(0.5) * 4662)) < 1e-6 );
    }
}
