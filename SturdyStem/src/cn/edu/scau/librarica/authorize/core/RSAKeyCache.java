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
  // SINGLETON
    private static class Singleton
    {
        public static RSAKeyCache instance = new RSAKeyCache();
    }

    private static class KeyPool extends HashMap<Long, PrivateKey>
    {
        public KeyPool()
        {
            super();
        }

        public PrivateKey put(Long id, PrivateKey key)
        {
            return(super.put(id, key));
        }

        public PrivateKey get(Long id)
        {
            return(super.get(id));
        }
    };

    private KeyPool[] cache;
    private int active;
    private long timestamp;
    private long interval;

    public static void put(Long id, PrivateKey key)
    {
        Singleton.instance.cache[Singleton.instance.active].put(id, key);

        return;
    }

    /**
     * @warning 包含 synchronized 调用所以性能有待观察
     */
    public static PrivateKey get(Long id)
    {
        expire();

        PrivateKey k = Singleton.instance.cache[Singleton.instance.active].get(id);

        return(k!=null?k:Singleton.instance.cache[1-Singleton.instance.active].get(id));
    }

    /** 检查并消去过期的 key
     * get() 包含这个方法
     */
    public static synchronized void expire()
    {
        long elapsed = System.currentTimeMillis() - Singleton.instance.timestamp;

        if (elapsed > Singleton.instance.interval)
        {
            Singleton.instance.cache[1 - Singleton.instance.active].clear();
            Singleton.instance.active = 1 - Singleton.instance.active;

            Singleton.instance.timestamp = System.currentTimeMillis();
        }
    }

  // CONSTRUCT
    private RSAKeyCache()
    {
        this.cache = new KeyPool[2];
        this.cache[0] = new KeyPool();
        this.cache[1] = new KeyPool();
        this.active = 0;
        this.timestamp = System.currentTimeMillis();
        this.interval = Configurator.getLong("librarica.authorize.rsakeylifetime", 100000L);

        return;
    }
}
