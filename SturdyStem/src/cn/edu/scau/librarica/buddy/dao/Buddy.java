package cn.edu.scau.librarica.buddy.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.authorize.dao.*;

public class Buddy
    implements Serializable
{
    public static long serialVersionUID = 1L;

  // FIELDS
    public static Byte LIKE = 1;
    public static Byte HATE = -1;

    private Long id;

    private Byte r; // relation

    private User me; // me
    private User op; // opponent

  // GETSET
    public Long getId()
    {
        return(this.id);
    }
    public void setId(Long aId)
    {
        this.id = aId;
    }

    public Byte getR()
    {
        return(this.r);
    }
    public void setR(Byte aR)
    {
        this.r = aR;
    }

    public User getMe()
    {
        return(this.me);
    }
    public void setO(User aMe)
    {
        this.me = aMe;
    }

    public User getOp()
    {
        return(this.op);
    }
    public void setOp(User aOp)
    {
        this.op = aOp;
    }

  // CONSTRUCT
    public Buddy()
    {
        return;
    }

    public Buddy(User me, User op)
    {
        this.me = me;
        this.op = op;

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

        Buddy b = (Buddy)o;

        return(
            (this.id == b.id) ||
            (this.id != null && this.id.equals(b.id))
        );
    }
}
