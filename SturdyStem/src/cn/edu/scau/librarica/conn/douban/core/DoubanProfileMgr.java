package cn.edu.scau.librarica.conn.douban.core;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.conn.douban.dao.*;
import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.authorize.core.UserMgr;

public class DoubanProfileMgr
{
    public static DoubanProfile get(Long id)
    {
        return(
            (DoubanProfile)HiberDao.get(DoubanProfile.class, id)
        );
    }

    public static DoubanProfile create(Long userId)
    {
        User u = UserMgr.get(userId);
        if (u == null)
            throw(new EntityNotFoundException("No sueh User:"+userId));

        DoubanProfile dp = new DoubanProfile(u);

        HiberDao.save(dp);

        return(dp);
    }

    //public static void update()

    public static void remove(Long id)
    {
        DoubanProfile dp = get(id);
        if (dp == null)
            throw(new EntityNotFoundException("No such DoubanProfile:"+id));

        HiberDao.delete(dp);

        return;
    }

  // EX
    //public static DoubanProfile getOrUpdate(Long userId)
    //{
        //DoubanProfile dp = get(userId);

        //return((dp!=null)?dp:create(userId));
    //}
}
