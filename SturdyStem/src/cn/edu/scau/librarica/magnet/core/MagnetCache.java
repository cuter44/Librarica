package cn.edu.scau.librarica.magnet.core;

import java.util.Hashtable;

import com.github.cuter44.util.geom.PointLong;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import net.sf.ehcache.*;

import cn.edu.scau.librarica.util.conf.*;

public class MagnetCache
{
  // SINGLETON
    private static int maxWaitSecond = Configurator.getInt("librarica.magnet.maxwaitsecond");;
    private Cache cache;

    private static class Singleton
    {
        public static MagnetCache instance = new MagnetCache();
    }

  // BLOCK
    private static class Token
    {
        public String pos;

        /** wait certain second
         */
        public synchronized void waitPos(Long millis)
            throws InterruptedException
        {
            this.wait(millis);
            return;
        }

        public synchronized void notifyPos()
        {
            this.notifyAll();
            return;
        }

        private Token()
        {
            return;
        }
    }

    private Hashtable<PointLong, Token> waitingList = new Hashtable<PointLong, Token>();

    public static void put(PointLong tuple, String pos)
    {
        Singleton.instance.cache.put(new Element(tuple, pos));

        Token t = Singleton.instance.waitingList.get(tuple);
        if (t != null)
            t.notifyPos();

        return;
    }


    /** ȡ��Ҫ���λ������
     * @param tuple
     * @return base32-geohsah, or null if no matched.
     */
    public static String get(PointLong tuple)
    {
        Element e = Singleton.instance.cache.get(tuple);
        return(
            e!=null ? (String)e.getObjectValue() : null
        );
    }


    /** ȡ��Ҫ�������λ������, ���û�п��õ���������������
     * "����" ��ָ����û�б��������� get ����ȡ�ù�.
     * @param tuple
     * @param waitSecond ���ڵ���0������, ��ʾ������ʱ��
     * @return base32-geohsah, or null if no matched.
     * @exception IllegalArgumentException �� waitSecond Ϊ����ʱ
     * @exception InterruptedExeption �������̱߳��ж�ʱ
     */
    public static String getLatest(PointLong tuple, int waitSecond)
        throws IllegalArgumentException, InterruptedException
    {
        Element e = Singleton.instance.cache.get(tuple);
        if (e!=null && e.getHitCount()==1)
            return((String)e.getObjectValue());

        // else
        e = null;
        long waitMillis = (waitSecond>maxWaitSecond ? maxWaitSecond : waitSecond)*1000L;
        // wait oppoent send
        if (waitMillis > 0L)
        {
            Token t = new Token();
            Singleton.instance.waitingList.put(tuple, t);
            try {
                t.waitPos(waitMillis);
            }
            catch (InterruptedException ex) {
                throw(ex);
            }
            finally {
                Singleton.instance.waitingList.remove(tuple);
            }

            e = Singleton.instance.cache.get(tuple);
        }
        return(
            e!=null ? (String)e.getObjectValue() : null
        );

    }

    private MagnetCache()
    {
        this.cache = CacheManager.getInstance().getCache("MagnetCache");
        if (this.cache == null)
            throw(new RuntimeException("Get MagnetCache failed, ehcache.xml missing or incorrect."));

        Long ttl = Configurator.getLong("librarica.magnet.magnetttl");
        if (ttl != null)
        {
            this.cache.getCacheConfiguration()
                .setTimeToLiveSeconds(ttl);
        }

        // experimental
        this.cache.getCacheEventNotificationService().registerListener(
            new CacheEventListenerAdapter()
            {
                @Override
                public void notifyElementPut(Ehcache c, Element e)
                {
                    System.out.println("insert or update?");
                }
            }
        );

        return;
    }
}