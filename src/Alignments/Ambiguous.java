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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores details of ambiguous characters
 * @author Daniel Money
 * @version 2.0
 */
public class Ambiguous implements Serializable
{
    /**
     * Default constructor for use when there are no ambiguous characters
     */
    public Ambiguous()
    {
        ambig = new HashMap<>();
    }
    
    /**
     * Constructor for use when there are ambiguous characters
     * @param ambig A map from the ambiguous characters to the set of characters
     *      the ambiguous character could represent.  Recursive definitions are
     *      not allowed.
     */
    public Ambiguous(Map<String,Set<String>> ambig)
    {
	this.ambig = ambig;
    }
    
    /**
     * Gets the characters an ambiguous character could represent
     * @param c The ambiguous character
     * @return The set of characters that could be represented by it
     */
    public Set<String> getPossible(String c)
    {
        if (ambig.containsKey(c))
	{
	    return ambig.get(c);
	}
	else
	{
	    Set<String> r = new HashSet<>();
	    r.add(c);
	    return r;
	}
    }

    private Map<String,Set<String>> ambig;
    
    /**
     * Reads in ambiguous data information from a file.  File should be a tab
     * delimited file with one ambiguous character per line.  The first field
     * on each line is the ambiguous character while also subsequent field
     * represents a character that could be represented by it
     * @param f File object for the input file
     * @return An Ambiguous object
     * @throws InputException
     */
    public static Ambiguous fromFile(File f) throws InputException
    {
	String line = null;
	try
	{
	    HashMap<String, Set<String>> ambig = new HashMap<>();

	    BufferedReader in = new BufferedReader(new FileReader(f));

	    while ((line = in.readLine())!= null)
	    {
		String[] parts = line.split("\t");
		ambig.put(parts[0], new HashSet<>(Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length))));
	    }

	    return new Ambiguous(ambig);
	}
	catch (FileNotFoundException e)
	{
	    throw new InputException(f.getAbsolutePath(), "Not Applicable", "File does not exist", e);
	}
	catch (IOException e)
	{
	    throw new InputException(f.getAbsolutePath(), "Not Applicable", "Problem reading file", e);
	}
    }
    
    private static final long serialVersionUID = 1;
}
