package cn.edu.scau.librarica.lend.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.shelf.dao.Book;
import cn.edu.scau.librarica.shelf.core.BookMgr;

/** ��ʾ�ɳ����鼮��ʵ��
 * �鼮������Ĳ��龭�ϼܺ��Ϊ�ɳ����鼮, Ȼ����Ա�������, ֱ��������¼�.
 * <br />
 * ͬһ�鼮ֻ�ܱ��Ǽǳ�����һ������ʵ��.
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
     * ����, ���������һ����ʾ
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
