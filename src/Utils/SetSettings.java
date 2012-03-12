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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the value of settings.  See {@link Settings} for further
 * details of how settings are structured.
 * @author Daniel Money
 * @version 1.0
 */
public class SetSettings
{
    /**
     * Default constructor
     */
    public SetSettings()
    {
	settings = new HashMap<>();
    }

    /**
     * Adds a new root level setting
     * @param setting The setting name
     * @param value The value to set the setting to
     * @return True if this setting was previously set to a value, false otherwise
     */
    public boolean addSetting(String setting, String value)
    {
	return addSetting(null, setting, value);
    }

    /**
     * Adds a new setting within a group
     * @param group The group name
     * @param setting The setting name
     * @param value The value to set the setting to
     * @return True if this setting was previously set to a value
     */
    public boolean addSetting(String group, String setting, String value)
    {
	if (settings.get(group) == null)
	{
	    settings.put(group, new HashMap<String, String>());
	}
	boolean replace = settings.get(group).containsKey(setting);
	settings.get(group).put(setting, value);
	return replace;
    }

    /**
     * Gets the value of a root level setting
     * @param setting The setting's name
     * @return The value the setting is set to
     */
    public String getSetting(String setting)
    {
	return getSetting(null, setting);
    }

    /**
     * Gets the value of a setting within a group
     * @param group The group's name
     * @param setting The setting's name
     * @return The value the setting is set to
     */
    public String getSetting(String group, String setting)
    {
	if (settings.get(group) == null)
	{
	    return null;
	}
	else
	{
	    return settings.get(group).get(setting);
	}
    }

    /**
     * Gets the set of all groups that have had a setting set within them
     * @return A set of groups
     */
    public Set<String> getGroups()
    {
	return settings.keySet();
    }

    /**
     * Gets the set of root level settings that have been set
     * @return A set of root level settings
     */
    public Set<String> getSet()
    {
	return getSet(null);
    }

    /**
     * Gets the set of settings that have been set within a group
     * @param group The group's name
     * @return A set of settings
     */
    public Set<String> getSet(String group)
    {
	if (settings.get(group) == null)
	{
	    return null;
	}
	else
	{
	    return settings.get(group).keySet();
	}
    }

    /**
     * Tests whether a root level setting has been set
     * @param setting The setting's name
     * @return Whether the setting has been set
     */
    public boolean isSet(String setting)
    {
	return isSet(null,setting);
    }

    /**
     * Tests whether a setting within a groups has been set
     * @param group The group's name
     * @param setting The setting's anme
     * @return Whether the setting has been set
     */
    public boolean isSet(String group, String setting)
    {
	if (settings.containsKey(group))
	{
	    return settings.get(group).containsKey(setting);
	}
	else
	{
	    return false;
	}
    }

    public String toString()
    {
	StringBuilder s = new StringBuilder();
	for (String group: settings.keySet())
	{
	    String gs;
	    if (group == null)
	    {
		gs = "(none)";
	    }
	    else
	    {
		gs = group;
	    }
	    for (String setting: settings.get(group).keySet())
	    {
		s.append(gs);
		s.append("->");
		s.append(setting);
		s.append("\t");
		s.append(settings.get(group).get(setting));
		s.append("\n");
	    }
	}
	return s.toString();
    }

    private HashMap<String, HashMap<String, String>> settings;

    /**
     * Reads settings from a file and returns an instance of this class.
     * Format of the setting file is:
     * <ol>
     * <li> The group the following settings will be in is changed by a line 
     * with [group name] on it.</li>
     * <li> Any setting before the first group line is treated as a root setting</li>
     * <li> Settings are given one per line with the setting name followed by a tab
     * followed by the settings value</li>
     * <li> Anything following "//" and on the same line is consider a comment</li>
     * <li> Blank and comment only lines are ignored</li>
     * </ol>
     * @param f The file name
     * @return The set settings
     * @throws FileNotFoundException If the settings file is not found
     * @throws IOException If there is a problem reading from the settings file
     * @throws SettingException If there is a line in an unexpected format
     */
    public static SetSettings fromFile(File f) throws FileNotFoundException, IOException, SettingException
    {
	SetSettings s = new SetSettings();
	CommentedFileReader in = new CommentedFileReader(f);

	String group = null;
	String line;
	while ((line = in.readLine()) != null)
	{
	    boolean good = false;
	    Matcher m;
	    m = groupRE.matcher(line);
	    if (m.find())
	    {
		group = m.group(1);
		good = true;
	    }
	    m = settingRE.matcher(line);
	    if (m.find())
	    {
		s.addSetting(group, m.group(1), m.group(2));
		good = true;
	    }
	    if (!good)
	    {
		throw new SettingException("\"" + line + "\" is not a valid group or setting");
	    }
	}
	in.close();
	return s;
    }

    /**
     * Returns settings from a file and the command line and returns an instance
     * of this class.  See {@link #fromFile(java.io.File)} for file format.  If
     * the value of a setting contains $i (where i is an integer) then it is 
     * replaced by the ith element of c.
     * @param f The settings file name
     * @param c An array of command line inputs
     * @return The set settings
     * @throws FileNotFoundException If the settings file is not found
     * @throws IOException If there is a problem reading from the settings file
     * @throws SettingException If there is a line in an unexpected format
     */
    public static SetSettings fromFileAndCommandLine(File f, String[] c) throws FileNotFoundException, IOException, SettingException
    {
	SetSettings s = new SetSettings();
	CommentedFileReader in = new CommentedFileReader(f);

	String group = null;
	String line;
	while ((line = in.readLine()) != null)
	{
	    boolean good = false;
	    Matcher m;
	    m = groupRE.matcher(line);
	    if (m.find())
	    {
		group = m.group(1);
		good = true;
	    }
	    m = settingRE.matcher(line);
	    if (m.find())
	    {
		Matcher m2 = clRE.matcher(m.group(2));
		String set = m.group(2);
		while (m2.find())
		{
		    int id = Integer.parseInt(m2.group(1));
		    if (id <= c.length)
		    {
			set = set.replace("$" + m2.group(1), c[id]);
		    }
		    else
		    {
			throw new SettingException("Command line option " + id + " not set");
		    }
		}
		s.addSetting(group, m.group(1), set);
		good = true;
	    }
	    if (!good)
	    {
		throw new SettingException("\"" + line + "\" is not a valid group or setting");
	    }
	}
	in.close();
	return s;
    }

    private static final Pattern groupRE = Pattern.compile("^\\[(.*)\\]$");
    private static final Pattern settingRE = Pattern.compile("\\s*(.*?)\\t+(.*)");
    private static final Pattern clRE = Pattern.compile("\\$(\\d)");
}
