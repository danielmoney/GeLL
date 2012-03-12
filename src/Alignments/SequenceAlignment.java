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
 * Represents a sequence alignment
 * @author Daniel Money
 * @version 1.0
 */
public class SequenceAlignment extends Alignment
{
    /**
     * Creates a sequence alignment from a file.  File should be in a format similar
     * to Phylip.  The first non-blank line, which normally gives the number and length
     * of the sequence, is ignored.  Further non-blank lines represent each sequence, one
     * per line.  Anything from the start of the line to the first white space is
     * considered the taxa's name.  Anything after the first whitespace is the sequence.
     * Whitespace in the sequence is ignored.
     * 
     * @param f The input file
     * @throws InputException Thrown if there is a problem reading the file
     * @throws AlignmentException Thrown if there issomething wrong with the alignment, e.g.
     * different length sequences
     */
    public SequenceAlignment(File f) throws InputException, AlignmentException
    {
        this(f, new Ambiguous(new HashMap<String,Set<String>>()));
    }

    /**
     * Creates a sequence alignment from a file with ambiguouis data.  File should
     * be in {@link #SequenceAlignment(java.io.File)}
     * @param f The input file
     * @param ambig Desription o the ambiguous data
     * @throws InputException Thrown if there is a problem with the input file
     * @throws AlignmentException Thrown if there issomething wrong with the alignment, e.g.
     * different length sequences
     */
    public SequenceAlignment(File f, Ambiguous ambig) throws InputException, AlignmentException
    {
	// Create the hashmap to store the sequences (seq name =>  sequence)
        LinkedHashMap<String,String> seq = new LinkedHashMap<>();

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
            boolean paramLine = true;
            String line;
            int seqLength = -1;
            while ((line = in.readLine()) != null)
            {
                if (!line.equals("") && !paramLine)
                {
                    String[] parts = line.split("\\s+", 2);
                    
                    if (parts.length == 1)
                    {
                        throw new AlignmentException("Sequence " + parts[0], line, "No sequence given",null);
                    }
                    
                    String sequence = parts[1].replaceAll("\\s+", "");
                    if ((seqLength != -1) && (sequence.length() != seqLength))
                    {
                        throw new AlignmentException("Sequence " + parts[0], line, "Wrong sequence length",null);
                    }
                    else
                    {
                        seqLength = sequence.length();
                    }
                    seq.put(parts[0], sequence);
                    if (!taxa.contains(parts[0]))
                    {
                        taxa.add(parts[0]);
                    }
                    else
                    {
                        throw new AlignmentException("Taxa names", line, "Repeated taxa name",null);
                    }
                }
                if (!line.equals("") && paramLine)
                {
                    paramLine = false;
                }
            }
            in.close();
            
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
                data.add(new Site(col,ambig));
            }
        }
        catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(),"Not Applicable","Problem reading file",e);
	}
    }

    /**
     * Writes a alignment to a file in the format described in 
     * {@link #SequenceAlignment(java.io.File)}.
     * @param a The alignment to write to the file. Need not be a sequence alignment
     * @param f The file to write to
     * @throws OutputException Thrown if there is a problem creating the file
     */
    public static void writeFile(Alignment a, File f) throws OutputException
    {
        try
        {
            PrintStream out = new PrintStream(new FileOutputStream(f));

            out.println("     " + a.getNumber() + "    " + a.getLength());
            out.println();
            for (String taxa: a.getTaxa())
            {
                out.print(taxa + "     ");
                for (int j=0; j < a.getLength(); j++)
                {
                    out.print(a.getSite(j).getRawCharacter(taxa));
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
