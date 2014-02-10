package cn.edu.scau.librarica.sale.dao;

import java.io.Serializable;
import java.util.Date;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.shelf.dao.Book;

/** 表示借阅的关联
 */
public class BuySession
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // CONSTANTS
    public static final Byte ABORTED = -2;
    public static final Byte REJECTED = -1;
    public static final Byte REQUESTED = 0;
    public static final Byte ACCEPTED = 1;
    public static final Byte CLOSED = 2;

  // FIELDS
    // 流水号
    private Long id;
    private Byte status;

    private Book book;
    private User buyer;

    /** 交易状态上一次变化的时间
     */
    private Date tmStatus;

    private Integer qty;

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

    public User getBuyer()
    {
        return(this.buyer);
    }
    public void setBuyer(User aBuyer)
    {
        this.buyer = aBuyer;
    }

    public Book getBook()
    {
        return(this.book);
    }
    public void setBook(Book aBook)
    {
        this.book = aBook;
    }

    public Date getTmStatus()
    {
        return(this.tmStatus);
    }
    public void setTmStatus(Date aTmStatus)
    {
        this.tmStatus = aTmStatus;
    }

    public Integer getQty()
    {
        return(this.qty);
    }
    public void setQty(Integer aQty)
    {
        this.qty = aQty;
    }

  // CONSTRUCT
    public BuySession()
    {
    }

    public BuySession(Book book, User buyer, Integer qty)
    {
        this();

        this.setBuyer(buyer);
        this.setBook(book);
        this.setStatus(REQUESTED);
        this.setTmStatus(new Date(System.currentTimeMillis()));
        this.setQty(qty);
    }

    //public SaleSession(Book book, User buyer)
    //{
        //this(book, buyer, 1);
    //}

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

        BuySession bs = (BuySession)o;

        return(
            (this.id == bs.id) ||
            (this.id != null && this.id.equals(bs.id))
        );
    }
}
