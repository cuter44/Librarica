package cn.edu.scau.librarica.profile.core;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.profile.dao.*;

public class ProfileMgr
{
    public static Profile get(Long id)
    {
        return(
            (Profile)HiberDao.get(Profile.class, id)
        );
    }

    public static Profile create(Long id)
        throws EntityNotFoundException
    {
        User u = UserMgr.get(id);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+id));

        Profile p = new Profile(u);

        HiberDao.save(p);

        return(p);
    }

    //public static void update

    //public static void remove(Long id)
}
