package Alignments;

import Exceptions.InputException;
import Exceptions.OutputException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Represents a duplication "alignment" - a set of gene families with their
 * associated size in each species
 * @author Daniel Money
 * @version 1.0
 */
public class DuplicationAlignment extends Alignment
{
    /**
     * Creates a duplication alignment from a file.  File is tab seperated.
     * First row is a header file.  First field is ignored while subsequent fields
     * are the name of the species.  Each additional row represents a family.
     * The first field is an ID for the family while subsequent fields are the
     * size of the family in the appropiate species.
     * @param f The input file
     * @throws InputException Thrown if there is a problem with the input file
     * @throws AlignmentException Thrown if any family contains the wrong number
     * of species
     */
    public DuplicationAlignment(File f) throws InputException, AlignmentException
    {
        this(f,new Ambiguous(new HashMap<String,Set<String>>()));
    }

    /**
     * Creates a duplication alignment which contains ambiguous data.  File format
     * is described {@link #DuplicationAlignment(java.io.File)}.
     * @param f The input file
     * @param ambig Desription o the ambiguous data
     * @throws InputException Thrown if there is a problem with the input file
     * @throws AlignmentException Thrown if any family contains the wrong number
     * of species
     */
    public DuplicationAlignment(File f, Ambiguous ambig) throws InputException, AlignmentException
    {
	BufferedReader ain;
	try
	{
	    ain = new BufferedReader(new FileReader(f));
	}
	catch (FileNotFoundException e)
 	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","File does not exist",e);
	}
	try
	{
            String line = ain.readLine();
	    String[] names = line.split("\t");
            
            if (names.length < 3)
            {
                throw new AlignmentException("Taxa names", line, "One or fewer taxa given",null);
            }

	    for (int i = 1; i < names.length; i++)
	    {
                if (!taxa.contains(names[i]))
                {
                    taxa.add(names[i]);
                }
                else
                {
                    throw new AlignmentException("Taxa names", line, "Repeated taxa name",null);
                }
	    }

	    int i = 0;

	    while ((line = ain.readLine()) != null)
	    {
		i++;
		String[] split = line.split("\t");
                if (split.length != names.length)
		{
		    throw new AlignmentException("Family " + i, line, "Wrong number of species",null);
		}
                LinkedHashMap<String,String> sizes = new LinkedHashMap<>();
                for (int j = 1; j < split.length; j++)
                {
                    sizes.put(names[j], split[j]);
                }
                data.add(new Site(sizes,ambig));
	    }
	    ain.close();
	}
	catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
    }

    /**
     * Writes a alignment to a file in the duplication alignment format described
     * above.
     * @param a The alignment to write to the file. Need not be a duplication alignment
     * @param f The file to write to
     * @throws OutputException Thrown if there is a problem creating the file
     */
    public static void writeFile(Alignment a, File f) throws OutputException
    {
	try
	{
	    PrintStream out = new PrintStream(new FileOutputStream(f));
	    out.print("FamilyID");
	    for (String taxa: a.getTaxa())
	    {
		out.print("\t" + taxa);
	    }
	    out.println();
	    for (int i=0; i < a.getLength(); i++)
	    {
		out.print(i);
		for (String taxa: a.getTaxa())
		{
		    out.print("\t" + a.getSite(i).getRawCharacter(taxa));
		}
		out.println();
	    }
	    out.close();
	}
	catch (FileNotFoundException e)
	{
	    throw new OutputException(f.getAbsolutePath(),"Unable to write out duplication alignment",e);
	}
    }
}
