package cn.edu.scau.librarica.lend.core;

import java.util.List;

import com.github.cuter44.util.dao.*;
import org.hibernate.criterion.*;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;


public class BorrowableBookMgr
{
    public static BorrowableBook get(Long id)
    {
        return(
            (BorrowableBook)HiberDao.get(BorrowableBook.class, id)
        );
    }

    public static BorrowableBook create(Long id)
    {
        // Duplication check
        BorrowableBook bb = get(id);
        if (bb != null)
            throw(new EntityDuplicatedException("BorrowableBook already exists:"+id));

        Book b = BookMgr.get(id);
        // Existence check
        if (b == null)
            throw(new EntityNotFoundException("No such Book:"+id));

        bb = new BorrowableBook(b);

        HiberDao.save(bb);

        return(bb);
    }

    //public static update()

    public static void remove(Long id)
    {
        BorrowableBook bb = get(id);
        if (bb == null)
            throw(new EntityNotFoundException("No such BorrowableBook:"+id));

        HiberDao.delete(bb);

        return;
    }

  // EX
  // FILTER
    public static List<Long> filterBids(List<Long> l)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(BorrowableBook.class)
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

        List<String> borrowables =
            HiberDao.createQuery("SELECT b.isbn FROM BorrowableBook bb INNER JOIN bb.book b WHERE (b.isbn IN (:isbns))")
                .setParameterList("isbns", l)
                .list();

        return(borrowables);
    }

}
