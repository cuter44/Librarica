package cn.edu.scau.librarica.profile.dao;

import java.util.Properties;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.edu.scau.librarica.authorize.dao.User;

public class Profile
{
    public static final long serialVersionUID = 1L;
    private static final String gravatarPattern = "http://www.gravatar.com/avatar/:key.:format?s=:size";

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

    /** 额外字段, 接受其他模块填入
     * 通常是一些导出值
     */
    public Properties others;

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

  // UTIL
    // based on com.github.cuter44.util.crypto.CryptoUtil
    private static String md5(String in)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(in.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder(bytes.length*2);
            for (int i=0; i<bytes.length; i++)
                sb.append(
                    String.format("%02x", bytes[i] & 0xff)
                );
            return(sb.toString());
        }
        catch (NoSuchAlgorithmException ex)
        {
            // never occur
            ex.printStackTrace();
            return(null);
        }
        catch (UnsupportedEncodingException ex)
        {
            // never occur
            ex.printStackTrace();
            return(null);
        }
    }

  // CONSTRUCT
    public Profile()
    {
        this.others = new Properties();

        return;
    }

    public Profile(User u)
    {
        this();

        this.user = u;

        if (u.getMail() != null)
        {
            // generate default dname
            this.dname = u.getMail().replaceAll("@.+", "");

            // generate default avatar
            this.avatar = gravatarPattern.replace(":key", md5(u.getMail()));
        }

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

