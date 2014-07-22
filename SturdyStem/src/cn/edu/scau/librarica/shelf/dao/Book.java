package cn.edu.scau.librarica.shelf.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.authorize.core.UserMgr;

/** 表示书的实体
 * 书被以单本的方式加入到收藏中, i.e. 同一个账户可以加入相同isbn的书
 */
public class Book
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // FIELD
    private Long id;

    private String isbn;
    private User owner;

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

    public String getIsbn()
    {
        return(this.isbn);
    }

    public void setIsbn(String aIsbn)
    {
        this.isbn = aIsbn;
        return;
    }

    public User getOwner()
    {
        return(this.owner);
    }

    public void setOwner(User aOwner)
    {
        this.owner = aOwner;
        return;
    }
  // CONSTRUCT
    public Book()
    {
        return;
    }

    public Book(String aIsbn, User aOwner)
    {
        this.isbn = aIsbn;
        this.owner = aOwner;
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

        Book b =(Book)o;

        return(
            (this.id == b.id) ||
            (this.id != null && this.id.equals(b.id))
        );
    }
}

