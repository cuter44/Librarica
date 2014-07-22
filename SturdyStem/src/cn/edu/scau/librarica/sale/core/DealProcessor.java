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

/** 交易流程处理器
 */
public class DealProcessor
{
  // ANNOUNCER
    /** 状态监听器
     * 当会话状态发生变更时(包括创建)将会通知所有注册的监听器
     * 除非另起线程, 否则请勿使用阻塞
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
    /** 排他处理器
     * 连接到 rejectAll() 上;
     * 用于排他拒绝对同一本书的其他借阅请求
     * 仅作用于个人用户出售的书
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

    /** 下架处理器
     * 用于将接受订单的书从可卖出状态清除
     * 仅作用于个人用户
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

    /** 过继处理器
     * 用于将卖出的书过继到买家名下
     * 仅作用于个人用户出售的书
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

    /** 拒绝请求
     * 状态: REQUESTED | ACCEPTED -> REJECTED
     * @warning 没有身份验证保护
     * @param id BuySession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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

    /** 取消请求
     * 状态: REQUESTED -> ABORTED
     * @warning 没有身份验证
     * @warning 不能从 ACCEPTED 变成 ABORTED,
     * @param id BuySession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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
    /** 请求
     * 只有登记为 SalableBook 的书可以借
     * 对于同一本书的请求只会返回同一个活动实例, 已经拒绝/撤销/结单的实例不属于活动实例
     * @param borrowerId 请求借阅者id
     * @param bookId 目标书籍的id
     * 状态: - -> REQUESTED
     * @exception EntityNotFoundException 当 bookId 不在出售时
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

    /** 接受请求
     * @warning 没有身份验证
     * 状态: REQUESTED -> ACCEPTED
     * @param id BuySession id
     * @exception IllegalStateException 当不是 REQUESTED 状态时
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

    /** 关闭购买会话
     * @warning 没有身份验证
     * 状态: ACCEPTED -> CLOSED
     * @param id BuySession id
     * @exception IllegalStateException 当不是 ACCEPTED 状态时
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
