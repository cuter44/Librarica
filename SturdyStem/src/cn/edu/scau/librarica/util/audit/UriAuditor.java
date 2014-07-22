package cn.edu.scau.librarica.util.audit;

import java.util.Map;
import java.util.Hashtable;

public class UriAuditor
{
  // BUCKET
    private static class Bucket
    {
        public Hashtable<String, Integer> m;

        public void inc(String uri)
        {
            Integer v = this.m.get(uri);
            if (v == null)
                this.m.put(uri, 1);
            else
                this.m.put(uri, v+1);
        }

        public Bucket()
        {
            this.m = new Hashtable<String, Integer>();
        }
    }

  // SINGLETON
    private static class Singleton
    {
        public static UriAuditor instance = new UriAuditor();
    }

    private long lastSwitched;
    private Bucket[] buckets;
    private int active;

    public static void inc(String uri)
    {
        gc();

        Singleton.instance.buckets[Singleton.instance.active].inc("");
        Singleton.instance.buckets[Singleton.instance.active].inc(uri);
    }

    public static Hashtable<String, Integer> getStatistics()
    {
        return(
            (Hashtable<String, Integer>)Singleton.instance.buckets[1-Singleton.instance.active].m.clone()
        );
    }

    public static void gc()
    {
        if (System.currentTimeMillis() - Singleton.instance.lastSwitched
            > 60000L)
        {
            synchronized(Singleton.instance)
            {
                if (System.currentTimeMillis() - Singleton.instance.lastSwitched
                    > 60000L)
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
    private UriAuditor()
    {
        this.buckets = new Bucket[2];
        this.buckets[0] = new Bucket();
        this.buckets[1] = new Bucket();
        this.active = 0;
        this.lastSwitched = System.currentTimeMillis();

        return;
    }

}
