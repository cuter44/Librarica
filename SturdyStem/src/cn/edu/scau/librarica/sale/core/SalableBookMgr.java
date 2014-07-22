package cn.edu.scau.librarica.sale.core;

import java.util.List;

import com.github.cuter44.util.dao.*;
import org.hibernate.criterion.*;

import cn.edu.scau.librarica.sale.dao.*;
import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;

public class SalableBookMgr
{
    public static SalableBook get(Long id)
    {
        return(
            (SalableBook)HiberDao.get(SalableBook.class, id)
        );
    }

    public static SalableBook create(Long bookId)
        throws EntityNotFoundException
    {
        SalableBook sb = get(bookId);
        if (sb != null)
            throw(new EntityDuplicatedException("SalableBook already exists:"+bookId));

        Book b = BookMgr.get(bookId);
        if (b == null)
            throw(new EntityNotFoundException("No such Book:"+bookId));

        sb = new SalableBook(b);

        HiberDao.save(sb);

        return(sb);
    }

    public static void remove(Long id)
        throws EntityNotFoundException
    {
        SalableBook sb = get(id);
        if (sb == null)
            throw(new EntityNotFoundException("No such SalableBook:"+id));

        HiberDao.delete(sb);

        return;
    }

  // FILTER
    public static List<Long> filterBids(List<Long> l)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(SalableBook.class)
            .setProjection(Projections.property("id"))
            .add(Restrictions.in("id", l));

        return(
            (List<Long>)HiberDao.search(dc)
        );
    }

    public static List<String> filterIsbns(List<String> l)
    {
        for (int i=0; i<l.size(); i++)
            System.out.println(l.get(i));

        List<String> salables =
            HiberDao.createQuery("SELECT b.isbn FROM SalableBook sb INNER JOIN sb.book b WHERE (b.isbn IN (:isbns))")
                .setParameterList("isbns", l)
                .list();

        return(salables);
    }
}