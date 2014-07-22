package cn.edu.scau.librarica.lend.core;

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
import cn.edu.scau.librarica.lend.dao.*;

/** 借书流程处理器
 */
public class BorrowProcessor
{
  // ANNOUNCER
    /** 借阅状态监听器
     * 当借阅会话状态发生变更时(包括创建)将会通知所有注册的监听器
     * 除非另起线程, 否则请勿使用阻塞
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
    /** 排他处理器
     * 连接到 rejectAll() 上;
     * 用于排他拒绝对同一本书的其他借阅请求
     */
    private static StatusChangedListener rejectOthersTrigger = new StatusChangedListener()
    {
        @Override
        public void onStatusChanged(BorrowSession bs)
        {
            if (!BorrowSession.ACCEPTED.equals(bs.getStatus()))
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
                Iterator<BorrowSession> i = requests.iterator();

                while (i.hasNext())
                {
                    BorrowSession _bs = i.next();

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

    /** 拒绝借书请求
     * 状态: REQUESTED | ACCEPTED -> REJECTED
     * @warning 没有身份验证保护
     * @param id BorrowSession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
     */
    public static void reject(Long id)
        throws IllegalStateException
    {
        BorrowSession bs = BorrowSessionMgr.get(id);
        if (bs == null)
            throw(new EntityNotFoundException("No such BorrowSession:"+id));

        Byte status = bs.getStatus();
        if (!(BorrowSession.REQUESTED.equals(status) ||
            BorrowSession.ACCEPTED.equals(status)))
            throw(new IllegalStateException("BorrowSession must be REQUESTED||ACCEPTED:"+id));

        reject(bs);
        return;
    }

    /** 拒绝所有同一本书的请求
     */
    //public static void rejectAll(Long borrowableBookId)
    //{
        //DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            //.add(Restrictions.eq("status", BorrowSession.REQUESTED))
            //.createCriteria("book")
            //.add(Restrictions.eq("id", borrowableBookId));
        //List<BorrowSession> requests = (List<BorrowSession>)HiberDao.search(dc);
        //Iterator<BorrowSession> i = requests.iterator();

        //while (i.hasNext())
            //reject(i.next());

        //return;
    //}

    /** 取消借书请求
     * 状态: REQUESTED -> ABORTED
     * @warning 没有身份验证
     * @warning 不能从 ACCEPTED 变成 ABORTED, 因为有拿到书之后欺诈的可能
     * @param id BorrowSession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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

    ///** (特殊)(计划中)双方协定结束会话
     //* 使用于 ACCEPTED 之后的状态, 允许双方通过协商强制取消一个借阅事务, 通常用于丢书之类的状态
     //*/
    //public static boolean terminate(Long id)

  // PROCESS
    /** 请求借书
     * 只有登记为 BorrowableBook 的书可以借
     * @param borrowerId 请求借阅者id
     * @param bookId 目标书籍的id
     * 状态: - -> REQUESTED
     * @exception EntityNotFoundException 当 bookId 不可以出借时
     */
    public static BorrowSession requestBorrow(Long bookId, Long borrowerId)
        throws EntityNotFoundException
    {
        // TODO
        // 黑名单拒绝

        // if existed
        DetachedCriteria dc = DetachedCriteria.forClass(BorrowSession.class)
            .add(Restrictions.ge("status", BorrowSession.REQUESTED))
            .add(Restrictions.lt("status", BorrowSession.CLOSED));
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

    /** 接受借书请求
     * @warning 没有身份验证
     * 状态: REQUESTED -> ACCEPTED
     * @param id BorrowSession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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

    /** 确认借入
     * @warning 没有身份验证, 没有登录密码验证
     * 状态: ACCEPTED -> BORROWED
     * @param id BorrowSession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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
        bs.setTmBorrow(new Date(System.currentTimeMillis()));
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }

    /** 请求还书
     * @warning 没有身份验证, 没有登录密码验证
     * 状态: BORROWED -> RETURNING
     * @param id BorrowSession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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
        bs.setTmReturn(new Date(System.currentTimeMillis()));
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }

    /** 确认还书
     * @warning 没有身份验证, 没有登录密码验证
     * 状态: RETURNING -> CLOSED
     * @param id BorrowSession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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
        bs.setTmReturn(new Date(System.currentTimeMillis()));
        HiberDao.update(bs);

        fireStatusChanged(bs);

        return;
    }
}
