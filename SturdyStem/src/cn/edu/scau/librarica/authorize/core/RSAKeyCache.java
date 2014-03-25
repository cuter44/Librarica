package cn.edu.scau.librarica.authorize.core;

import java.security.PrivateKey;

import net.sf.ehcache.*;
import com.github.cuter44.util.crypto.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;

public class RSAKeyCache
{
    private Cache cache;

  // SINGLETON
    private static class Singleton
    {
        public static RSAKeyCache instance = new RSAKeyCache();
    }

    public static void put(Long id, PrivateKey key)
    {
        Singleton.instance.cache.put(new Element(id, key));

        return;
    }

    public static PrivateKey get(Long id)
    {
        Element e = Singleton.instance.cache.get(id);
        return(
            e!=null ? (PrivateKey)e.getObjectValue() : null
        );
    }

  // CONSTRUCT
    private RSAKeyCache()
    {
        this.cache = CacheManager.getInstance().getCache("RSAKeyCache");
        if (this.cache == null)
            throw(new RuntimeException("Get RSAKeyCache failed, ehcache.xml missing or incorrect."));

        Long ttl = Configurator.getLong("librarica.authorize.rsakeyttl");
        if (ttl != null)
        {
            this.cache.getCacheConfiguration()
                .setTimeToLiveSeconds(ttl);
        }

        return;
    }
}
