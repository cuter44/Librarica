package cn.edu.scau.librarica.authorize.core;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.cuter44.util.crypto.*;
import java.security.PrivateKey;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;

public class RSAKeyCache
{
    private static class Singleton
    {
        private static RSAKeyCache instance = new RSAKeyCache();
    }

    private HashMap<Long, PrivateKey> l1Cache;
    private HashMap<Long, PrivateKey> l2Cache;

    private ReentrantReadWriteLock rwLock;

    public static void put(Long id, PrivateKey key)
    {
        Singleton.instance.l1Cache.put(id, key);

        return;
    }

    public static PrivateKey get(Long id)
    {
        Singleton.instance.rwLock.readLock().lock();

        PrivateKey l1 = Singleton.instance.l1Cache.get(id);
        PrivateKey l2 = Singleton.instance.l2Cache.get(id);

        Singleton.instance.rwLock.readLock().unlock();

        return(l1!=null?l1:l2);
    }

    public static void expire()
    {
        Singleton.instance.rwLock.writeLock().lock();

        Singleton.instance.l2Cache = Singleton.instance.l1Cache;
        Singleton.instance.l1Cache = new HashMap<Long, PrivateKey>();

        Singleton.instance.rwLock.writeLock().unlock();

        return;
    }

    private RSAKeyCache()
    {
        this.l1Cache = new HashMap<Long, PrivateKey>();
        this.l2Cache = this.l1Cache;
        this.rwLock = new ReentrantReadWriteLock();

        long interval = Configurator.getLong("librarica.authorize.rsakeylifetime");
        new ExpireExecutor(interval);

        return;
    }

    /** 过期定时器
     */
    private class ExpireExecutor
        extends Timer
    {
        public ExpireExecutor(long interval)
            throws IllegalArgumentException
        {
            super(true);

            this.schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        RSAKeyCache.expire();
                    }
                },
                interval,
                interval
            );
        }
    }
}
