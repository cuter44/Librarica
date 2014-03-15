package cn.edu.scau.librarica.authorize.core;


import cn.edu.scau.librarica.authorize.dao.User;
/* hibernate */
import com.github.cuter44.util.dao.HiberDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class UserMgr
{
    public static User get(Long id)
    {
        return(
            (User)HiberDao.get(User.class, id)
        );
    }

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

    //public static void remove(Long id)

    public static boolean isLoginable(Long id)
    {
        User u = get(id);
        return(
            User.ACTIVATED.equals(u.getStatus())
        );
    }

    public static boolean isActivatable(Long id)
    {
        User u = get(id);
        return(
            User.REGISTERED.equals(u.getStatus())
        );
    }
}
