package cn.edu.scau.librarica.sale.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.sale.dao.*;

/** �������̴�����
 */
public class DealProcessor
{
  // ANNOUNCER
    /** ״̬������
     * ���Ự״̬�������ʱ(��������)����֪ͨ����ע��ļ�����
     * ���������߳�, ��������ʹ������
     */
    public static interface StatusChangedListener
    {
        public abstract void onStatusChanged(BuySession bs);
    }

    private static ArrayList<StatusChangedListener> statusChangedListeners = new ArrayList<StatusChangedListener>();

    public static synchronized void addListener(StatusChangedListener l)
    {
        statusChangedListeners.add(l);

        return;
    }

    private static void fireStatusChanged(BuySession bs)
    {
        for (int i=0; i<statusChangedListeners.size(); i++)
        {
            try
            {
                statusChangedListeners.get(i).onStatusChanged(bs);
            }
            catch (Throwable ex)
            {
                ex.printStackTrace();
            }
        }
    }

  // SELF-LISTENER
    /** ����������
     * ���ӵ� rejectAll() ��;
     * ���������ܾ���ͬһ�����������������
     * �������ڸ����û����۵���
     */
    private static StatusChangedListener rejectOthersTrigger = new StatusChangedListener()
    {
        @Override
        public void onStatusChanged(BuySession bs)
        {
            if (!BuySession.ACCEPTED.equals(bs.getStatus()))
                return;

            if (!User.INDIVIDUAL.equals(bs.getBook().getOwner().getUserType()))
                return;

            Session s = null;

            try
            {
                s = HiberDao.begin(HiberDao.newSession());

                DetachedCriteria dc = DetachedCriteria.forClass(BuySession.class)
                    .add(Restrictions.ne("id", bs.getId()))
                    .add(Restrictions.eq("status", BuySession.REQUESTED))
                    .createCriteria("book")
                    .add(Restrictions.eq("id", bs.getBook().getId()));

                List<BuySession> requests = (List<BuySession>)HiberDao.search(s, dc);
                Iterator<BuySession> i = requests.iterator();

                while (i.hasNext())
                {
                    BuySession _bs = i.next();

                    _bs.setStatus(BuySession.REJECTED);
                    HiberDao.update(s, _bs);

                    DealProcessor.fireStatusChanged(_bs);
                }

                HiberDao.commit(s);
            }
            catch (Exception ex)
            {
                HiberDao.rollback(s);
                ex.printStackTrace();
            }
            finally
            {
                HiberDao.close(s);
            }

            return;
        }
    };

    static
    {
        addListener(rejectOthersTrigger);
    }

    /** �¼ܴ�����
     * ���ڽ����ܶ�������ӿ�����״̬���
     * �������ڸ����û�
     */
    private static StatusChangedListener salableRemover = new StatusChangedListener()
    {
        @Override
        public void onStatusChanged(BuySession bs)
        {
            if (!BuySession.ACCEPTED.equals(bs.getStatus()))
                return;

            if (!User.INDIVIDUAL.equals(bs.getBook().getOwner().getUserType()))
                return;

            try
            {
                SalableBookMgr.remove(bs.getBook().getId());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            return;
        }
    };

    static
    {
        addListener(salableRemover);
    }

    /** ���̴�����
     * ���ڽ�����������̵��������
     * �������ڸ����û����۵���
     */
    private static StatusChangedListener soldBookTransactor = new StatusChangedListener()
    {
        @Override
        public void onStatusChanged(BuySession bs)
        {
            if (!BuySession.CLOSED.equals(bs.getStatus()))
                return;

            if (!User.INDIVIDUAL.equals(bs.getBook().getOwner().getUserType()))
                return;

            try
            {
                Book b = bs.getBook();
                b.setOwner(bs.getBuyer());

                HiberDao.update(b);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            return;
        }
    };

    static
    {
        addListener(soldBookTransactor);
    }

  // CANCELIATION
    private static void reject(BuySession bs)
    {
        bs.setStatus(BuySession.REJECTED);
        HiberDao.update(bs);

        fireStatusChanged(bs);
    }

    /** �ܾ�����
     * ״̬: REQUESTED | ACCEPTED -> REJECTED
     * @warning û�������֤����
     * @param id BuySession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void reject(Long id)
        throws IllegalStateException
    {
        BuySession bs = BuySessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BuySession:"+id));

        Byte status = bs.getStatus();
        if (!(BuySession.REQUESTED.equals(status) ||
            BuySession.ACCEPTED.equals(status)))
            throw(new IllegalStateException("BuySession must be REQUESTED||ACCEPTED:"+id));

        reject(bs);
        return;
    }

    /** ȡ������
     * ״̬: REQUESTED -> ABORTED
     * @warning û�������֤
     * @warning ���ܴ� ACCEPTED ��� ABORTED,
     * @param id BuySession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void abort(Long id)
        throws IllegalStateException
    {
        BuySession bs = BuySessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BuySession:"+id));

        if (!BuySession.REQUESTED.equals(bs.getStatus()))
            throw(new IllegalStateException("BuySession must be REQUESTED:"+id));

        bs.setStatus(BuySession.ABORTED);
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }

  // PROCESS
    /** ����
     * ֻ�еǼ�Ϊ SalableBook ������Խ�
     * ����ͬһ���������ֻ�᷵��ͬһ���ʵ��, �Ѿ��ܾ�/����/�ᵥ��ʵ�������ڻʵ��
     * @param borrowerId ���������id
     * @param bookId Ŀ���鼮��id
     * ״̬: - -> REQUESTED
     * @exception EntityNotFoundException �� bookId ���ڳ���ʱ
     */
    public static BuySession request(Long bookId, Long buyerId, Integer qty)
        throws EntityNotFoundException
    {
        // if existed
        DetachedCriteria dc = DetachedCriteria.forClass(BuySession.class)
            .add(Restrictions.ge("status", BuySession.REQUESTED))
            .add(Restrictions.lt("status", BuySession.CLOSED));
        dc.createCriteria("buyer")
            .add(Restrictions.eq("id", buyerId));
        dc.createCriteria("book")
            .add(Restrictions.eq("id", bookId));
        BuySession bs = (BuySession)HiberDao.get(dc);
        if (bs != null)
            return(bs);

        // else
        //
        User buyer = UserMgr.get(buyerId);
        if (buyer == null)
            throw(new EntityNotFoundException("No such User:"+buyerId));

        SalableBook sb = SalableBookMgr.get(bookId);
        if (sb == null)
            throw(new EntityNotFoundException("No such BorrowableBook:"+bookId));
        Book book = sb.getBook();

        bs = new BuySession(book, buyer, qty);

        HiberDao.save(bs);

        fireStatusChanged(bs);

        return(bs);
    }

    /** ��������
     * @warning û�������֤
     * ״̬: REQUESTED -> ACCEPTED
     * @param id BuySession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void accept(Long id)
        throws EntityNotFoundException, IllegalStateException
    {
        BuySession bs = BuySessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BuySession:"+id));

        if (!BuySession.REQUESTED.equals(bs.getStatus()))
            throw(new IllegalStateException("BuySession must be REQUESTED:"+id));

        bs.setStatus(BuySession.ACCEPTED);
        HiberDao.update(bs);

        //// remove borrowable
        //try
        //{
            //BorrowableBookMgr.remove(bs.getBook().getId());
        //}
        //catch (EntityNotFoundException ex)
        //{
        //}

        fireStatusChanged(bs);

        return;
    }

    /** �رչ���Ự
     * @warning û�������֤
     * ״̬: ACCEPTED -> CLOSED
     * @param id BuySession id
     * @exception IllegalStateException ������ ACCEPTED ״̬ʱ
     */
    public static void close(Long id)
        throws EntityNotFoundException, IllegalStateException
    {
        BuySession bs = BuySessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BuySession:"+id));

        if (!BuySession.ACCEPTED.equals(bs.getStatus()))
            throw(new IllegalStateException("BuySession must be ACCEPTED:"+id));

        bs.setStatus(BuySession.CLOSED);
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }
}
