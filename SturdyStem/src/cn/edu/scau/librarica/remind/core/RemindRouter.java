package cn.edu.scau.librarica.remind.core;

import java.util.List;
import java.util.Hashtable;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.remind.dao.*;

public class RemindRouter
{
    private static int maxWaitSecond = Configurator.getInt("librarica.remind.maxwaitsecond", 120);

  // BLOCKING
    private static class Token
    {
        public Remind remind;

        public synchronized void waitRemind(Long millis)
            throws InterruptedException
        {
            this.wait(millis);

            return;
        }

        public synchronized void notifyRemind(Remind r)
        {
            this.remind = r;
            this.notifyAll();

            return;
        }

        private Token()
        {
            return;
        }
    }

    private static Hashtable<Long, Token> tokens = new Hashtable<Long, Token>();

  // ARRAVIAL
    public static void put(Remind r)
    {
        Token t = tokens.remove(r.getUser().getId());
        if (t != null)
        {
            // push to client
            t.notifyRemind(r);
        }
        else
        {
            // store to database

            HiberDao.saveOrUpdate(r);
        }
    }

  // DEPARTURE
    public static List<Remind> receive(Long uid, int waitSecond)
        throws IllegalArgumentException, InterruptedException
    {
        List<Remind> l = RemindMgr.retrieve(uid);
        if (l.size() > 0)
            return(l);

        // else
        long waitMillis = (waitSecond>maxWaitSecond ? maxWaitSecond : waitSecond)*1000L;

        if (waitMillis > 0L)
        {
            Token t = new Token();
            tokens.put(uid, t);
            try
            {
                t.waitRemind(waitMillis);
            }
            catch (InterruptedException ex)
            {
                tokens.remove(uid);
                throw(ex);
            }

            // woken
            if (t.remind != null)
                l.add(t.remind);
        }

        return(l);
    }
}
