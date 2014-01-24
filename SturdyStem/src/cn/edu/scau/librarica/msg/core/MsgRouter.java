package cn.edu.scau.librarica.msg.core;

import java.util.List;
import java.util.Hashtable;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.msg.dao.*;

public class MsgRouter
{
    private static int maxWaitSecond = Configurator.getInt("librarica.msg.maxwaitsecond", 120);

  // BLOCKING
    private static class Token
    {
        public Msg msg;

        /** wait certain second
         */
        public synchronized void waitMsg(Long millis)
            throws InterruptedException
        {
            this.wait(millis);

            return;
        }

        public synchronized void notifyMsg(Msg m)
        {
            this.msg = m;
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
    /** ����(�ӿͻ���)��������Ϣ
     * ��Ϣ���ڽ����ߵȴ�ʱ�������ͻ���, �����޽�����ʱ���洢�����ݿ�
     * @warn risk of losing message if they were sent in too high frequency/network timeout, not tested.
     */
    public static void send(Msg m)
    {
        Token t = tokens.remove(m.getT().getId());
        if (t != null)
        {
            // push to client
            t.notifyMsg(m);
        }
        else
        {
            // store to database
            HiberDao.save(m);
        }

        return;
    }

  // DEPARTURE
    /** ���ܷ������˻����ת������Ϣ, ��û����Ϣʱ�ȴ�һ��������
     * @param waitSecond �ȴ�������, ʵ�ʵȴ���ʱ���ܷ�������������, ָ��Ϊ 0 �������Ϊ������.
     */
    public static List<Msg> receive(Long uid, int waitSecond)
        throws IllegalArgumentException, InterruptedException
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
            try
            {
                t.waitMsg(waitMillis);
            }
            catch (InterruptedException ex)
            {
                tokens.remove(uid);
                throw(ex);
            }

            // waken
            if (t.msg != null)
                l.add(t.msg);
        }

        return(l);
    }

    /** ���ܷ������˻����ת������Ϣ, ��û����Ϣʱ�ȴ�һ��������
     */
    public static List<Msg> receive(Long uid)
        throws InterruptedException
    {
        return(
            receive(uid, 0)
        );
    }

}
