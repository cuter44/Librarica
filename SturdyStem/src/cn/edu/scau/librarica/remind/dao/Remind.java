package cn.edu.scau.librarica.remind.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.authorize.dao.*;

public class Remind
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // FIELDS
    private Long id;

    private User user;

    private String t; // type
    private String v; // value

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

    public String getT()
    {
        return(this.t);
    }
    public void setT(String aT)
    {
        this.t = aT;
    }

    public String getV()
    {
        return(this.v);
    }
    public void setV(String aV)
    {
        this.v = aV;
    }
    public void setV(Long aId)
    {
        if (aId == null)
            this.v = null;
        else
            this.v = aId.toString();
    }

  // CONSTRUCT
    public Remind()
    {
    }

    public Remind(User u, String t, String v)
    {
        this();

        this.user = u;
        this.t = t;
        this.v = v;
    }

  // HASH
    @Override
    public int hashCode()
    {
        int hash = 17;

        if (this.user != null)
            hash = hash * 31 + this.user.hashCode();
        if (this.t != null)
            hash = hash * 31 + this.t.hashCode();
        if (this.v != null)
            hash = hash * 31 + this.v.hashCode();

        return(hash);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return(true);

        if (o!=null && !this.getClass().equals(o.getClass()))
            return(false);

        Remind r = (Remind)o;

        return(
            (
                (this.user == r.user) ||
                (this.user != null && this.user.equals(r.user))
            ) && (
                (this.t == r.t) ||
                (this.t != null && this.t.equals(r.t))
            ) && (
                (this.v == r.v) ||
                (this.v != null && this.v.equals(r.v))
            )
        );
    }
}
