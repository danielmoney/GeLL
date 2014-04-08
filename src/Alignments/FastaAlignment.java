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
package Alignments;

import Exceptions.InputException;
import Exceptions.OutputException;
import Exceptions.UnexpectedError;
import java.io.File;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Static classes for reading/writing sequence alignment in FASTA format
 * @author Daniel Money
 * @version 2.0
 */
public class FastaAlignment
{
    private FastaAlignment()
    {
        
    }
    
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
     * @return The alignment created from the file
     * @throws InputException Thrown if there is a problem with the input file
     * @throws Alignments.AlignmentException Thrown if there issomething wrong with the alignment, e.g.
     * different length sequences
     */
    public static Alignment fromFile(File f) throws InputException, AlignmentException
    {
        return fromFile(f, new Ambiguous(new HashMap<String,Set<String>>()));
    }

    /**
     * Creates a sequence alignment from a file with ambiguouis data.  File should
     * be in {@link #FastaAlignment(java.io.File)}
     * @param f The input file
     * @param ambig Desription of the ambiguous data
     * @return The alignment created from the file
     * @throws InputException Thrown if there is a problem with the input file
     * @throws Alignments.AlignmentException Thrown if there issomething wrong with the alignment, e.g.
     * different length sequences
     */
    public static Alignment fromFile(File f, Ambiguous ambig) throws InputException, AlignmentException
    {
	// Create the hashmap to store the sequences (seq name =>  sequence)
        LinkedHashMap<String,String> seq = new LinkedHashMap<>();
        String classLine = null;
        Set<String> taxa = new HashSet<>();
        List<Site> data = new ArrayList<>();
        boolean hasClasses = false;

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
                            if (checkSequence(name,sequence,seqLength,classLine,taxa))
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
                        name = parts[0].substring(1).trim();
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
                if (checkSequence(name,sequence,seqLength,classLine,taxa))
                {
                    taxa.add(name);
                    seq.put(name, sequence);
                }
                else
                {
                    hasClasses = true;
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
            return new Alignment(data);
        }
        catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
    }
    
    private static boolean checkSequence(String name, String sequence, int seqLength, String classLine,
            Set<String> taxa) throws AlignmentException
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
                    try
                    {
                        out.print(a.getSite(j).getRawCharacter(taxa));
                    }
                    catch (AlignmentException e)
                    {
                        //Should never reach here as we're looping over the known taxa hence...
                        throw new UnexpectedError(e);
                    }
                }
                out.println();
            }
            
            if (a.hasClasses())
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
