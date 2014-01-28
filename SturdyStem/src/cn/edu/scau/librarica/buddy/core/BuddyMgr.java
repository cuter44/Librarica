package cn.edu.scau.librarica.buddy.core;

import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.DetachedCriteria;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.buddy.dao.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

public class BuddyMgr
{
    public static Buddy get(Long id)
    {
        return(
            (Buddy)HiberDao.get(Buddy.class, id)
        );
    }

    public static Buddy get(Long meId, Long opId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(Buddy.class);
        dc.createCriteria("o")
            .add(Restrictions.eq("id", meId));
        dc.createCriteria("s")
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
        try
        {

        }
        catch (EntityNotFoundException ex)
        {
        }

        return;
    }
}
