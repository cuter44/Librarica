package com.github.cuter44.util.geom;

import java.io.Serializable;
import java.lang.Cloneable;
import java.awt.Point;

/** Basic Point bean using long data-type
 */
public class PointLong
    implements Cloneable, Serializable
{
    public static final long serialVersionUID = 1;
    public long x;
    public long y;

    public PointLong()
    {
        this.x = 0L;
        this.y = 0L;
        return;
    }

    public PointLong(long x, long y)
    {
        this.x = x;
        this.y = y;
    }

    public PointLong(PointLong p)
    {
        this.x = p.x;
        this.y = p.y;
    }

    public PointLong(Point p)
    {
        this.x = (long)p.x;
        this.y = (long)p.y;
    }

    @Override
    public Object clone()
    {
        PointLong p = new PointLong(this);

        return(
            (Object)this
        );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;

        hash = hash*31 + (int)(this.x^(this.x>>>32));
        hash = hash*31 + (int)(this.y^(this.y>>>32));

        return(hash);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return(true);

        if (o!=null && !this.getClass().equals(o.getClass()))
            return(false);

        PointLong p = (PointLong)o;

        return(
            (this.x == p.x) &&
            (this.y == p.y)
        );
    }

    public long getX()
    {
        return(this.x);
    }
    public void setX(long x)
    {
        this.x = x;
    }

    public long getY()
    {
        return(this.y);
    }
    public void setY(long y)
    {
        this.y = y;
    }
}
