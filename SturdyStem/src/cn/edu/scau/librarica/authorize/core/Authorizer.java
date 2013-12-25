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

/** ע��/����/�ǳ�
 */
public class Authorizer
{
    private static Logger logger = Logger.getLogger(Authorizer.class);

    /** session ����
     */
    private static final int SKEY_LENGTH = 8;
    /** �γ���
     */
    private static final int SALT_LENGTH = 8;

    /** ע�����ʺ�
     * @param mail �ʼ���ַ
     * @return ����δע����ĵ�ַ��ע�ᵫδ�����ַ����״̬Ϊ REGISTER �� User ����
     * @exception EntityDuplicatedException ������״��ʱ
     */
    public static User register(String mail)
    {
        User u = UserMgr.forMail(mail);

        if (u == null)
        {
            // δע��
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
            // ��ע��
            if (u.getStatus() == User.REGISTER)
                return(u);
            else
                throw(new EntityDuplicatedException("Mail address is occupied."));
        }
    }

    /** �����ʺŲ��趨��¼����
     * @param uid ע��ʱ�ʼ����͵� uid
     * @param activateCode ע��ʱͨ���ʼ����͵���֤��
     * @param newPass ��¼�����UTF-8����, ʵ��Ϊ���û��� pass ��
     * @return boolean ����ɹ�����true, ��֤�����/��REGISTER״̬ʱ����false
     * @exception EntityNotFoundException ��ָ����uid������ʱ
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

    /** ��¼
     * @param uid
     * @param pass UTF-8 ����ĵ�¼����
     * @return session key, ����ע����¼, ����������¼���ص� session key ��ͬ, ���벻��ȷ���� null
     * @exception EntityNotFoundException ��ָ��uid������ʱ
     */
    public static byte[] login(Long uid, byte[] pass)
    {
        // ��֤����
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

    /** ��� session key, ��ע����¼, ͨ����¼����
     * @param uid, ��Ҫע����¼�� uid
     * @param pass, UTF-8 ����ĵ�¼����
     * @return boolean �ɹ�����true, ��¼���벻��ȷ����false
     * @exception EnetityNotFoundException ��ָ����uid������ʱ
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

    /** ��� session key, ��ע����¼
     * @param uid, ��Ҫע����¼�� uid
     * @param skey, session key
     * @return boolean �ɹ�����true, skey����ȷ����false
     * @exception EnetityNotFoundException ��ָ����uid������ʱ
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

    /** �������
     * @param uid
     * @param pass ������, UTF-8����
     * @exception EntityNotFoundException ָ����uid������ʱ
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

    /** ��֤��¼����
     * @param uid
     * @param pass ��¼����, UTF-8����
     * @return boolean ������ȷ���
     * @exception NullPointerException ��uidΪnull
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

    /** ��֤ session key
     * @param uid
     * @param pass ��¼����, UTF-8����
     * @return boolean session key ��ȷ���
     * @exception NullPointerException ��uidΪnull
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
