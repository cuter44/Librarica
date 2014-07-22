package cn.edu.scau.librarica.magnet.core;

import java.util.Hashtable;

import com.github.cuter44.util.geom.PointLong;
import com.github.cuter44.util.dao.*;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import net.sf.ehcache.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.remind.core.*;

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


    /** 取回要求的位置数据
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


    /** 取回要求的最新位置数据, 如果没有可用的数据则阻塞请求
     * "最新" 是指数据没有被此类任意 get 方法取得过.
     * @param tuple
     * @param waitSecond 大于等于0的整数, 表示阻塞的时间
     * @return base32-geohsah, or null if no matched.
     * @exception IllegalArgumentException 当 waitSecond 为负数时
     * @exception InterruptedExeption 当请求线程被中断时
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
                    HiberDao.begin();

                    // 发送提醒
                    PointLong t = (PointLong)e.getObjectKey();
                    RemindRouter.put(
                        RemindMgr.createTransient(t.y, "Magnet", t.x)
                    );

                    HiberDao.commit();

                    HiberDao.close();
                }
            }
        );

        return;
    }
}
