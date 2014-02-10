package cn.edu.scau.librarica.sale.core;

import com.github.cuter44.util.dao.*;

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
}