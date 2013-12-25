package cn.edu.scau.librarica.shelf.core;

import java.util.List;

import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.authorize.core.UserMgr;
import com.github.cuter44.util.dao.*;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

public class BookMgr
{
    public static Book get(Long id)
    {
        return(
            (Book)HiberDao.get(Book.class, id)
        );
    }

    public static List<Book> find(String isbn, Long ownerId)
    {
        DetachedCriteria dc = DetachedCriteria.forClass(Book.class)
            .add(Restrictions.eq("isbn", isbn))
            .createCriteria("owner")
            .add(Restrictions.eq("id", ownerId));

        return(
            (List<Book>)HiberDao.search(dc)
        );
    }

    public static Book create(String isbn, Long ownerId)
    {
        User u = UserMgr.get(ownerId);
        if (ownerId == null)
            throw(new EntityNotFoundException("No such user:"+ownerId));

        Book b = new Book(isbn, u);
        HiberDao.save(b);

        return(b);
    }

    //public static Book update(id)

    public static void remove(Long id)
        throws EntityReferencedException
    {
        Book b = get(id);

        if (b == null)
            throw(new EntityNotFoundException("No such Book:"+id));

        try
        {
            HiberDao.delete(b);
        }
        catch (ConstraintViolationException ex)
        {
            throw(new EntityReferencedException(ex));
        }
    }
}
