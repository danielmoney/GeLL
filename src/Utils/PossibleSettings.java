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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Maintains a list of possible settings.  See {@link Settings} for further
 * details of how settings are structured.
 * @author Daniel Money
 * @version 1.3
 */
public class PossibleSettings
{
    /**
     * Default constructor
     */
    public PossibleSettings()
    {
	groups = new HashMap<>();
        groups.put(null,false);
	settings = new HashMap<>();
	defaults = new HashMap<>();
	settings.put(null,new HashMap<String,Boolean>());
	defaults.put(null,new HashMap<String,String>());
    }
    
    /**
     * Adds a new setting group
     * @param name The name of the group
     * @param needed Whether the group is required
     * @throws SettingException Thrown if a group with that name already exists 
     */
    public void addGroup(String name, boolean needed) throws SettingException
    {
	if (groups.containsKey(name))
	{
	    throw new SettingException("A group with that name already exists");
	}
	else
	{
	    groups.put(name, needed);
	    settings.put(name, new HashMap<String,Boolean>());
	    defaults.put(name, new HashMap<String,String>());
	}
    }
    
    /**
     * Add a required setting at the root level (i.e. not within a group)
     * @param name The name of the setting
     * @throws SettingException Thrown if a setting with that name already exists 
     */
    public void addNeededSetting(String name) throws SettingException
    {
	addNeededSetting(null,name);
    }
    
    /**
     * Add a required setting within a group
     * @param group The group to add the setting to
     * @param name The name of the setting
     * @throws SettingException Thrown if that group / name combination already exists 
     */
    public void addNeededSetting(String group, String name) throws SettingException
    {
	if (!groups.containsKey(group))
	{
	    throw new SettingException("No group with that name exists");
	}
	else
	{
	    if (settings.get(group).containsKey(name))
	    {
		throw new SettingException("A setting with that name already exists");
	    }
	    else
	    {
		settings.get(group).put(name,true);
	    }
	}
    }
    
    /**
     * Add an optional setting at the root level.
     * @param name The name fo the setting
     * @param def The default value for the setting
     * @throws SettingException Thrown if a setting with that name already exists
     */
    public void addOptionalSetting(String name, String def) throws SettingException
    {
        addOptionalSetting(null,name,def);
    }
    
    /**
     * Add an optional setting to a group.
     * @param group The group the setting is to be added to
     * @param name The name fo the setting
     * @param def The default value for the setting
     * @throws SettingException Thrown if that group / name combination already exists 
     */
    public void addOptionalSetting(String group, String name, String def) throws SettingException
    {
	if (!groups.containsKey(group))
	{
	    throw new SettingException("No group with that name exists");
	}
	else
	{
	    if (settings.get(group).containsKey(name))
	    {
		throw new SettingException("A setting with that name already exists");
	    }
	    else
	    {
		settings.get(group).put(name, false);
		defaults.get(group).put(name, def);
	    }
	}	
    }
    
    /**
     * Tests whether a setting name is valid at the root level
     * @param setting The setting name
     * @return Whether the setting is a valid setting
     */
    public boolean validSetting(String setting)
    {
        return validSetting(null,setting);
    }

    /**
     * Tests whether a setting name is valid
     * @param group The group the setting is contained in
     * @param setting The setting name
     * @return Whether the setting is a valid setting
     */
    public boolean validSetting(String group, String setting)
    {
	if (groups.containsKey(group))
	{
	    return settings.get(group).containsKey(setting);
	}
	else
	{
	    return false;
	}
    }

    Set<String> getGroups()
    {
	return groups.keySet();
    }

    boolean neededGroup(String group)
    {
	return groups.get(group);
    }

    Set<String> getNeeded(String group)
    {
	HashSet<String> needed = new HashSet<>();

	for (String setting: settings.get(group).keySet())
	{
	    if (settings.get(group).get(setting))
	    {
		needed.add(setting);
	    }
	}
	return needed;
    }

    String getDefault(String setting) throws SettingException
    {
	String d = defaults.get(null).get(setting);
	if (d == null)
	{
	    throw new SettingException("No default value for " + setting);
	}
	else
	{
	    return d;
	}
    }
    
    String getDefault(String group, String setting) throws SettingException
    {
	if (!settings.containsKey(group))
	{
	    throw new SettingException("Group " + group + " does not exist");
	}
	else
	{
	    return defaults.get(group).get(setting);
	}
    }

    private HashMap<String,Boolean> groups;
    private HashMap<String,HashMap<String,Boolean>> settings;
    private HashMap<String,HashMap<String,String>> defaults;
}