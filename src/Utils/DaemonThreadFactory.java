package Utils;

import java.util.concurrent.ThreadFactory;

/**
 * A thread factory that creates daemon threads
 * @author Daniel Money
 * @version 1.2
 */
public class DaemonThreadFactory implements ThreadFactory
{
    public Thread newThread(Runnable r)
    {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }
}
