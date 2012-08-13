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

import Alignments.AlignmentException;
import Alignments.Site;
import Trees.Tree;
import Trees.TreeException;

/**
 * Interface for classes that constrain the values of internal nodes of a tree.
 * This can be used in both likelihood calculations and simulations to enforce
 * that an internal node is a certain value or one of a range of values.
 * @author Daniel Money
 * @version 1.0
 */
public interface Constrainer
{
    /**
     * Gets the consraints on an the external nodes of a site given the tree
     * and the state of the leafs.
     * @param t The tree
     * @param s The site (need not include data on internal leaves)
     * @return Constraints on that site
     * @throws TreeException Thrown if the constraints on that site can't be
     * calculated due to problems with the tree.
     * @throws AlignmentException Thrown if the tree and site have incomptiable taxa 
     */
    public SiteConstraints getConstraints(Tree t, Site s) throws TreeException, AlignmentException;
}