package cn.edu.scau.librarica.authorize.dao;

import java.io.Serializable;
import java.util.Date;

public class User
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // ENUM
    // �ʻ�״̬ö��
    /** ע��
     */
    public static final Byte REGISTERED = 0;
    /** ����֤����
     */
    public static final Byte ACTIVATED = 1;
    /** ����ע��
     */
    public static final Byte CANCELED = -1;
    /** ���
     */
    public static final Byte BANNED = -2;

  // FIELDS
    private Long id;

    /** �ʼ���ַ, unique
     */
    private String mail;
    /** �û���, unique
     */
    private String uname;

    /** session
     */
    private byte[] skey;
    /** ��
     */
    private byte[] salt;
    /** ����ɢ�к�����
     */
    private byte[] pass;

    /** �ʻ�״̬
     */
    private Byte status;
    /** ע������
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
        this.status = REGISTERED;
    }

    public User(String aMail)
    {
        this();
        this.mail = aMail;
    }
  // HASH
    public int hashCode()
    {
        return(this.id!=null?this.id.hashCode():0);
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

