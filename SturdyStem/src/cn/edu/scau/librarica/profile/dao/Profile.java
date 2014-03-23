package cn.edu.scau.librarica.profile.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.authorize.dao.User;

public class Profile
{
    public static final long serialVersionUID = 1L;

  // fields
    private Long id;
    private User user;

    private String dname;
    /** Trusted Name
     * 由系统认证功能产生
     */
    private String tname;
    private String motto;
    private String avatar;
    /** 预留 geohash 字段, 也可以自愿性地提供
     */
    private String pos;

    /** 藏书数
     */
    private Long stored;

    private Long borrowing;
    private Long borrowed;
    private Long lent;

    private Long bought;
    private Long sold;

    private Long like;
    private Long liked;
    private Long hate;
    private Long hated;

  // GET/SET
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

    public String getDname()
    {
        return(this.dname);
    }
    public void setDname(String aDname)
    {
        this.dname = aDname;
    }

    public String getTname()
    {
        return(this.tname);
    }
    public void setTname(String aTname)
    {
        this.tname = aTname;
    }

    public String getMotto()
    {
        return(this.motto);
    }
    public void setMotto(String aMotto)
    {
        this.motto = aMotto;
    }

    public String getAvatar()
    {
        return(this.avatar);
    }
    public void setAvatar(String aAvatar)
    {
        this.avatar = aAvatar;
    }

    public String getPos()
    {
        return(this.pos);
    }
    public void setPos(String aPos)
    {
        this.pos = aPos;
    }

    public Long getStored()
    {
        return(this.stored);
    }
    public void setStored(Long aStored)
    {
        this.stored = aStored;
    }

    public Long getBorrowing()
    {
        return(this.borrowing);
    }
    public void setBorrowing(Long aBorrowing)
    {
        this.borrowing = aBorrowing;
    }

    public Long getBorrowed()
    {
        return(this.borrowed);
    }
    public void setBorrowed(Long aBorrowed)
    {
        this.borrowed = aBorrowed;
    }

    public Long getLent()
    {
        return(this.lent);
    }
    public void setLent(Long aLent)
    {
        this.lent = aLent;
    }

    public Long getBought()
    {
        return(this.bought);
    }
    public void setBought(Long aBought)
    {
        this.bought = aBought;
    }

    public Long getSold()
    {
        return(this.sold);
    }
    public void setSold(Long aSold)
    {
        this.sold = aSold;
    }

    public Long getLike()
    {
        return(this.like);
    }
    public void setLike(Long aLike)
    {
        this.like = aLike;
    }

    public Long getLiked()
    {
        return(this.liked);
    }
    public void setLiked(Long aLiked)
    {
        this.liked = aLiked;
    }

    public Long getHate()
    {
        return(this.hate);
    }
    public void setHate(Long aHate)
    {
        this.hate = aHate;
    }

    public Long getHated()
    {
        return(this.hated);
    }
    public void setHated(Long aHated)
    {
        this.hated = aHated;
    }

  // CONSTRUCT
    public Profile()
    {
        return;
    }

    public Profile(User u)
    {
        this();

        this.user = u;
        if (u.getMail() != null)
            this.dname = u.getMail().replaceAll("@.+", "");

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

        Profile p = (Profile)o;

        return(
            (this.id == p.id) ||
            (this.id != null && this.id.equals(p.id))
        );
    }
}

