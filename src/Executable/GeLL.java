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

package Executable;

import Alignments.Alignment;
import Alignments.Ambiguous;
import Alignments.DuplicationAlignment;
import Alignments.PhylipAlignment;
import Ancestors.AncestralJoint;
import Ancestors.AncestralMarginal;
import Exceptions.GeneralException;
import Exceptions.InputException;
import Exceptions.UnexpectedError;
import Likelihood.StandardCalculator;
import Likelihood.StandardLikelihood;
import Maths.SquareMatrix;
import Models.Distributions;
import Models.Model;
import Models.Model.ModelException;
import Models.RateCategory.RateException;
import Optimizers.GoldenSection;
import Optimizers.NelderMead;
import Optimizers.Optimizer;
import Parameters.Parameters;
import Simulations.Simulate;
import Trees.Tree;
import Utils.PossibleSettings;
import Utils.SetSettings;
import Utils.SettingException;
import Utils.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A general purpose driver around the library.  See {@code GeLL.html} in
 * the distribution for information on how to run this driver.
 * 
 * @author Daniel Money
 * @version 1.2
 */
public class GeLL
{
    private GeLL()
    {
        //No reason for anyone to be creating an instance of this class
    }

    /**
     * Main function
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
	String debugFile = null;
	try
	{
	    // SETTINGS
	    PossibleSettings ps = getPossibleSettings();
	    SetSettings ss = getSetSettings(args);
	    Settings settings = new Settings(ps,ss);

	    // CONTROL
            setDebugLevel(settings.getSetting("Control", "DebugLevel"));
	    debugFile = settings.getSetting("Control", "DebugFile");
            
            setMatrixExpMethod(settings.getSetting("Control", "MatrixExponentation"));
            setMatrixForceSquare(settings.getSetting("Control", "ForceSquare"));
            setDistributionMethod(settings.getSetting("Control", "Distributions"));
            
	    Alignment a = null;
	    Tree t = null;
	    Map<String,Model> m = null;
	    Parameters p = null;
	    Alignment missing = null;

            // LIKELIHOOD
	    if (settings.hasGroup("Likelihood"))
	    {
                a = getAlignment(settings.getSetting("Likelihood", "AlignmentType"),
                        settings.getSetting("Likelihood", "Alignment"),
                        settings.getSetting("Likelihood","Ambig"));
                
                if (settings.getSetting("Likelihood", "Missing") != null)
                {
                    missing = getAlignment(settings.getSetting("Likelihood", "AlignmentType"),
                            settings.getSetting("Likelihood", "Missing"),
                            settings.getSetting("Likelihood","MissingAmbig"));
                }

		t = Tree.fromFile(new File(settings.getSetting("Likelihood", "TreeInput")));

		m = getModels(new File(settings.getSetting("Likelihood", "Model")));

                p = getParameters(settings.getSetting("Likelihood","ParameterInput"), t);

		StandardCalculator c = new StandardCalculator(m,a,t,missing);

		Optimizer o = getOptimizer(settings.getSetting("Likelihood", "Optimizer"));
                o.setCheckPointFile(new File(settings.getSetting("Likelihood", "Checkpoint")));
                o.setCheckPointFrequency(getCheckpointFreq(settings.getSetting("Likelihood", "Checkpoint")),
                        TimeUnit.MINUTES);

		StandardLikelihood like = getLikelihoodResult(settings.getSetting("Likelihood","Restart"),o,c,p);
                
                //Update p to be the optimized value
                p = like.getParameters();
                t = new Tree(t,p);
                
                outputLikelihoodResults(settings.getSetting("Likelihood", "ParameterOutput"),
                        settings.getSetting("Likelihood", "TreeOutput"),p,like.getLikelihood(),t);
	    }
	    
	    //ANCESTRAL
	    if (settings.hasGroup("Ancestral"))
	    {
                //All these sort of calls will ensure either the likelihood settings or
                //the specific settings are used.  If both or neither are set an exception is thrown
		Tree at = getTree(settings.getSetting("Ancestral", "Tree"), t);

		Alignment aa = getAlignment(settings.getSetting("Ancestral", "AlignmentType"),
                        settings.getSetting("Ancestral", "Alignment"), a);

		Map<String,Model> am = getModel(settings.getSetting("Ancestral", "Model"),m);

                Parameters ap = getParameters(settings.getSetting("Ancestral","Parameters"),p,at);
		
                Alignment anc = getAncestral(settings.getSetting("Ancestral", "Type"),am,aa,at,ap);
                
                String alignmentType = getAlignmentType(settings.getSetting("Ancestral", "AlignmentType"),
                        settings.getSetting("Likelihood", "AlignmentType"));
                outputAlignment(settings.getSetting("Ancestral", "Output"),
                        alignmentType, anc);
	    }
            
	    //SIMULATION
	    if (settings.hasGroup("Simulation"))
	    {
		Tree st = getTree(settings.getSetting("Simulation", "Tree"), t);

		Map<String,Model> sm = getModel(settings.getSetting("Simulation", "Model"),m);

                Parameters sp = getParameters(settings.getSetting("Simulation","Parameters"),p,st);
                
                Alignment smissing = null;
                if (!((missing == null) && settings.getSetting("Simulation","Missing") == null))
                {
                    smissing = getAlignment(settings.getSetting("Simulation","AlignmentType"),
                            settings.getSetting("Simulation","Missing"), missing);
                }
		
		Simulate sim = new Simulate(sm,st,sp,smissing);
		Alignment simAlign = sim.getAlignment(getLength(settings.getSetting("Simulation","Parameters")));

		String sat = settings.getSetting("Simulation", "AlignmentType");
		if (sat == null)
		{
		    sat = settings.getSetting("Likelihood", "AlignmentType");
		}

                String alignmentType = getAlignmentType(settings.getSetting("Simulation", "AlignmentType"),
                        settings.getSetting("Likelihood", "AlignmentType"));
                outputAlignment(settings.getSetting("Simulation", "Output"),
                        alignmentType, simAlign);
	    }
      	}
	catch (GeneralException e)
	{
	    exceptionHandler(e,debugFile);
	}
	catch (SettingException e)
	{
	    exceptionHandler(e,debugFile);
	}
    }

    private static void exceptionHandler(Exception e, String debugFile)
    {
	try
	{
	    PrintStream out;
	    if (debugFile != null)
	    {
		out = new PrintStream(new FileOutputStream(new File(debugFile)));
	    }
	    else
	    {
		out = System.err;
	    }
	    out.println(e.toString());
	    out.close();
	}
	catch (Exception ee)
	{
	    System.err.println("Unable to write to debug file");
	    System.err.println("\t" + ee.getMessage());
	    System.err.println();
	    System.err.println(e.toString());
	}
    }
    
    private static PossibleSettings getPossibleSettings()
    {
        try
        {
            PossibleSettings ps = new PossibleSettings();

            ps.addGroup("Likelihood", false);
            ps.addNeededSetting("Likelihood", "AlignmentType");
            ps.addNeededSetting("Likelihood", "Alignment");
            ps.addNeededSetting("Likelihood", "TreeInput");            
            ps.addNeededSetting("Likelihood", "Model");
            ps.addOptionalSetting("Likelihood", "ParameterInput", null);
            ps.addOptionalSetting("Likelihood", "TreeOutput", null);
            ps.addOptionalSetting("Likelihood", "ParameterOutput", null);
            ps.addOptionalSetting("Likelihood", "Optimizer", "GoldenSection");
            ps.addOptionalSetting("Likelihood", "Checkpoint", null);
            ps.addOptionalSetting("Likelihood", "CheckpointFreq", null);
            ps.addOptionalSetting("Likelihood", "Restart", null);
            ps.addOptionalSetting("Likelihood", "Ambig", null);
            ps.addOptionalSetting("Likelihood", "Missing", null);
            ps.addOptionalSetting("Likelihood", "MissingAmbig", null);

            ps.addGroup("Ancestral", false);
            ps.addOptionalSetting("Ancestral", "Tree", null);
            ps.addOptionalSetting("Ancestral", "Model", null);
            ps.addOptionalSetting("Ancestral", "Parameters", null);
            ps.addOptionalSetting("Ancestral", "Alignment", null);
            ps.addOptionalSetting("Ancestral", "AlignmentType",null);
            ps.addOptionalSetting("Ancestral", "Type", "Joint");
            ps.addNeededSetting("Ancestral", "Output");

            ps.addGroup("Simulate", false);
            ps.addOptionalSetting("Simulate", "AlignmentType",null);
            ps.addOptionalSetting("Simulate", "Tree", null);
            ps.addOptionalSetting("Simulate", "Model", null);
            ps.addOptionalSetting("Simulate", "Parameters", null);
            ps.addNeededSetting("Simulate", "Length");
            ps.addNeededSetting("Simulate", "Output");

            ps.addGroup("Control", false);
            ps.addOptionalSetting("Control", "DebugLevel", "1");
            ps.addOptionalSetting("Control", "DebugFile", null);
            ps.addOptionalSetting("Control", "Distributions", "Repeat");
            ps.addOptionalSetting("Control", "MatrixExponentation", "Taylor");
            ps.addOptionalSetting("Control", "ForceSquare", "0");

            return ps;
        }
        catch (SettingException ex)
        {
            throw new UnexpectedError(ex);
        }
    }
     
    private static SetSettings getSetSettings(String[] args) throws SettingException
    {
        try
        {
            return SetSettings.fromFileAndCommandLine(new File(args[0]),args);
        }
        catch (FileNotFoundException e)
        {
            throw new SettingException("Settings file not found");
        }
        catch (IOException e)
        {
            throw new SettingException("Unable to read setting file");
        }
    }
    
    private static void setDebugLevel(String setting) throws SettingException
    {
        try
        {
            int d = Integer.parseInt(setting);
            switch (d)
            {
                case 1:
                    GeneralException.setDebug(GeneralException.Debug.MESSAGE);
                    break;
                case 2:
                    GeneralException.setDebug(GeneralException.Debug.UNDERLYING_MESSAGE);
                    break;
                case 3:
                    GeneralException.setDebug(GeneralException.Debug.STACK_TRACE);
                    break;
                default:
                    throw new SettingException("Invalid debug level");
            }
        }
        catch (NumberFormatException e)
        {
            throw new SettingException("Invalid debug level");
        }
    }
    
    private static void setMatrixExpMethod(String setting) throws SettingException
    {
        boolean mat = false;
        if (setting.equals("Taylor"))
        {
            SquareMatrix.setExpMethod(SquareMatrix.Calculation.TAYLOR);
            mat = true;
        }
        if (setting.equals("Eigen"))
        {
            SquareMatrix.setExpMethod(SquareMatrix.Calculation.EIGEN);
            mat = true;
        }
        if (!mat)
        {
            throw new SettingException("Invalid MatrixExponentation option");
        }
    }
    
    private static void setDistributionMethod(String setting) throws SettingException
    {
        boolean dist = false;
        if (setting.equals("Repeat"))
        {
            Distributions.setMethod(Distributions.Calculation.REPEAT);
            dist = true;
        }
        if (setting.equals("Eigen"))
        {
            Distributions.setMethod(Distributions.Calculation.EIGEN);
            dist = true;
        }
        if (!dist)
        {
            throw new SettingException("Invalid Distributions option");
        }
    }
    
    private static void setMatrixForceSquare(String setting) throws SettingException
    {

        try
        {
            int f = Integer.parseInt(setting);
            if (f >= 0)
            {
                SquareMatrix.setForce(f);
            }
            else
            {
                throw new SettingException("Invalid force square amount");
            }
        }
        catch (NumberFormatException e)
        {
            throw new SettingException("Invalid force square amount");
        }
    }
    
    private static Alignment getAlignment(String type, String file) throws GeneralException, SettingException
    {
        Alignment a = null;
        if (type.equals("Duplication"))
        {
            a = new DuplicationAlignment(new File(file));
        }
        if (type.equals("Sequence"))
        {
            a = new PhylipAlignment(new File(file));
        }
        if (a == null)
        {
            throw new SettingException("Invalid AlignmentType Setting");
        }
        return a;
    }
    
    private static Alignment getAlignment(String type, String file, String ambig) throws GeneralException, SettingException
    {
        Alignment missing = null;
        if (type.equals("Duplication"))
        {
            missing = new DuplicationAlignment(new File(file),
                    Ambiguous.fromFile(new File(ambig)));
        }
        if (type.equals("Sequence"))
        {
            missing = new PhylipAlignment(new File(file));
        }
        if (missing == null)
        {
            throw new SettingException("Invalid AlignmentType Setting");
        }
        return missing;
    }
    
    private static Optimizer getOptimizer(String setting) throws SettingException
    {
        Optimizer o = null;
        if (setting.equals("GoldenSection"))
        {
            o = new GoldenSection(1);
        }
        if (setting.equals("NelderMead"))
        {
            o = new NelderMead();
        }
        if (o == null)
        {
            throw new SettingException("Invalid Optimizer Setting");
        }
        return o;
    }
    
    private static StandardLikelihood getLikelihoodResult(String restart, Optimizer o,
            StandardCalculator c, Parameters p) throws GeneralException, SettingException
    {
        StandardLikelihood like = null;
        if (restart == null)
        {
            if (p != null)
            {
                like = o.maximise(c, p);
            }
            else
            {
                throw new SettingException("No paramters given");
            }
        }
        else
        {
            like = o.restart(c, new File(restart));
        }
        return like;
    }
    
    private static void outputLikelihoodResults(String paramFile, String treeFile,
            Parameters p, double l, Tree t) throws GeneralException
    {
        if (paramFile != null)
        {
            p.toFile(new File(paramFile), l);
        }
        if (treeFile != null)
        {
            t.toFile(new File(treeFile),true);//??
        }
    }
    
    private static Tree getTree(String file, Tree t) throws GeneralException, SettingException
    {
        if (t != null)
        {
            if (file == null)
            {
                return t;
            }
            else
            {
                throw new SettingException("Likelihood tree calculated but also given tree");
            }            
        }
        else
        {
            if (file != null)
            {
                return Tree.fromFile(new File(file));
            }
            else
            {
                throw new SettingException("No tree to use - no likelihood result and no file given");
            }
        }
    }
    
    private static Alignment getAlignment(String type, String file, Alignment a)
            throws GeneralException, SettingException
    {
        if (a != null)
        {
            if (file == null)
            {
                return a;
            }
            else
            {
                throw new SettingException("Alignment given at both likelihood and ancestor setting");
            }              
        }
        else
        {
            if (file != null)
            {
                return getAlignment(file,type);
            }
            else
            {
                throw new SettingException("No alignment to use");
            }
        }
    }
    
    private static Map<String,Model> getModel(String file, Map<String,Model> m)
            throws GeneralException, SettingException
    {
        if (m != null)
        {
            if (file == null)
            {
                return m;
            }
            else
            {
                throw new SettingException("Model given at both likelihood and ancestor settings");
            }              
        }
        else
        {
            if (file != null)
            {
                return getModels(new File(file)); 
            }
            else
            {
                throw new SettingException("No model to use");
            }
        }
    }
    
    private static Map<String,Model> getModels(File f) throws InputException,
            ModelException, RateException
    {
        Map<String,Model> ret = new HashMap<>();
        BufferedReader in;
	try
	{
	    in = new BufferedReader(new FileReader(f));
	}
	catch (FileNotFoundException e)
 	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","File does not exist",e);
	}
        try
        {
            String line = in.readLine();
            if (line.startsWith("**"))
            {
                in.close();
                ret.put(null,Model.fromFile(f));
            }
            else
            {
                do
                {
                    String[] parts = line.split("\t",2);
                    ret.put(parts[0],Model.fromFile(new File(parts[1])));
                }
                while ((line = in.readLine()) != null);
            }
        }
        catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
        return ret;
    }
    
    private static Parameters getParameters(String file, Parameters p, Tree t)
            throws GeneralException, SettingException
    {
        if (p != null)
        {
            if (file == null)
            {
                return p;
            }
            else
            {
                throw new SettingException("Parameters given at both likelihood and ancestor settings");
            }              
        }
        else
        {
            if (file != null)
            {
                Parameters np = Parameters.fromFile(new File(file));
                np.addParameters(t.getParameters());
                return np;
            }
            else
            {
                throw new SettingException("No parameters to use");
            }
        }
    }
    
    private static int getLength(String setting) throws SettingException
    {
        try
        {
            int l = Integer.parseInt(setting);
            if (l <= 0)
            {
                throw new SettingException("Simulated alignment length must be  apositive integer");
            }
            return l;
        }
        catch (NumberFormatException ex)
        {
            throw new SettingException("Simulated alignment length must be  apositive integer");
        }
    }
    
    private static String getAlignmentType(String n, String def) throws SettingException
    {
        String type;
        if (def != null)
        {
            if (n == null)
            {
                type = def;
            }
            else
            {
                throw new SettingException("Alignment type given at both likelihood and ancestor settings");
            }              
        }
        else
        {
            if (n != null)
            {
                type = n;
            }
            else
            {
                throw new SettingException("No alignment type to use");
            }
        }
        if (type.equals("Duplication") || type.equals("Sequence"))
        {
            return type;
        }
        else
        {
            throw new SettingException("Invalid alignment type");
        }
    }
    
    private static void outputAlignment(String file, String type, Alignment a)
            throws GeneralException
    {
        if (type.equals("Duplication"))
        {
            DuplicationAlignment.writeFile(a, new File(file));
        }
        if (type.equals("Sequence"))
        {
            PhylipAlignment.writeFile(a, new File(file));
        }
    }
    
    private static Parameters getParameters(String pi, Tree t) throws GeneralException
    {
        Parameters p;
        if (pi != null)
        {
            p = Parameters.fromFile(new File(pi));
            p.addParameters(t.getParametersForEstimation());
        }
        else
        {
            p = null;
        }
        return p;
    }
    
    private static Alignment getAncestral(String type, Map<String,Model> m, Alignment a, Tree t, Parameters p)
            throws GeneralException, SettingException
    {
        if (type.equals("Joint"))
        {
            AncestralJoint aj = AncestralJoint.newInstance(m,a,t);
            return aj.calculate(p);
        }
        if (type.equals("Marginal"))
        {
            AncestralMarginal am = new AncestralMarginal(m,a,t);
            return am.calculate(p).getAlignment();
        }
        throw new SettingException("Invalid ancestral calculation type");
    }
    
    private static int getCheckpointFreq(String s) throws SettingException
    {
        try
        {
            int num = Integer.parseInt(s);
            if (num <= 0)
            {
                throw new SettingException("Checkpoint frequency must be positive");
            }
            return num;
        }
        catch(NumberFormatException ex)
        {
            throw new SettingException("Checkpoint frequency must be an integer");
        }
    }
}
