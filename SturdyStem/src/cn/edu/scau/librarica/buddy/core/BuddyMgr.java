package cn.edu.scau.librarica.buddy.core;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.DetachedCriteria;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.buddy.dao.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

public class BuddyMgr
{
    /**
     * @param Buddy 的 id
     */
    public static Buddy get(Long id)
    {
        return(
            (Buddy)HiberDao.get(Buddy.class, id)
        );
    }

    /**
     * @param meId 己方的 uid
     * @param opId 对方的 uid
     */
    public static Buddy get(Long meId, Long opId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(Buddy.class);
        dc.createCriteria("me")
            .add(Restrictions.eq("id", meId));
        dc.createCriteria("op")
            .add(Restrictions.eq("id", opId));

        return(
            (Buddy)HiberDao.get(dc)
        );
    }

    /**
     * @exception EntityNotFoundException when relation is already existed, whether like or hate
     */
    public static Buddy create(Long meId, Long opId)
        throws EntityDuplicatedException, EntityNotFoundException
    {
        if (get(meId, opId) != null)
            throw(new EntityDuplicatedException("Buddy already existed:"+meId+"->"+opId));

        User me = UserMgr.get(meId);
        if (me == null)
            throw(new EntityNotFoundException("No such User:"+meId));

        User op = UserMgr.get(opId);
        if (op == null)
            throw(new EntityNotFoundException("No such User:"+opId));

        Buddy b = new Buddy(me, op);

        HiberDao.save(b);

        return(b);
    }

    /**
     * @param Buddy 的 id
     */
    public static void remove(Long id)
        throws EntityNotFoundException
    {
        Buddy b = get(id);
        if (b == null)
            throw(new EntityNotFoundException("No such Buddy:"+id));

        HiberDao.delete(b);

        return;
    }

    /**
     * @param meId 己方的 uid
     * @param opId 对方的 uid
     */
    public static void remove(Long meId, Long opId)
        throws EntityNotFoundException
    {
        Buddy b = get(meId, opId);
        if (b == null)
            throw(new EntityNotFoundException("No such Buddy:"+meId+"->"+opId));

        HiberDao.delete(b);

        return;
    }

  // EX
    public static void setLike(Long meId, Long opId)
        throws EntityNotFoundException
    {
        Buddy b = get(meId, opId);
        if (b == null)
            b = create(meId, opId);

        b.setR(Buddy.LIKE);
        HiberDao.update(b);

        return;
    }

    public static void setHate(Long meId, Long opId)
        throws EntityNotFoundException
    {
        Buddy b = get(meId, opId);
        if (b == null)
            b = create(meId, opId);

        b.setR(Buddy.HATE);
        HiberDao.update(b);

        return;
    }

    public static void setNull(Long meId, Long opId)
    {
        Buddy b = get(meId, opId);
        if (b != null)
            HiberDao.delete(b);

        return;
    }

    public static boolean isHated(Long meId, Long opId)
    {
        Buddy b = get(opId, meId);

        if (b != null && Buddy.HATE.equals(b.getR()))
            return(false);
        // else
        return(true);
    }
}
