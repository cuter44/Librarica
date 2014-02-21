package cn.edu.scau.librarica.conn.douban.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.authorize.dao.User;

public class DoubanProfile
{
    public static final long serialVerisonUID = 1L;

  // FIELDS
    private Long id;
    private User user;

    /**
     * 这么写是为了与豆瓣接口的命名风格一致
     */
    private String access_token;
    private String refresh_token;

    private String douban_user_id;

  // GETSET
    public Long getId()
    {
        return(this.id);
    }
    public void setId(Long aId)
    {
        this.id = aId;
    }

    public User getUser()
    {
        return(this.user);
    }
    public void setUser(User aUser)
    {
        this.user = aUser;
    }

    public String getAccess_token()
    {
        return(this.access_token);
    }
    public void setAccess_token(String aAccess_token)
    {
        this.access_token = aAccess_token;
    }

    public String getRefresh_token()
    {
        return(this.refresh_token);
    }
    public void setRefresh_token(String aRefresh_token)
    {
        this.refresh_token = aRefresh_token;
    }

    public String getDouban_user_id()
    {
        return(this.douban_user_id);
    }
    public void setDouban_user_id(String aDouban_user_id)
    {
        this.douban_user_id = aDouban_user_id;
    }

  // CONSTRUCT
    public DoubanProfile()
    {
        return;
    }

    public DoubanProfile(User u)
    {
        this();

        this.user = u;

        return;
    }

  // HASH
    public int hashCode()
    {
        int hash = 17;

        if (this.id != null)
            hash = hash * 31 + this.id.hashCode();

        return(hash);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return(true);

        if (o!=null && !this.getClass().equals(o.getClass()))
            return(false);

        DoubanProfile dp = (DoubanProfile)o;

        return(
            (this.id == dp.id) ||
            (this.id != null && this.id.equals(dp.id))
        );
    }
}
