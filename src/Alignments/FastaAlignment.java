package Alignments;

import Exceptions.InputException;
import Exceptions.OutputException;
import java.io.File;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents a sequence alignment in FASTA format
 * @author Daniel Money
 * @version 1.2
 */
public class FastaAlignment extends Alignment
{
    /**
     * Creates a sequence alignment from a file.  File should be in Fasta format
     * File should consist of a line beginningb with a ">" followed by the taxa name -
     * anything after a "|" is ignored.  The following lines to the next line
     * beginning with ">" are tjhe sequence associated with that taxa.  Blank lines
     * are ignored.  Comments, i.e. lines beginning with ";" are NOT supported.
     * A taxa name of <code>*class*</code> is
     * assumed not to be a taxa but rather gives the class of each site (which can be any
     * single character).
     * @param f The input file
     * @throws InputException Thrown if there is a problem with the input file
     * @throws Alignments.AlignmentException Thrown if there issomething wrong with the alignment, e.g.
     * different length sequences
     */
    public FastaAlignment(File f) throws InputException, AlignmentException
    {
        this(f, new Ambiguous(new HashMap<String,Set<String>>()));
    }

    /**
     * Creates a sequence alignment from a file with ambiguouis data.  File should
     * be in {@link #FastaAlignment(java.io.File)}
     * @param f The input file
     * @param ambig Desription of the ambiguous data
     * @throws InputException Thrown if there is a problem with the input file
     * @throws Alignments.AlignmentException Thrown if there issomething wrong with the alignment, e.g.
     * different length sequences
     */
    public FastaAlignment(File f, Ambiguous ambig) throws InputException, AlignmentException
    {
	// Create the hashmap to store the sequences (seq name =>  sequence)
        LinkedHashMap<String,String> seq = new LinkedHashMap<>();
        String classLine = null;

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
            String line;
            int seqLength = -1;
            String sequence = "";
            String name = null;
            while ((line = in.readLine()) != null)
            {
                if (!line.equals(""))
                {
                    if (line.startsWith(">"))
                    {
                        if (name != null)
                        {
                            if (checkSequence(name,sequence,seqLength,classLine))
                            {
                                seq.put(name, sequence);
                                seqLength = sequence.length();
                            }
                            else
                            {
                                classLine = sequence;
                                seqLength = sequence.length();
                            }
                        }
                        sequence = "";
                        String[] parts = line.split("\\|");
                        name = parts[0].substring(1);
                    }
                    else
                    {
                        sequence = sequence + line;
                    }               
                }
            }
            in.close();
            
            if (name != null)
            {
                if (checkSequence(name,sequence,seqLength,classLine))
                {
                    seq.put(name, sequence);
                }
                else
                {
                    classLine = sequence;
                }
            }
            
            if (taxa.size() < 2)
            {
                throw new AlignmentException("Taxa names", "N/A", "One or fewer taxa given",null);
            }
            
            for (int i = 0; i < seqLength; i++)
            {
                LinkedHashMap<String,String> col = new LinkedHashMap<>();
                for (Entry<String,String> e: seq.entrySet())
                {
                    col.put(e.getKey(),e.getValue().substring(i, i+1));
                }
                String c = null;
                if (classLine != null)
                {
                    c = classLine.substring(i, i+1);
                }
                data.add(new Site(col,ambig,c));
            }
        }
        catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
    }
    
    private boolean checkSequence(String name, String sequence, int seqLength, String classLine) throws AlignmentException
    {
        if (sequence.length() == 0)
        {
            throw new AlignmentException("Sequence " + name, "N/A", "No sequence given",null);
        }
        if ((seqLength != -1) && (sequence.length() != seqLength))
        {
            throw new AlignmentException("Sequence " + name, "N/A", "Wrong sequence length",null);
        }
        else
        {
            seqLength = sequence.length();
        }
        if (!name.equals("*Class*"))
        {
            if (!taxa.contains(name))
            {
                taxa.add(name);
                return true;
            }
            else
            {
                throw new AlignmentException("Taxa names", "N/A", "Repeated taxa name",null);
            }
        }
        else
        {
            if (classLine == null)
            {
                hasClasses = true;
                return false;
            }
            else
            {
                throw new AlignmentException("Taxa names", "N/A", "Site class defined more than once",null);
            }
        }
    }

    /**
     * Writes a alignment to a file in the format described in 
     * {@link #FastaAlignment(java.io.File)}.
     * @param a The alignment to write to the file. Need not be a sequence alignment
     * @param f The file to write to
     * @throws OutputException Thrown if there is a problem creating the file
     */
    public static void writeFile(Alignment a, File f) throws OutputException
    {
        try
        {
            PrintStream out = new PrintStream(new FileOutputStream(f));

            for (String taxa: a.getTaxa())
            {
                out.print(">" + taxa);
                for (int j=0; j < a.getLength(); j++)
                {
                    if (j % 70 == 0)
                    {
                        out.println();
                    }
                    out.print(a.getSite(j).getRawCharacter(taxa));
                }
                out.println();
            }
            
            if (a.hasClasses)
            {
                out.println();
                out.print(">*Class*     ");
                for (int j=0; j < a.getLength(); j++)
                {
                    if (j % 70 == 0)
                    {
                        out.println();
                    }
                    out.print(a.getSite(j).getSiteClass());
                }
                out.println();
            }
            
            out.close();
        }
	catch (FileNotFoundException e)
	{
	    throw new OutputException(f.getAbsolutePath(),"Unable to write out sequence alignment",e);
	}
    }
}
