package cn.edu.scau.librarica.authorize.core;

import java.util.Map;
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
  // SINGLETON
    private static class Singleton
    {
        public static RSAKeyCache instance = new RSAKeyCache();
    }

  // BUCKET
    private static class Bucket
    {
        public Map<Long, PrivateKey> m;

      // CONSTRUCT
        public Bucket()
        {
            this.m = new HashMap<Long, PrivateKey>();
        }

    };

    private Bucket[] buckets;
    private int active;
    private long lastSwitched;
    private long interval;

    public static void put(Long id, PrivateKey key)
    {
        Singleton.instance.buckets[Singleton.instance.active].m.put(id, key);

        return;
    }

    /**
     */
    public static PrivateKey get(Long id)
    {
        gc();

        PrivateKey k = Singleton.instance.buckets[Singleton.instance.active].m.get(id);

        return(k!=null?k:Singleton.instance.buckets[1-Singleton.instance.active].m.get(id));
    }

    /** 检查上一次过期时间, 如果超过间隔则除去过期的key.
     * get() 包含这个方法
     */
    public static void gc()
    {
        if (System.currentTimeMillis() - Singleton.instance.lastSwitched
            > Singleton.instance.interval)
        {
            synchronized(Singleton.instance)
            {
                if (System.currentTimeMillis() - Singleton.instance.lastSwitched
                    > Singleton.instance.interval)
                {
                    Singleton.instance.buckets[1 - Singleton.instance.active].m.clear();
                    Singleton.instance.active = 1 - Singleton.instance.active;

                    Singleton.instance.lastSwitched = System.currentTimeMillis();
                }
            }
        }

        return;
    }

  // CONSTRUCT
    private RSAKeyCache()
    {
        this.buckets = new Bucket[2];
        this.buckets[0] = new Bucket();
        this.buckets[1] = new Bucket();
        this.active = 0;
        this.lastSwitched = System.currentTimeMillis();
        this.interval = Configurator.getLong("librarica.authorize.rsakeylifetime", 100000L);

        return;
    }
}
