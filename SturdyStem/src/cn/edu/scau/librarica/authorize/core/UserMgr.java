package cn.edu.scau.librarica.authorize.core;


import cn.edu.scau.librarica.authorize.dao.User;
/* hibernate */
import com.github.cuter44.util.dao.HiberDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class UserMgr
{
    public static User forMail(String mail)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("mail", mail));

        return(
            (User)HiberDao.get(dc)
        );
    }

    public static User forUname(String uname)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(User.class)
            .add(Restrictions.eq("uname", uname));

        return(
            (User)HiberDao.get(dc)
        );
    }

    //public static String create();

    //public static String update();

    public static String remove(Long id)
    {
        User u = (User)HiberDao.get(User.class, id);

        if (u == null)
            return("notfound");

        HiberDao.delete(u);

        return("success");
    }
}
