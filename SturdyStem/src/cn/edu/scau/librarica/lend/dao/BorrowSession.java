package cn.edu.scau.librarica.lend.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.shelf.dao.Book;

/** 表示借阅的关联
 */
public class BorrowSession
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // CONSTANTS
    public static final Byte ABORTED = -2;
    public static final Byte REJECTED = -1;
    public static final Byte REQUESTED = 0;
    public static final Byte ACCEPTED = 1;
    public static final Byte READING = 2;
    public static final Byte RETURN = 3;
    public static final Byte CLOSED = 4;

  // FIELDS
    // 流水号
    private Long id;
    private Byte status;

    private User lender;
    private User borrower;

    private Book book;

  // GETTER/SETTER
    public Long getId()
    {
        return(this.id);
    }
    public void getId(Long aId)
    {
        this.id = aId;
    }

    public Byte getStatus()
    {
        return(this.status);
    }
    public void setStatus(Byte aStatus)
    {
        this.status = aStatus;
    }

    public User getLender()
    {
        return(this.lender);
    }
    public void setLender(User aLender)
    {
        this.lender = lender;
    }

    public User getBorrower()
    {
        return(this.borrower);
    }
    public void setBorrower(User aBorrower)
    {
        this.borrower = aBorrower;
    }

    public Book getBook()
    {
        return(this.book);
    }
    public void setBook(Book aBook)
    {
        this.book = aBook;
    }

  // CONSTRUCT
    public BorrowSession()
    {
        this.status = REQUESTED;
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

        if (o != null && !this.getClass().equals(o.getClass()))
            return(false);

        BorrowSession bs = (BorrowSession)o;

        return(
            (this.id == bs.id) ||
            (this.id != null && this.id.equals(bs.id))
        );
    }
}
