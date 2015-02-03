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

package ModelTest;

import Alignments.Alignment;

/**
 * Interface for an adapter.  See Whelan et al 2015 for more on adapters
 * @author Daniel Money
 * @version 2.0
 */
public interface Adapter
{

    /**
     * Return the likelihood of the adapter given the distinct alignment
     * @param distinct The distinct alignment
     * @return The likelihood of the adapter
     */
    public double likelihood(Alignment distinct);

    /**
     * Return the number of parameters associated with the adapter
     * @return The number of parameters
     */
    public int numberParameters();
}
