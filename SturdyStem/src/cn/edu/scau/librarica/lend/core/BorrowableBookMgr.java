package cn.edu.scau.librarica.lend.core;

import com.github.cuter44.util.dao.*;

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
        Book b = BookMgr.get(id);
        // Existence check
        if (b == null)
            throw(new EntityNotFoundException("No such Book:"+id));

        // Duplication check
        BorrowableBook bb = get(id);
        if (bb != null)
            throw(new EntityDuplicatedException("BorrowableBook already exists:"+id));

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
}
