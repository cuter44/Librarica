package cn.edu.scau.librarica.msg.core;

import java.util.List;
import java.util.Hashtable;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.msg.dao.*;

public class MsgRouter
{
    private static int maxWaitSecond= Configurator.getInt("librarica.msg.maxwaitsecond");

  // BLOCKING
    private static class Token
    {
        public Msg msg;

        /** wait certain second
         */
        public synchronized void waitMsg(Long millis)
        {
            try
            {
                this.wait(millis);
            }
            catch (InterruptedException ex)
            {
                // TODO:log
                ex.printStackTrace();
            }
        }

        public synchronized void notifyMsg(Msg m)
        {
            this.msg = m;
            this.notifyAll();
        }

        private Token()
        {
            return;
        }
    }

    private static Hashtable<Long, Token> tokens = new Hashtable<Long, Token>();

  // ARRAVIAL
    /** 接受(从客户端)发出的消息
     * 消息会在接收者等待时被送往客户端, 或在无接收者时被存储到数据库
     * @warn risk of losing message if they were sent in too high frequency/network timeout, not tested.
     */
    public static void send(Msg m)
    {
        Token t = tokens.remove(m.getT().getId());
        // dispatch to client
        if (t != null)
        {
            t.notifyMsg(m);
            return;
        }

        // else
        // store to database
        HiberDao.save(m);
    }

  // DEPARTURE
    /** 接受服务器端缓存或转交的信息, 在没有消息时等待一定的秒数
     * @param waitSecond 等待的秒数, 实际等待的时间受服务器配置限制, 指定为 0 或负数则变为非阻塞.
     */
    public static List<Msg> receive(Long uid, int waitSecond)
        throws IllegalArgumentException
    {
        // from database
        List<Msg> l = MsgMgr.retrieve(uid);
        if (l.size() > 0)
            return(l);

        // else
        long waitMillis = (waitSecond>maxWaitSecond ? maxWaitSecond : waitSecond)*1000L;
        // wait someone send
        if (waitMillis > 0L)
        {
            Token t = new Token();
            tokens.put(uid, t);
            t.waitMsg(waitMillis);

            // waken
            if (t.msg != null)
                l.add(t.msg);
        }

        return(l);
    }

    /** 接受服务器端缓存或转交的信息, 在没有消息时等待一定的秒数
     */
    public static List<Msg> receive(Long uid)
    {
        return(
            receive(uid, 0)
        );
    }

}
