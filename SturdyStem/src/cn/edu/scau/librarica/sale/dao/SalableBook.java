package cn.edu.scau.librarica.sale.dao;

import java.io.Serializable;

import cn.edu.scau.librarica.shelf.dao.*;

public class SalableBook
    implements Serializable
{
    public static long serialVersionUID = 1L;

  // FIELDS
    private Long id;

    private Book book;
    private String geohash;
    private Float price;

    /** postscript
     */
    private String ps;

  // GETSET
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

    public String getGeohash()
    {
        return(this.geohash);
    }
    public void setGeohash(String aGeohash)
    {
        this.geohash = aGeohash;
    }

    public String getPs()
    {
        return(this.ps);
    }
    public void setPs(String aPs)
    {
        this.ps = aPs;
    }

    public Float getPrice()
    {
        return(this.price);
    }
    public void setPrice(Float aPrice)
    {
        this.price = aPrice;
    }

  // CONSTRUCT
    public SalableBook()
    {
        return;
    }

    public SalableBook(Book book)
    {
        this.setBook(book);

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

        SalableBook sb = (SalableBook)o;

        return(
            (this.id == sb.id) ||
            (this.id != null && this.id.equals(sb.id))
        );
    }
}
