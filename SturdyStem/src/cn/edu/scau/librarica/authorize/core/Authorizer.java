package cn.edu.scau.librarica.authorize.core;

import java.util.Date;
import java.nio.ByteBuffer;
import java.util.Arrays;

import cn.edu.scau.librarica.authorize.dao.User;

import com.github.cuter44.util.dao.HiberDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import com.github.cuter44.util.crypto.CryptoUtil;

import org.apache.log4j.Logger;

public class Authorizer
{
    private static Logger logger = Logger.getLogger(Authorizer.class);

    private static final int SKEY_LENGTH = 16;
    private static final int SALT_LENGTH = 8;

    public static String register(String mail)
    {
        try
        {
            User u = UserMgr.forMail(mail);
            if (u == null)
            {
                u = new User(mail);

                u.setStatus(User.REGISTER);
                u.setRegDate(new Date(System.currentTimeMillis()));

                byte[] salt = CryptoUtil.randomBytes(SALT_LENGTH);
                byte[] pass = ByteBuffer.allocate(16).put(CryptoUtil.MD5Digest(salt)).array();
                u.setPass(pass);

                HiberDao.save(u);

                return("success");
            }
            else
            {
                if (u.getStatus() == User.REGISTER)
                    return("success");
                else
                    return("!occupied");
            }
        }
        catch (ConstraintViolationException ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!duplicated");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }

    public static String activate(Long uid, String activateCode, byte[] newPass)
    {
        try
        {
            User u = (User)HiberDao.get(User.class, uid);
            if (u == null)
                return("!nomatch");
            if (u.getStatus() != User.REGISTER)
                return("!activated");

            byte[] aPass = CryptoUtil.hexToBytes(activateCode);
            byte[] pass = u.getPass();

            if (Arrays.equals(pass, aPass))
            {
                String flag = setPassword(uid, newPass);
                return(flag);
            }
            else
            {
                return("!code");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }

    public static String login(Long uid, byte[] rawPlainPass)
    {
        try
        {
            String flag = verifyPassword(uid, rawPlainPass);

            if (!flag.equals("success"))
                return(flag);

            User u = (User)HiberDao.get(User.class, uid);
            if (u.getSkey() == null)
            {
                byte[] skey = CryptoUtil.randomBytes(SKEY_LENGTH);
                HiberDao.update(u);
            }

            return("success");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }

    public static String clearSkey(Long uid, byte[] rawPlainPass)
    {
        try
        {
            String flag = verifyPassword(uid, rawPlainPass);

            if (!flag.equals("success"))
                return(flag);

            User u = (User)HiberDao.get(User.class, uid);
            u.setSkey(null);

            HiberDao.update(u);
            return("success");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }

    public static String setPassword(Long uid, byte[] rawPlainPass)
    {
        try
        {
            User u = (User)HiberDao.get(User.class, uid);
            if (u == null)
                return("!nomatch");

            byte[] salt = CryptoUtil.randomBytes(SALT_LENGTH);
            u.setSalt(salt);

            byte[] buf = ByteBuffer.allocate(rawPlainPass.length + salt.length)
                .put(rawPlainPass)
                .put(salt)
                .array();
            byte[] pass = CryptoUtil.MD5Digest(buf);
            u.setPass(pass);

            return("success");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }

    public static String verifyPassword(Long uid, byte[] rawPlainPass)
    {
        try
        {
            User u = (User)HiberDao.get(User.class, uid);
            if (u == null)
                return("!nomatch");
            byte[] pass = u.getPass();

            if (Arrays.equals(rawPlainPass, pass))
                return("success");
            else
                return("!incorrect");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }

    public static String verifyLogin(Long uid, String strSkey)
    {
        try
        {
            User u = (User)HiberDao.get(User.class, uid);
            if (u == null)
                return("!nomatch");

            byte[] skey = u.getSkey();
            if (skey == null)
                return("!incorrect");

            byte[] aSkey = CryptoUtil.hexToBytes(strSkey);

            if (Arrays.equals(skey, aSkey))
                return("success");
            else
                return("!incorrect");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("", ex);
            return("!error");
        }
    }
}
