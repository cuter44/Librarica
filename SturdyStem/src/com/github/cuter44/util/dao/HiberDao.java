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
import org.hibernate.Transaction;
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
 * @version 1.2.3 build 20140110
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
     * @return ��ǰ�̵߳� Session ���� null (�����û����ʼ�߳�)
     */
    public static Session getCurrentThreadSession()
    {
        return(threadSession.get());
    }

  // TRANSACTION
    /**
     * ��Ӧ�����ķ��̰߳󶨷�װ, ��ͬ.
     * @exception HibernateException �� s �Ѿ��ر�ʱ.
     */
    public static Session begin(Session s)
    {
        if (!s.getTransaction().isActive())
            s.beginTransaction();

        return(s);
    }

    /**
     * ��װ���ݿ������ begin ����
     * <br />
     * ʹ���߳�Ψһ�����ݿ�����, �����ǰ�߳�δ�������ӻ������ѹر����Զ�����һ���µ�.
     * <br />
     * �����ǰ�Ѵ���session��δ����������, ������һ��������, ����Ѿ�����������, �������Ѿ����ڵ�����.
     * e.g. �����ظ�����begin(), ��ʵ�ʵ�������޴ӵ�һ��begin()��ʼ����,
     */
    public static Session begin()
    {
        if (!threadSession.get().isOpen())
            threadSession.set(newSession());

        return(begin(threadSession.get()));
    }

    public static Session flush(Session s)
    {
        s.flush();

        return(s);
    }

    /** ��װ flush ����
     */
    public static Session flush()
    {
        return(
            flush(threadSession.get())
        );
    }

    public static Session commit(Session s)
    {
        s.getTransaction()
            .commit();

        return(s);
    }
    /**
     * ��װ���ݿ������ commit ����
     */
    public static Session commit()
    {
        return(
            commit(threadSession.get())
        );
    }

    public static Session rollback(Session s)
    {
        s.getTransaction()
            .rollback();

        return(s);
    }
    /**
     * ��װ���ݿ������ rollback ����
     */
    public static Session rollback()
    {
        return(
            rollback(threadSession.get())
        );
    }

    /**
     * ֧���ظ��ر�
     */
    public static void close(Session s)
    {
        if (s.isOpen())
            s.close();

        return;
    }
    /**
     * ��װ���ݿ����ӵ� close ����
     * ֧���ظ��ر�
     */
    public static void close()
    {
        close(threadSession.get());

        return;
    }

  // DAO
    public static Object get(Session s, Class c, Serializable id)
    {
        return(
            s.get(c, id)
        );
    }
    /** ��������ѯ
     * @return Object ������Ӧ����, û���򷵻� null
     */
    public static Object get(Class c, Serializable id)
    {
        return(
            get(threadSession.get(), c, id)
        );
    }

    public static Object get(Session s, DetachedCriteria dc)
    {
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
    /** ��������Ψһѡȡ
     * @return ���������Ķ���, ����0�������һ�����������null
     */
    public static Object get(DetachedCriteria dc)
    {
        return(
            get(threadSession.get(), dc)
        );
    }

    public static void saveOrUpdate(Session s, Object o)
    {
        s.saveOrUpdate(o);

        return;
    }
    /** ������������ݶ���
     */
    public static void saveOrUpdate(Object o)
    {
        saveOrUpdate(threadSession.get(), o);

        return;
    }

    public static void save(Session s, Object o)
    {
        s.save(o);

        return;
    }
    /** �������ݶ���
     */
    public static void save(Object o)
    {
        save(threadSession.get(), o);

        return;
    }

    public static void update(Session s, Object o)
    {
        s.update(o);

        return;
    }
    /** �������ݶ���
     */
    public static void update(Object o)
    {
        update(threadSession.get(), o);

        return;
    }

    public static void delete(Session s, Object o)
    {
        s.delete(o);

        return;
    }
    /** ɾ�����ݶ���
     */
    public static void delete(Object o)
    {
        delete(threadSession.get(), o);

        return;
    }

    public static List search(Session s, DetachedCriteria dc, Integer start, Integer size)
    {
        Criteria c = dc.getExecutableCriteria(s);

        if (start != null)
            c.setFirstResult(start);
        if (size != null)
            c.setMaxResults(size);

        List li = c.list();

        return(li);
    }
    /** ����
     * <br />
     * @param dc ����
     * @param start �����ҳ��, �ӵ�#����¼��ʼ
     * @param limit �����ҳ��, ������෵��#����¼
     */
    public static List search(DetachedCriteria dc, Integer start, Integer size)
    {
        return(
            search(threadSession.get(), dc, start, size)
        );
    }
    public static List search(Session s, DetachedCriteria dc)
    {
        return(
            search(s, dc, null, null)
        );
    }
    /** search(dc, start, limit) �ļ򻯽ӿ�
     */
    public static List search(DetachedCriteria dc)
    {
        return(
            search(threadSession.get(), dc, null, null)
        );
    }

    public static Long count(Session s, DetachedCriteria dc)
    {
        Criteria c = dc.getExecutableCriteria(s)
            .setProjection(
                Projections.rowCount()
            );

        return((Long)c.uniqueResult());
    }
    /** ����������������
     */
    public static Long count(DetachedCriteria dc)
    {
        return(
            count(threadSession.get(), dc)
        );
    }

  // HQL
    public static Query createQuery(Session s, String hql)
    {
        return(
            s.createQuery(hql)
        );
    }
    public static Query createQuery(String hql)
    {
        return(
            createQuery(threadSession.get(), hql)
        );
    }
}
