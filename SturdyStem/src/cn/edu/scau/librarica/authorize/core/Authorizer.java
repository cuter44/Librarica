package cn.edu.scau.librarica.authorize.core;

import java.util.Date;
import java.nio.ByteBuffer;
import java.util.Arrays;

import cn.edu.scau.librarica.authorize.dao.*;

import com.github.cuter44.util.dao.*;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.github.cuter44.util.crypto.CryptoUtil;

import org.apache.log4j.Logger;

/** 注册/登入/登出
 */
public class Authorizer
{
    private static Logger logger = Logger.getLogger(Authorizer.class);

    /** session 长度
     */
    private static final int SKEY_LENGTH = 8;
    /** 盐长度
     */
    private static final int SALT_LENGTH = 8;

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

            u.setStatus(User.REGISTER);
            u.setRegDate(new Date(System.currentTimeMillis()));

            byte[] pass = ByteBuffer.allocate(16).put(CryptoUtil.randomBytes(16)).array();

            u.setPass(pass);

            HiberDao.save(u);

            return(u);
        }
        else
        {
            // 已注册
            if (u.getStatus() == User.REGISTER)
                return(u);
            else
                throw(new EntityDuplicatedException("Mail address is occupied."));
        }
    }

    /** 激活帐号并设定登录密码
     * @param uid 注册时邮件发送的 uid
     * @param activateCode 注册时通过邮件发送的验证码
     * @param newPass 登录密码的UTF-8编码, 实际为该用户的 pass 域
     * @return boolean 激活成功返回true, 验证码错误/非REGISTER状态时返回false
     * @exception EntityNotFoundException 当指定的uid不存在时
     */
    public static boolean activate(Long uid, byte[] activateCode, byte[] newPass)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        if (Arrays.equals(u.getPass(),activateCode) || (u.getStatus() == User.REGISTER))
        {
            setPassword(uid, newPass);
            return(true);
        }
        else
        {
            return(false);
        }
    }

    /** 登录
     * @param uid
     * @param pass UTF-8 编码的登录密码
     * @return session key, 除非注销登录, 否则连续登录返回的 session key 相同, 密码不正确返回 null
     * @exception EntityNotFoundException 当指定uid不存在时
     */
    public static byte[] login(Long uid, byte[] pass)
    {
        // 验证密码
        if (!verifyPassword(uid, pass))
            return(null);

        User u = UserMgr.get(uid);
        if (u.getSkey() == null)
        {
            u.setSkey(CryptoUtil.randomBytes(SKEY_LENGTH));
            HiberDao.update(u);
        }

        return(u.getSkey());
    }

    /** 清除 session key, 即注销登录, 通过登录密码
     * @param uid, 需要注销登录的 uid
     * @param pass, UTF-8 编码的登录密码
     * @return boolean 成功返回true, 登录密码不正确返回false
     * @exception EnetityNotFoundException 当指定的uid不存在时
     */
    public static boolean logoutViaPass(Long uid, byte[] pass)
    {
        if (!verifyPassword(uid, pass))
            return(true);

        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        u.setSkey(null);

        HiberDao.update(u);
        return(true);
    }

    /** 清除 session key, 即注销登录
     * @param uid, 需要注销登录的 uid
     * @param skey, session key
     * @return boolean 成功返回true, skey不正确返回false
     * @exception EnetityNotFoundException 当指定的uid不存在时
     */
    public static boolean logoutViaSkey(Long uid, byte[] skey)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            throw(new EntityNotFoundException("No such User:"+uid));

        if (Arrays.equals(u.getSkey(), skey))
        {
            u.setSkey(null);
            HiberDao.update(u);
            return(true);
        }
        else
        {
            return(false);
        }
    }

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
     * @exception NullPointerException 当uid为null
     */
    public static boolean verifyPassword(Long uid, byte[] pass)
    {
        User u = UserMgr.get(uid);
        if (u == null)
            return(false);

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
    {
        User u = UserMgr.get(uid);
        if (u == null)
            return(false);

        if (Arrays.equals(skey, u.getSkey()))
            return(true);
        else
            return(false);
    }
}
