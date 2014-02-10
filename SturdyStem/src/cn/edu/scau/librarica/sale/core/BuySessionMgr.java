package cn.edu.scau.librarica.sale.core;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.authorize.core.UserMgr;
import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.sale.dao.*;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class BuySessionMgr
{
    public static BuySession get(Long id)
    {
        return(
            (BuySession)HiberDao.get(BuySession.class, id)
        );
    }

    //public static create()

    //public static update()

    public static void remove(Long id)
    {
        BuySession bs = get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BuySession:"+bs));

        HiberDao.delete(bs);

        return;
    }

  // EX
    public static boolean isBuyer(Long buySessionId, Long userId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BuySession.class)
            .add(Restrictions.eq("id", buySessionId))
            .createCriteria("buyer")
            .add(Restrictions.eq("id", userId));

        return(HiberDao.count(dc)==1);
    }

    public static boolean isSeller(Long buySessionId, Long userId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BuySession.class)
            .add(Restrictions.eq("id", buySessionId))
            .createCriteria("book")
            .createCriteria("owner")
            .add(Restrictions.eq("id", userId));

        return(HiberDao.count(dc)==1);
    }

    public static boolean isBookInvolved(Long bookId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BuySession.class)
            .add(Restrictions.ge("status", BuySession.REQUESTED))
            .add(Restrictions.lt("status", BuySession.CLOSED))
            .createCriteria("book")
            .add(Restrictions.eq("id", bookId));

        return(HiberDao.count(dc)>0);
    }

}
