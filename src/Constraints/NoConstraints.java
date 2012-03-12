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

package Constraints;

import Alignments.Site;
import Trees.Tree;
import java.util.Set;

/**
 * Simple implementation of {@link Constrainer} that imposes no constraints
 * @author Daniel Money
 */
public class NoConstraints implements Constrainer
{
    /**
     * Constructor
     * @param allStates The set of all possible states.  Needed as the internal
     * nodes will be constrained to these values.  As these are all the posisble
     * values there are no constraints.
     */
    public NoConstraints(Set<String> allStates)
    {
        def = new SiteConstraints(allStates);
    }
    
    public SiteConstraints getConstraints(Tree t, Site s)
    {
        return def;
    }
    
    private SiteConstraints def;
}
