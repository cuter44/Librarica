package cn.edu.scau.librarica.authorize.dao;

import java.io.Serializable;
import java.util.Date;

public class User
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // ENUM
    // 帐户状态枚举
    /** 注册
     */
    public static final Byte REGISTER = 0;
    /** 已验证邮箱
     */
    public static final Byte ACTIVATED = 1;
    /** 主动注销
     */
    public static final Byte CANCEL = -1;
    /** 封号
     */
    public static final Byte BANNED = -2;

  // FIELDS
    private Long id;

    /** 邮件地址, unique
     */
    private String mail;
    /** 用户名, unique
     */
    private String uname;

    /** session
     */
    private byte[] skey;
    /** 盐
     */
    private byte[] salt;
    /** 加盐散列后密码
     */
    private byte[] pass;

    /** 帐户状态
     */
    private Byte status;
    /** 注册日期
     */
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

        User u = (User)o;

        return(
            (this.id == u.id) ||
            (this.id != null && this.id.equals(u.id))
        );
    }
}

