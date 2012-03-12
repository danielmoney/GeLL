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

/**
 * Represents the value of settings (including retutning default settings).  The
 * constructor checks the validity of the settings past to it.<br><br>
 * 
 * Settings can either be root level settings or within a group.  Only a single
 * level of groups is a allowed (i.e. a groups within a group is disallowed).<br><br>
 * 
 * {@link PossibleSettings} represents the list of settings that are possible.
 * Each setting can be optional or compulsory.  Groups can like wise be optional
 * or compulsory.  If a group is optional then any settings within it which are
 * comulsory are only compulsory when the group is present.<br><br>
 * 
 * {@link SetSettings} represents the current value of settings, e.g. from a
 * settings file or from a GUI.  SetSettings represents values set by a user
 * whereas this class represents values need by a program - it takes into
 * account default values and the constructor of this class ensures that all
 * needed settings are set and there aren't any unexpected settings (as this is
 * probably an input error).
 * @author Daniel Money
 * @version 1.0
 */
public class Settings
{
    /**
     * Creates an instance of this class
     * @param need Settings needed by the program
     * @param set Settings set by the user
     * @throws SettingException If a setting that needs to be set isn't or an
     * unexpected setting is set
     */
    public Settings(PossibleSettings need, SetSettings set) throws SettingException
    {
	//Check we don't have any settings we don't need
	for (String group: set.getGroups())
	{
	    for (String setting: set.getSet(group))
	    {
		if (!need.validSetting(group, setting))
		{
		    if (group == null)
		    {
			throw new SettingException("Setting " + setting + " is not needed");
		    }
		    else
		    {
			throw new SettingException("Setting " + group + "->" + setting + " is not needed");
		    }
		}
	    }
	}
	//Check we have all the settings we need
	for (String group: need.getGroups())
	{
	    if (need.neededGroup(group) && !set.getGroups().contains(group))
	    {
		throw new SettingException("Group " + group + " is needed");
	    }
	    if (set.getGroups().contains(group))
	    {
		for (String setting: need.getNeeded(group))
		{
		    if (!set.getSet(group).contains(setting))
		    {
			if (group == null)
			{
			    throw new SettingException("Setting " + setting + " is needed");
			}
			else
			{
			    throw new SettingException("Setting " + group + "->" + setting + " is needed");
			}
		    }
		}
	    }
	}

	this.need = need;
	this.set = set;
    }

    /**
     * Gets the value of a root level setting
     * @param setting The setting's name
     * @return The value of the setting
     * @throws SettingException Thrown if there is no default value (this
     * shouldn't occur due to the chacks at construction).
     */
    public String getSetting(String setting) throws SettingException
    {
	if (set.isSet(setting))
	{
	    return set.getSetting(setting);
	}
	else
	{
            return need.getDefault(setting);

	}
    }

    /**
     * Gets the value of a setting within a group
     * @param group The group's name
     * @param setting The settings name
     * @return The value of the setting
     * @throws SettingException Thrown if there is no default value (this
     * shouldn't occur due to the chacks at construction). 
     */
    public String getSetting(String group, String setting) throws SettingException
    {
	if (set.isSet(group,setting))
	{
	    return set.getSetting(group,setting);
	}
	else
	{
            return need.getDefault(group,setting);

	}
    }

    /**
     * Tests whether a group is present
     * @param group The group's name
     * @return Whether the group is present
     */
    public boolean hasGroup(String group)
    {
	return set.getGroups().contains(group);
    }

    private PossibleSettings need;
    private SetSettings set;
}
