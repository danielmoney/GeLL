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

package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads from a commented file ignoring any comments
 * @author Daniel Money
 * @version 1.0
 */
public class CommentedFileReader
{
    /**
     * Constructor
     * @param f The file to read from
     * @throws FileNotFoundException thrown if the file is not found
     */
    public CommentedFileReader(File f) throws FileNotFoundException
    {
	in = new BufferedReader(new FileReader(f));
	comment = 0;
    }

    /**
     * Reads a line from the file
     * @return The next line in the file that isn't completely commented out
     *      and with all comments stripped.
     * @throws IOException thrown if the underlying {@link BufferedReader}
     *      throws this exception
     */
    public String readLine() throws IOException
    {
	String line = "";
	while (line.equals(""))
	{
	    line = in.readLine();
	    if (line == null)
	    {
		return null;
	    }
	    Matcher m;
	    m = start.matcher(line);
	    if (m.matches())
	    {
		line = m.group(1);
		comment++;
	    }
	    m = end.matcher(line);
	    if (m.matches() && (comment > 0))
	    {
		line = m.group(1);
		comment--;
	    }
	    m = inline.matcher(line);
	    if (m.matches())
	    {
		line = m.group(1);
	    }
	    if (line.matches("^\\s+$"))
	    {
		line = "";
	    }
	    if (comment > 0)
	    {
		line = "";
	    }
	}
	return line;
    }

    /**
     * Close the file reader
     * @throws IOException thrown if the underlying BuffereReader throws this
     *      exception when it is closed.
     */
    public void close() throws IOException
    {
	in.close();
    }

    private static final Pattern start = Pattern.compile("(.*)/\\*.*");
    private static final Pattern end = Pattern.compile("(.*)\\*/.*");
    private static final Pattern inline = Pattern.compile("(.*)//.*");
    private int comment;
    private BufferedReader in;
}
