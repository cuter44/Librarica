package cn.edu.scau.librarica.lend.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.shelf.dao.Book;
import cn.edu.scau.librarica.shelf.core.BookMgr;

/** 表示可出借书籍的实体
 * 书籍从自身的藏书经上架后成为可出借书籍, 然后可以被搜索到, 直到被借出下架.
 * <br />
 * 同一书籍只能被登记出借于一个出借实体.
 *
 */
public class BorrowableBook
    implements Serializable
{
    public static final long serialVersionUID = 1L;

  // FIELDS
    // ref book id
    private Long id;

    private Book book;
    private String pos;

    /** postscript
     * 附言, 与搜索结果一起显示
     */
    private String ps;

  // GETTER/SETTER
    public Long getId()
    {
        return(this.id);
    }
    public void setId(Long aId)
    {
        this.id = aId;
    }

    public Book getBook()
    {
        return(this.book);
    }
    public void setBook(Book aBook)
    {
        this.book = aBook;
    }

    public String getPos()
    {
        return(this.pos);
    }
    public void setPos(String aPos)
    {
        this.pos = aPos;
    }

    public String getPs()
    {
        return(this.ps);
    }
    public void setPs(String aPs)
    {
        this.ps = aPs;
    }

  // CONSTRUCT
    public BorrowableBook()
    {
        return;
    }

    public BorrowableBook(Book book)
    {
        this();

        this.setBook(book);
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

        BorrowableBook bb = (BorrowableBook)o;

        return(
            (this.id == bb.id) ||
            (this.id != null && this.id.equals(bb.id))
        );
    }
}
