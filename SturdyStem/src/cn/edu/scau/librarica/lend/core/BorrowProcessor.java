package cn.edu.scau.librarica.lend.core;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.shelf.dao.*;
import cn.edu.scau.librarica.shelf.core.*;
import cn.edu.scau.librarica.lend.dao.*;

/** �������̴�����
 */
public class BorrowProcessor
{
  // ANNOUNCER
    /** ����״̬������
     * �����ĻỰ״̬�������ʱ(��������)����֪ͨ����ע��ļ�����
     * ���������߳�, ��������ʹ������
     */
    public static interface StatusChangedListener
    {
        public abstract void onStatusChanged(BorrowSession bs);
    }

    private static ArrayList<StatusChangedListener> statusChangedListeners = new ArrayList<StatusChangedListener>();

    public static synchronized void addListener(StatusChangedListener l)
    {
        statusChangedListeners.add(l);

        return;
    }

    private static void fireStatusChanged(BorrowSession bs)
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
     */
    private static StatusChangedListener rejectOthersTrigger = new StatusChangedListener()
    {
        @Override
        public void onStatusChanged(BorrowSession bs)
        {
            if (bs.getStatus() != BorrowSession.ACCEPTED)
                return;

            Session s = null;

            try
            {
                s = HiberDao.begin(HiberDao.newSession());

                DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
                    .add(Restrictions.ne("id", bs.getId()))
                    .add(Restrictions.eq("status", BorrowSession.REQUESTED))
                    .createCriteria("book")
                    .add(Restrictions.eq("id", bs.getBook().getId()));

                List<BorrowSession> requests = (List<BorrowSession>)HiberDao.search(s, dc);
                Iterator<BorrowSession> itr = requests.iterator();

                while (itr.hasNext())
                {
                    BorrowSession _bs = itr.next();

                    _bs.setStatus(BorrowSession.REJECTED);
                    HiberDao.update(s, _bs);

                    BorrowProcessor.fireStatusChanged(_bs);
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

  // CANCELIATION
    private static void reject(BorrowSession bs)
    {
        bs.setStatus(BorrowSession.REJECTED);
        HiberDao.update(bs);

        fireStatusChanged(bs);
    }

    /** �ܾ���������
     * ״̬: REQUESTED | ACCEPTED -> REJECTED
     * @warning û�������֤����
     * @warning ��Ӧ�ô� ACCEPTED ��� REJECTED, ��Ϊ�н�����֮��������Ŀ�����
     * @param id BorrowSession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void reject(Long id)
        throws IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        if (!BorrowSession.REQUESTED.equals(bs.getStatus()))
            throw(new IllegalStateException("Status must be REQUESTED, BorrowSession id:"+id));

        reject(bs);
        return;
    }

    /** �ܾ�����ͬһ���������
     */
    //public static void rejectAll(Long borrowableBookId)
    //{
        //DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            //.add(Restrictions.eq("status", BorrowSession.REQUESTED))
            //.createCriteria("book")
            //.add(Restrictions.eq("id", borrowableBookId));
        //List<BorrowSession> requests = (List<BorrowSession>)HiberDao.search(dc);
        //Iterator<BorrowSession> itr = requests.iterator();

        //while (itr.hasNext())
            //reject(itr.next());

        //return;
    //}

    /** ȡ����������
     * ״̬: REQUESTED -> ABORTED
     * @warning û�������֤
     * @warning ���ܴ� ACCEPTED ��� ABORTED, ��Ϊ���õ���֮����թ�Ŀ���
     * @param id BorrowSession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void abort(Long id)
        throws IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        if (!BorrowSession.REQUESTED.equals(bs.getStatus()))
            throw(new IllegalStateException("Status must be REQUESTED, BorrowSession id:"+id));

        bs.setStatus(BorrowSession.ABORTED);
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }

    ///** (����)(�ƻ���)˫��Э�������Ự
     //* ʹ���� ACCEPTED ֮���״̬, ����˫��ͨ��Э��ǿ��ȡ��һ����������, ͨ�����ڶ���֮���״̬
     //*/
    //public static boolean terminate(Long id)

  // PROCESS
    /** �������
     * ֻ�еǼ�Ϊ BorrowableBook ������Խ�
     * @param borrowerId ���������id
     * @param bookId Ŀ���鼮��id
     * ״̬: - -> REQUESTED
     * @exception EntityNotFoundException �� bookId �����Գ���ʱ
     */
    public static BorrowSession requestBorrow(Long bookId, Long borrowerId)
        throws EntityNotFoundException
    {
        // TODO
        // �������ܾ�

        // if existed
        DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.eq("status", BorrowSession.REQUESTED));
        dc.createCriteria("borrower")
            .add(Restrictions.eq("id", borrowerId));
        dc.createCriteria("book")
            .add(Restrictions.eq("id", bookId));
        BorrowSession bs = (BorrowSession)HiberDao.get(dc);
        if (bs != null)
            return(bs);

        // else
        //
        User borrower = UserMgr.get(borrowerId);
        if (borrower == null)
            throw(new EntityNotFoundException("No such User:"+borrowerId));

        BorrowableBook bb = BorrowableBookMgr.get(bookId);
        if (bb == null)
            throw(new EntityNotFoundException("No such BorrowableBook:"+bookId));
        Book book = bb.getBook();

        bs = new BorrowSession(book, borrower);

        HiberDao.save(bs);

        fireStatusChanged(bs);

        return(bs);
    }

    /** ���ܽ�������
     * @warning û�������֤
     * ״̬: REQUESTED -> ACCEPTED
     * @param id BorrowSession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void accept(Long id)
        throws EntityNotFoundException, IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        if (!BorrowSession.REQUESTED.equals(bs.getStatus()))
            throw(new IllegalStateException("Status must be REQUESTED, BorrowSession id:"+id));

        bs.setStatus(BorrowSession.ACCEPTED);
        HiberDao.update(bs);

        // remove borrowable
        try
        {
            BorrowableBookMgr.remove(bs.getBook().getId());
        }
        catch (EntityNotFoundException ex)
        {
        }

        fireStatusChanged(bs);

        return;
    }

    /** ȷ�Ͻ���
     * @warning û�������֤, û�е�¼������֤
     * ״̬: ACCEPTED -> BORROWED
     * @param id BorrowSession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void confirmBorrow(Long id)
        throws IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        if (!BorrowSession.ACCEPTED.equals(bs.getStatus()))
            throw(new IllegalStateException("Status must be ACCEPTED, BorrowSession id:"+id));

        bs.setStatus(BorrowSession.BORROWED);
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }

    /** ���ܽ�������
     * @warning û�������֤, û�е�¼������֤
     * ״̬: BORROWED -> RETURNING
     * @param id BorrowSession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void requestReturn(Long id)
        throws IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        if (!BorrowSession.BORROWED.equals(bs.getStatus()))
            throw(new IllegalStateException("Status must be BORROWED, BorrowSession id:"+id));

        bs.setStatus(BorrowSession.RETURNING);
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }

    /** ���ܽ�������
     * @warning û�������֤, û�е�¼������֤
     * ״̬: RETURNING -> CLOSED
     * @param id BorrowSession id
     * @exception IllegalStateException ������ REQUESTED ״̬ʱ
     */
    public static void confirmReturn(Long id)
        throws IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        if (!BorrowSession.RETURNING.equals(bs.getStatus()))
            throw(new IllegalStateException("Status must be RETURNING, BorrowSession id:"+id));

        bs.setStatus(BorrowSession.CLOSED);
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }
}
