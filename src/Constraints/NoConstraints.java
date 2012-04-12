package Constraints;

import Alignments.Site;
import Trees.Tree;
import java.util.List;

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
    public NoConstraints(List<String> allStates)
    {
        def = new SiteConstraints(allStates);
    }
    
    public SiteConstraints getConstraints(Tree t, Site s)
    {
        return def;
    }
    
    private SiteConstraints def;
}
