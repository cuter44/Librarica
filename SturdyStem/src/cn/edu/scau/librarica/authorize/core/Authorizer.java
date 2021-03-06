package cn.edu.scau.librarica.authorize.core;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
//import java.util.concurrent.LinkedBlockingQueue;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.crypto.CryptoUtil;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.apache.log4j.Logger;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 注册/登入/登出
 */
public class Authorizer
{
  // ANNOUNCER
    /** 帐号状态监听器
     * 对于注册(重新发送邮件)/激活/封号/注销事件进行回调
     */
    public static interface StatusChangedListener
    {
        public abstract void onStatusChanged(User u);
    }

    private static ArrayList<StatusChangedListener> statusChangedListeners = new ArrayList<StatusChangedListener>();

    public static synchronized void addListener(StatusChangedListener l)
    {
        statusChangedListeners.add(l);

        return;
    }

  // EVENT-DISPATCH
    /**
     * For tomcat preventing thread forking, it is impossible to use a asynochronized diapatching queue.
     * So time-consuming task or heavy concurrent traffic may TAKE DOWN the server, and I can do nothing about it.
     * The only workaround is: if I have my own server, I can deploy another process to deal with it.
     */
    private static void fireStatusChanged(User u)
    {
        for (int i=0; i<statusChangedListeners.size(); i++)
        {
            try
            {
                statusChangedListeners.get(i).onStatusChanged(u);
            }
            catch (Throwable ex)
            {
                ex.printStackTrace();
            }
        }
    }

  // CONFIG
    /** seems no use...
     */
    private static Logger logger = Logger.getLogger(Authorizer.class);

    /** session 长度
     */
    private static final int SKEY_LENGTH = 4;
    /** 盐长度
     */
    private static final int SALT_LENGTH = 8;

  // BASE
    /** 变更密码
     * @param uid
     * @param pass 新密码, UTF-8编码
     * @exception EntityNotFoundException 指定的uid不存在时
     */
    public static void setPassword(Long uid, byte[] pass)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        byte[] salt = CryptoUtil.randomBytes(SALT_LENGTH);
        u.setSalt(salt);

        byte[] buf = ByteBuffer.allocate(pass.length + salt.length)
            .put(pass)
            .put(salt)
            .array();
        u.setPass(CryptoUtil.MD5Digest(buf));

        HiberDao.update(u);
        return;
    }

    /** 验证登录密码
     * @param uid
     * @param pass 登录密码, UTF-8编码
     * @return boolean 密码正确与否
     * @exception IllegalArgumentException 当用户名或密码为空时
     * @exception EntityNotFoundException 当 uid 不存在时
     */
    public static boolean verifyPassword(Long uid, byte[] pass)
        throws IllegalArgumentException, EntityNotFoundException
    {
        if (uid == null || pass == null)
            throw(new IllegalArgumentException());

        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        byte[] salt = u.getSalt();
        byte[] buf = ByteBuffer.allocate(pass.length + salt.length)
            .put(pass)
            .put(salt)
            .array();
        buf = CryptoUtil.MD5Digest(buf);

        if (Arrays.equals(u.getPass(), buf))
            return(true);
        else
            return(false);
    }

    /** 验证 session key
     * @param uid
     * @param pass 登录密码, UTF-8编码
     * @return boolean session key 正确与否
     * @exception NullPointerException 当uid为null
     */
    public static boolean verifySkey(Long uid, byte[] skey)
        throws IllegalArgumentException, EntityNotFoundException
    {
        if (uid == null || skey == null)
            throw(new IllegalArgumentException());

        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        if (Arrays.equals(skey, u.getSkey()))
            return(true);
        else
            return(false);
    }

    /** 清除 session key, 即注销登录, 通过登录密码
     * @param uid, 需要注销登录的 uid
     * @param pass, UTF-8 编码的登录密码
     * @exception EntityNotFoundException 当指定的uid不存在时
     * @exception PasswordIncorrectException 当pass错误时
     */
    public static void logoutViaPass(Long uid, byte[] pass)
    {
        if (!verifyPassword(uid, pass))
            throw(new PasswordIncorrectException());

        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        u.setSkey(null);

        HiberDao.update(u);
    }

    /** 清除 session key, 即注销登录
     * @param uid, 需要注销登录的 uid
     * @param skey, session key
     * @exception EnetityNotFoundException 当指定的uid不存在时
     * @exception PasswordIncorrectException 当skey不正确时
     */
    public static void logoutViaSkey(Long uid, byte[] skey)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        if (!Arrays.equals(u.getSkey(), skey))
            throw(new PasswordIncorrectException());

        u.setSkey(null);
        HiberDao.update(u);

        return;
    }

  // EX
    /** 注册新帐号
     * @param mail 邮件地址
     * @return 对于未注册过的地址或注册但未激活地址返回状态为 REGISTER 的 User 对象
     * @exception EntityDuplicatedException 除上述状况时
     */
    public static User register(String mail)
    {
        User u = UserMgr.forMail(mail);

        if (u == null)
        {
            // 未注册
            u = new User(mail);

            u.setStatus(User.REGISTERED);
            u.setUserType(User.INDIVIDUAL);
            u.setRegDate(new Date(System.currentTimeMillis()));

            byte[] pass = ByteBuffer.allocate(16).put(CryptoUtil.randomBytes(16)).array();

            u.setPass(pass);

            HiberDao.save(u);

            fireStatusChanged(u);
            return(u);
        }
        else
        {
            // 已注册
            if (User.REGISTERED.equals(u.getStatus()))
            {
                fireStatusChanged(u);
                return(u);
            }
            else
                throw(new EntityDuplicatedException("Mail address is occupied:"+mail));
        }
    }

    /** 激活帐号并设定登录密码
     * @param uid 注册时邮件发送的 uid
     * @param activateCode 注册时通过邮件发送的验证码
     * @param newPass 登录密码的UTF-8编码, 实际为该用户的 pass 域
     * @exception EntityNotFoundException 当指定的uid不存在时
     */
    public static void activate(Long uid, byte[] activateCode, byte[] newPass)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        if (!User.REGISTERED.equals(u.getStatus()))
            throw(new LoginBlockedException());

        if (!Arrays.equals(u.getPass(), activateCode))
            throw(new PasswordIncorrectException());

        setPassword(uid, newPass);

        u.setStatus(User.ACTIVATED);
        HiberDao.update(u);
        fireStatusChanged(u);

        return;
    }

    /** 登录
     * @param uid
     * @param pass UTF-8 编码的登录密码
     * @return session key, 除非注销登录, 否则连续登录返回的 session key 相同, 密码不正确返回 null
     * @exception EntityNotFoundException 当指定uid不存在时
     * @exception PasswordIncorrectException 当pass不正确时
     */
    public static byte[] login(Long uid, byte[] pass)
        throws PasswordIncorrectException, EntityNotFoundException
    {
        // 验证密码
        if (!verifyPassword(uid, pass))
            throw(new PasswordIncorrectException());

        User u = UserMgr.get(uid);
        if (u.getSkey() == null)
        {
            u.setSkey(CryptoUtil.randomBytes(SKEY_LENGTH));
            HiberDao.update(u);
        }

        return(u.getSkey());
    }

    public static void passwd(Long uid, byte[] pass, byte[] newpass)
        throws PasswordIncorrectException, EntityNotFoundException
    {
        // 验证密码
        if (!verifyPassword(uid, pass))
            throw(new PasswordIncorrectException());

        setPassword(uid, newpass);
        logoutViaPass(uid, newpass);

        return;
    }
}
