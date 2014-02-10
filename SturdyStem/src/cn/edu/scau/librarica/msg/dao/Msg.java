package cn.edu.scau.librarica.msg.dao;

import java.io.Serializable;
import java.util.Date;

import cn.edu.scau.librarica.authorize.dao.User;

public class Msg
    implements Serializable
{
  // SERIALIZABLE
    public static long serialVersionUID = 1L;

  // FIELDS
    private Long id;

    /** from
     */
    private User f;
    /** to
     */
    private User t;

    /** timestamp
     */
    public Date m;
    /** content
     */
    private String c;

  // GETTER/SETTER
    public Long getId()
    {
        return(this.id);
    }
    public void setId(Long aId)
    {
        this.id = aId;

        return;
    }

    public User getF()
    {
        return(this.f);
    }
    public void setF(User aF)
    {
        this.f = aF;
        return;
    }

    public User getT()
    {
        return(this.t);
    }
    public void setT(User aT)
    {
        this.t = aT;
        return;
    }

    public Date getM()
    {
        return(this.m);
    }
    public void setM(Date aM)
    {
        this.m = aM;
        return;
    }

    public String getC()
    {
        return(this.c);
    }
    public void setC(String aC)
    {
        this.c = aC;
        return;
    }

  // CONSTRUCT
    public Msg()
    {
        this.m = new Date(System.currentTimeMillis());

        return;
    }

    public Msg(User from, User to, String content)
    {
        this();

        this.setF(from);
        this.setT(to);
        this.setC(content);

        return;
    }

  // HASH
    @Override
    public int hashCode()
    {
        int hash = 17;

        if (this.id != null)
            hash = hash * 31 + this.id.hashCode();

        return(hash);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return(true);

        if (o!=null && !this.getClass().equals(o.getClass()))
            return(false);

        Msg msg = (Msg)o;

        return(
            (
                (this.f == msg.f) ||
                (this.f != null && this.f.equals(msg.f))
            ) && (
                (this.t == msg.t) ||
                (this.t != null && this.t.equals(msg.t))
            ) && (
                (this.c == msg.c) ||
                (this.c != null && this.c.equals(msg.c))
            ) && (
                (this.m == msg.m) ||
                (this.m != null && this.m.equals(msg.m))
            )
        );
    }

}
