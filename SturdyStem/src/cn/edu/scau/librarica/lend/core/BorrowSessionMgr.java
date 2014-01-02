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

    //public static remove()

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
}
