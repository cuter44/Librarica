package cn.edu.scau.librarica.authorize.dao;

import java.io.Serializable;
import java.util.Date;

public class User
    implements Serializable
{
    private static final long serialVersionUID = 1L;
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

  // ENUM
    public static Byte REGISTER = 0;
    public static Byte ACTIVE = 1;
    public static Byte CANCEL = -1;
    public static Byte BANNED = -2;

  // FIELDS
    private Long id;

    private String mail;
    private String uname;

    private byte[] skey;
    private byte[] salt;
    private byte[] pass;

    private Byte status;
    private Date regDate;

  // GETTER/SETTER
    public Long getId()
    {
        return(id);
    }
    public void setId(Long aId)
    {
        this.id = aId;
    }

    public String getMail()
    {
        return(this.mail);
    }
    public void setMail(String aMail)
    {
        this.mail = aMail;
    }

    public String getUname()
    {
        return(this.uname);
    }
    public void setUname(String aUname)
    {
        this.uname = aUname;
    }

    public byte[] getSkey()
    {
        return(this.skey);
    }
    public void setSkey(byte[] aSkey)
    {
        this.skey = aSkey;
    }

    public byte[] getSalt()
    {
        return(this.salt);
    }
    public void setSalt(byte[] aSalt)
    {
        this.salt = aSalt;
    }

    public byte[] getPass()
    {
        return(this.pass);
    }
    public void setPass(byte[] aPass)
    {
        this.pass = aPass;
    }

    public Byte getStatus()
    {
        return(this.status);
    }
    public void setStatus(Byte aStatus)
    {
        this.status = aStatus;
    }

    public Date getRegDate()
    {
        return(regDate);
    }
    public void setRegDate(Date aRegDate)
    {
        this.regDate = aRegDate;
    }

  // CONSTRUCT
    public User()
    {
        this.status = REGISTER;
    }

    public User(String aMail)
    {
        this();
        this.mail = aMail;
    }
}
