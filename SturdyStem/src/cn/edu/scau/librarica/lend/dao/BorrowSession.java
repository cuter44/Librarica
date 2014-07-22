package cn.edu.scau.librarica.lend.dao;

import java.io.Serializable;
import java.util.Date;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.shelf.dao.Book;

/** 表示借阅的关联
 */
public class BorrowSession
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // CONSTANTS
    /** -2, 请求方放弃借阅请求
     */
    public static final Byte ABORTED = -2;
    /** -1, 被请求方拒绝请求
     */
    public static final Byte REJECTED = -1;
    /** 0, 发起请求的初始状态
     */
    public static final Byte REQUESTED = 0;
    /** 1, 被请求方已接受请求
     */
    public static final Byte ACCEPTED = 1;
    /** 2, 被请求方已交付书籍
     */
    public static final Byte BORROWED = 2;
    /** 3, 请求方请求还书
     */
    public static final Byte RETURNING = 3;
    /** 4, 被请求方确认已收到还书
     */
    public static final Byte CLOSED = 4;

  // FIELDS
    // 流水号
    private Long id;
    /** 以上常量之一, 表示借阅会话的状态
     */
    private Byte status;

    private Book book;
    private User borrower;

    /**
     * 在 ABORTED, REJECTED, REQUESTED, ACCEPTED 状态下表示请求始发时间
     * 在 BORROWED, RETURNING, CLOSED 状态下表示实际借出交接时间
     */
    private Date tmBorrow;
    /**
     * 在 RETURNING 状态下表示归还请求时间
     * 在 CLOSED 状态下表示表示实际归还时间
     * 在其他状态下无定义
     */
    private Date tmReturn;

  // GETTER/SETTER
    public Long getId()
    {
        return(this.id);
    }
    public void setId(Long aId)
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

    public Date getTmBorrow()
    {
        return(this.tmBorrow);
    }
    public void setTmBorrow(Date aTmBorrow)
    {
        this.tmBorrow = aTmBorrow;
    }

    public Date getTmReturn()
    {
        return(this.tmReturn);
    }
    public void setTmReturn(Date aTmReturn)
    {
        this.tmReturn = aTmReturn;
    }

  // CONSTRUCT
    public BorrowSession()
    {
    }

    public BorrowSession(Book book, User borrower)
    {
        this();

        this.setBorrower(borrower);
        this.setBook(book);
        this.setStatus(REQUESTED);
        this.setTmBorrow(new Date(System.currentTimeMillis()));
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

        BorrowSession bs = (BorrowSession)o;

        return(
            (this.id == bs.id) ||
            (this.id != null && this.id.equals(bs.id))
        );
    }
}
