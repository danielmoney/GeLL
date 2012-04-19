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
 * @version 1.1
 */
public class DuplicationAlignment extends Alignment
{
    /**
     * Creates a duplication alignment from a file.  File is tab seperated.
     * First row is a header file.  First field is ignored while subsequent fields
     * are the name of the species.  Each additional row represents a family.
     * The first field is an ID for the family while subsequent fields are the
     * size of the family in the appropiate species.  A family name of <code>*class*</code> is
     * assumed not to be a taxa but rather gfives the class of each site (which can be any
     * string).
     * @param f The input file
     * @throws InputException Thrown if there is a problem with the input file
     * @throws Alignments.AlignmentException Thrown if any family contains the wrong number
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
     * @throws Alignments.AlignmentException Thrown if any family contains the wrong number
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
                if (names[i].equals("*Class*"))
                {
                    hasClasses = true;
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
                String c = null;
                for (int j = 1; j < split.length; j++)
                {
                    if (!names[j].equals("*Class*"))
                    {
                        sizes.put(names[j], split[j]);
                    }
                    else
                    {
                        c = split[j];
                    }
                }
                data.add(new Site(sizes,ambig,c));
	    }
	    ain.close();
            
            if (hasClasses)
            {
                taxa.remove("*Class*");
            }
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
                if (a.hasClasses)
                {
                    out.print("\t*Class*");
                }
	    }
	    out.println();
	    for (int i=0; i < a.getLength(); i++)
	    {
		out.print(i);
		for (String taxa: a.getTaxa())
		{
		    out.print("\t" + a.getSite(i).getRawCharacter(taxa));
                    if (a.hasClasses)
                    {
                        out.print("\t" + a.getSite(i).getSiteClass());
                    }
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
