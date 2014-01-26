package cn.edu.scau.librarica.msg.core;

import java.util.List;
import java.util.Iterator;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.msg.dao.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

public class MsgMgr
{
    public static Msg get(Long id)
    {
        return(
            (Msg)HiberDao.get(Msg.class, id)
        );
    }

    public static Msg create(Long fromId, Long toId, String content)
    {
        Msg m = createTransient(fromId, toId, content);

        HiberDao.save(m);

        return(m);
    }

    /**
     * @warn 虽然名字带有 transient 但为了查找 User 依然需要数据库连接
     */
    public static Msg createTransient(Long fromId, Long toId, String content)
        throws EntityNotFoundException
    {
        User f = UserMgr.get(fromId);
        if (f == null)
            throw(new EntityNotFoundException("No such User, fromId:"+fromId));

        User t = UserMgr.get(toId);
        if (t == null)
            throw(new EntityNotFoundException("No such User, toId:"+toId));

        Msg m = new Msg(f, t, content);

        return(m);
    }

    //public static void update()

    public static void remove(Long id)
    {
        Msg m = get(id);

        if (m == null)
            throw(new EntityNotFoundException("No such Msg:"+id));

        HiberDao.delete(m);

        return;
    }

  // EX
    /** 取出在数据库中的消息并清除它们
     * @return 按时间升序的列表
     */
    public static List<Msg> retrieve(Long toUserId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(Msg.class)
            .addOrder(Order.asc("m"))
            .createCriteria("t")
            .add(Restrictions.eq("id", toUserId));

        List<Msg> l = (List<Msg>)HiberDao.search(dc);

        Iterator<Msg> i = l.iterator();
        while (i.hasNext())
            HiberDao.delete(i.next());

        return(l);
    }

}
