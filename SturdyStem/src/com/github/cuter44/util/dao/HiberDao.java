package com.github.cuter44.util.dao;

/* base */
import java.io.Serializable;
import java.util.List;
/* hibernate */
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
/* log */
import org.apache.log4j.Logger;

/**
 * DAO ��� Hibernate ʵ��
 * <br />
 * �ù������װ���̰߳�ȫ�����ݿ����Ӽ���д����. ͬһ�����ӽ��еĶ�д����
 * ͬһ�����ݿ������ڽ���(�Ա������ݵ�һ����), �����ϲ������ⲻ��ô��.
 * <br />
 * �����ṩһ����д����
 * <br />
 <code>
    // Ҫ���ж�д������Ҫ�ȴ���һ������
    // ע���ظ�������ر�����ᵼ�²���Ԥ�ϵĽ��
    HiberDao.begin();

    // Ȼ��Ϳ��Խ�����ɾ��ĵ�ҵ��
    User u = (User)HiberDao.get(User.class, 1);
    // ҵ�����
    u.setName("foobar");
    // д��
    HiberDao.saveOrUpdate(u);

    // �ύ(commit)��ع�(rollback)����
    HiberDao.commit();
    // Ȼ����Խ�������һ������
    HiberDao.begin();
    u = HiberDao.get(User.class, 2);
    // ...���߹ر�����
    HiberDao.close();
 </code>
 * ������ô��...
 * <br />
 * �м�Ҫ�ύ���ҹرջỰ, ��������ڴ����, ��дʧ��ʲô�ĸŲ�����.
 * @version 1.2.2 build 20131212
 */
public class HiberDao
{
  // SESSION MANAGEMENT
    private SessionFactory sf = null;

    private static ThreadLocal<Session> threadSession = new ThreadLocal<Session>()
    {
        protected Session initialValue()
        {
            return(Singleton.instance.newSession());
        }
    };

    // �ݴ����ǳ��� Google ����ʦ֮�ֵ� Singleton д��...
    private static class Singleton
    {
        private static HiberDao instance = new HiberDao();
    }

    private HiberDao()
    {
        try
        {
            // Ϊ������ hibernate 4 ��д��...
            Configuration cfg = new Configuration()
                .configure();
            ServiceRegistry sr = new ServiceRegistryBuilder()
                .applySettings(
                    cfg.getProperties()
                ).buildServiceRegistry();
            this.sf = cfg.buildSessionFactory(sr);

            // hibernate 3 ������
            //this.sf = new Configuration()
                //.configure()
                //.buildSessionFactory();

            return;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    /**
     * ����һ���µ�Session
     * <br />
     * �����ⲿ������ø÷������������µĻỰ, ͨ���÷������ɵĻỰ���ᱻ
     * �󶨵��߳�Ҳ���ᱻ HiberDao ����, ��������Ҫ���и�������̺߳ͻỰ.
     */
    public static Session newSession()
    {
        return(Singleton.instance.sf.openSession());
    }

    /** ���ص�ǰ�߳�ʹ�õ�Session
     * ��Ҫ�� HiberDao.begin() ��ʹ��
     */
    public static Session getSession()
    {
        return(threadSession.get());
    }

  // TRANSACTION
    /**
     * ��װ���ݿ������ begin ����
     * <br />
     * ʹ���߳�Ψһ�����ݿ�����, �����ǰ�߳�δ�������ӻ������ѹر����Զ�����һ���µ�.
     * <br />
     * �����ǰ�Ѵ���session��δ����������, ������һ��������, ����Ѿ�����������, �������Ѿ����ڵ�����.
     * e.g. �����ظ�����begin(), ��ʵ�ʵ�������޴ӵ�һ��begin()��ʼ����,
     */
    public static void begin()
    {
        if (!threadSession.get().isOpen())
            threadSession.set(newSession());
        if (!threadSession.get().getTransaction().isActive())
            threadSession.get().beginTransaction();

        return;
    }

    /** ��װ flush ����
     */
    public static void flush()
    {
        threadSession.get()
            .flush();
        return;
    }


    /**
     * ��װ���ݿ������ commit ����
     */
    public static void commit()
    {
        threadSession.get()
            .getTransaction()
            .commit();

        return;
    }

    /**
     * ��װ���ݿ������ rollback ����
     */
    public static void rollback()
    {
        threadSession.get()
            .getTransaction()
            .rollback();

        return;
    }

    /**
     * ��װ���ݿ����ӵ� close ����
     */
    public static void close()
    {
        if (threadSession.get().isOpen())
            threadSession.get()
                .close();

        return;
    }

  // DAO
    /** ��������ѯ
     * @return Object ������Ӧ����, û���򷵻� null
     */
    public static Object get(Class c, Serializable id)
    {
        return(
            threadSession.get()
                .get(c, id)
        );
    }

    /** ��������Ψһѡȡ
     * @return ���������Ķ���, ����0�������һ�����������null
     */
    public static Object get(DetachedCriteria dc)
    {
        Session s = threadSession.get();
        Object o = null;

        try
        {
            o = dc.getExecutableCriteria(s)
                .uniqueResult();
        }
        catch (HibernateException ex)
        {
            ex.printStackTrace();
            Logger.getLogger("librarica.dao")
                .error(ex.toString());
        }

        return(o);

    }

    /** ������������ݶ���
     */
    public static void saveOrUpdate(Object o)
    {
        threadSession.get()
            .saveOrUpdate(o);

        return;
    }

    /** �������ݶ���
     */
    public static void save(Object o)
    {
        threadSession.get()
            .save(o);

        return;
    }

    /** �������ݶ���
     */
    public static void update(Object o)
    {
        threadSession.get()
            .update(o);

        return;
    }

    /** ɾ�����ݶ���
     */
    public static void delete(Object o)
    {
        threadSession.get()
            .delete(o);

        return;
    }

    /** ����
     * <br />
     * @param dc ����
     * @param start �����ҳ��, �ӵ�#����¼��ʼ
     * @param limit �����ҳ��, ������෵��#����¼
     */
    public static List search(DetachedCriteria dc, Integer start, Integer size)
    {
        Session s = threadSession.get();
        Criteria c = dc.getExecutableCriteria(s);

        if (start != null)
            c.setFirstResult(start);
        if (size != null)
            c.setMaxResults(size);

        List li = c.list();

        return(li);
    }
    /** search(dc, start, limit) �ļ򻯽ӿ�
     */
    public static List search(DetachedCriteria dc)
    {
        return(search(dc, null, null));
    }

    /** ����������������
     */
    public static Long count(DetachedCriteria dc)
    {
        Session s = threadSession.get();

        Criteria c = dc.getExecutableCriteria(s)
            .setProjection(
                Projections.rowCount()
            );

        return((Long)c.uniqueResult());
    }
  // HQL
    public static Query createQuery(String hql)
    {
        return(
            threadSession.get()
                .createQuery(hql)
        );
    }
}
