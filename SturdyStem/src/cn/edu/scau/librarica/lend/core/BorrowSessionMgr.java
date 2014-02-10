package cn.edu.scau.librarica.lend.core;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.authorize.core.UserMgr;
import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.lend.dao.*;
// import cn.edu.scau.librarica.lend.dao.*;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class BorrowSessionMgr
{
    public static BorrowSession get(Long id)
    {
        return((BorrowSession)HiberDao.get(BorrowSession.class, id));
    }

    //public static create()

    //public static update()

    public static void remove(Long id)
    {
        BorrowSession bs = get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+bs));

        HiberDao.delete(bs);

        return;
    }

    public static boolean isBorrower(Long borrowSessionId, Long userId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.eq("id", borrowSessionId))
            .createCriteria("borrower")
            .add(Restrictions.eq("id", userId));

        return(HiberDao.count(dc)==1);
    }

    public static boolean isLender(Long borrowSessionId, Long userId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.eq("id", borrowSessionId))
            .createCriteria("book")
            .createCriteria("owner")
            .add(Restrictions.eq("id", userId));

        return(HiberDao.count(dc)==1);
    }

    public static boolean isBookInvolved(Long bookId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.ge("status", BorrowSession.REQUESTED))
            .add(Restrictions.lt("status", BorrowSession.CLOSED))
            .createCriteria("book")
            .add(Restrictions.eq("id", bookId));

        return(HiberDao.count(dc)>0);
    }

}
