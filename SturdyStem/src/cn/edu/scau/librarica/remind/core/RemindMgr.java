package cn.edu.scau.librarica.remind.core;

import java.util.List;
import java.util.Iterator;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.remind.dao.*;

public class RemindMgr
{
    public static Remind get(Long id)
    {
        return(
            (Remind)HiberDao.get(Remind.class, id)
        );
    }

    private static Remind get(Long userId, String t, String v)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(Remind.class)
            .add(Restrictions.eq("t", t))
            .add(Restrictions.eq("v", v))
            .createCriteria("user")
            .add(Restrictions.eq("id", userId));

        return(
            (Remind)HiberDao.get(dc)
        );
    }

    /**
     * ������ݿ��д����غ϶���, �򷵻��غ϶���.
     */
    public static Remind createTransient(User u, String t, String v)
    {
        // ������
        Remind r = get(u.getId(), t, v);
        if (r != null)
            return(r);

        // else
        r = new Remind(u, t, v);

        return(r);
    }

    public static Remind createTransient(User u, String t, Long v)
    {
        return(
            createTransient(
                u,
                t,
                v!=null?v.toString():(String)null
            )
        );
    }

    public static Remind createTransient(Long userId, String t, String v)
    {
        User u = UserMgr.get(userId);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+userId));

        return(
            createTransient(u, t, v)
        );
    }

    public static Remind createTransient(Long userId, String t, Long v)
    {
        return(
            createTransient(
                userId,
                t,
                v!=null?v.toString():(String)null
            )
        );
    }

    //public static void create()

    public static void remove(Long id)
    {
        Remind r = get(id);

        if (r == null)
            throw(new EntityNotFoundException("No suck Remind:"+id));

        HiberDao.delete(r);

        return;
    }

  // EX
    /** ȡ�ز��������ݿ��д� User ��֪ͨ
     */
    public static List<Remind> retrieve(Long uid)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(Remind.class)
            .createCriteria("user")
            .add(Restrictions.eq("id", uid));

        List<Remind> l = (List<Remind>)HiberDao.search(dc);

        Iterator<Remind> i = l.iterator();
        while (i.hasNext())
            HiberDao.delete(i.next());

        return(l);
    }
}
