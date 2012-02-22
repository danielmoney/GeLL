package Constraints;

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
     */
    public SiteConstraints getConstraints(Tree t, Site s) throws TreeException;
}