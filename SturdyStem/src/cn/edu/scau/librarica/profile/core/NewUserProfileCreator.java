package cn.edu.scau.librarica.profile.core;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.profile.core.*;

public class NewUserProfileCreator
    implements Authorizer.StatusChangedListener
{
    private NewUserProfileCreator()
    {
        return;
    }

    @Override
    public void onStatusChanged(User u)
    {
        if (!User.ACTIVATED.equals(u.getStatus()))
            return;

        ProfileMgr.create(u.getId());
    }

    static
    {
        Authorizer.addListener(new NewUserProfileCreator());
    }
}
