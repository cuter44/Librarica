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
import org.apache.log4j.Logger;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 身份验证及访问控制模块
 * 提供基于帐号密码的身份验证实现:
 * * 注册(register)
 *   要求用户提供邮件地址, 然后为该邮件地址分配用户ID, 及发送激活邮件
 * * 激活(activate)
 *   用户获得并回答(从其他信道获得的)激活码, 申请密钥对并指定(RSA加密的)登录密码
 *   用户给定的密码在服务器端解密, 并与随机生成的盐拼接, 散列后写入数据库
 * * 登录(login)
 *   用户给出自己的ID(邮件, username 或者 userid), 以及(RSA加密的)密码
 *   服务器端按以上方式验证密码是否匹配, 如匹配发给session key, 作为一般业务的凭证使用, 允许明文传输
 *   session key会在显式注销后失效, 多端登录下, 其他客户端的session key将失去作用
 * * 修改密码(passwd)
 *   用户申请密钥对, 加密并指定新旧两个登录密码
 *   服务器按以上方式验证密码是否匹配, 如匹配则变更密码, 并重置session key
 * * 注销
 *   用户给出自己的id, 以及session key或密码, 要求吊销session key
 *   服务器验证session key或密码, 如匹配, 吊销当前用户的session key
 *
 * 以拦截器的方式提供基于密码或session key的验证服务
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
     * @exception UnauthorizedException 当pass错误时
     */
    public static void logoutViaPass(Long uid, byte[] pass)
    {
        if (!verifyPassword(uid, pass))
            throw(new UnauthorizedException());

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
     * @exception UnauthorizedException 当skey不正确时
     */
    public static void logoutViaSkey(Long uid, byte[] skey)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        if (!Arrays.equals(u.getSkey(), skey))
            throw(new UnauthorizedException());

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
            u = new User(mail);

            u.setStatus(User.REGISTERED);
            u.setUserType(User.INDIVIDUAL);
            u.setRegDate(new Date(System.currentTimeMillis()));

            byte[] pass = ByteBuffer.allocate(16).put(CryptoUtil.randomBytes(16)).array();

            u.setPass(pass);

            HiberDao.save(u);
        }

        if (User.REGISTERED.equals(u.getStatus()))
        {
            fireStatusChanged(u);
        }
        else
            throw(new EntityDuplicatedException("Mail address is occupied:"+mail));

        return(u);
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
            throw(new UnauthorizedException());

        setPassword(uid, newPass);

        u.setStatus(User.ACTIVATED);
        HiberDao.update(u);
        fireStatusChanged(u);

        return;
    }

    /** 登录
     * @param uid
     * @param pass UTF-8 编码的登录密码
     * @exception EntityNotFoundException 当指定uid不存在时
     * @exception UnauthorizedException 当pass不正确时
     */
    public static void login(Long uid, byte[] pass)
        throws UnauthorizedException, EntityNotFoundException
    {
        // 验证密码
        if (!verifyPassword(uid, pass))
            throw(new UnauthorizedException());

        User u = UserMgr.get(uid);
        if (u.getSkey() == null)
        {
            u.setSkey(CryptoUtil.randomBytes(SKEY_LENGTH));
            HiberDao.update(u);
        }

        return;
    }

    public static void passwd(Long uid, byte[] pass, byte[] newpass)
        throws UnauthorizedException, EntityNotFoundException
    {
        // 验证密码
        if (!verifyPassword(uid, pass))
            throw(new UnauthorizedException());

        setPassword(uid, newpass);
        logoutViaPass(uid, newpass);

        return;
    }
}
