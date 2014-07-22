package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import java.security.PrivateKey;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.crypto.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getByteArrayParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 修改密码
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/passwd.api

   <strong>参数</strong>
   uid:long, uid
   pass:hex, RSA 加密的 UTF-8 编码的用户登录密码.
   newpass:hex, 使用相同 key 加密的新密码

   <strong>响应</strong>
   application/json class=authorize.dao.User(private)
   @see J#writeUserPrivate
   <i>密码被变更为 newpass</i>
   <i>原session key失效</i>

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler
    <i>只要发生例外密码就不会变更</i>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Passwd extends HttpServlet
{
    private static final String UID = "uid";
    private static final String PASS = "pass";
    private static final String NEWPASS = "newpass";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        req.setCharacterEncoding("utf-8");

        try
        {
            Long        uid =       (Long)          notNull(getLongParam(req, UID));
            PrivateKey  key =       (PrivateKey)    notNull(RSAKeyCache.get(uid));
            byte[]      pass =      (byte[])        notNull(getByteArrayParam(req, PASS));

            pass =                  (byte[])        notNull(CryptoUtil.RSADecrypt(pass, key));

            byte[]      newpass =   (byte[])        notNull(getByteArrayParam(req, NEWPASS));

            newpass =               (byte[])        notNull(CryptoUtil.RSADecrypt(newpass, key));

            HiberDao.begin();

            Authorizer.passwd(uid, pass, newpass);
            Authorizer.login(uid, newpass);

            HiberDao.commit();

            J.writeUserPrivate(UserMgr.get(uid), resp);
        }
        catch (Exception ex)
        {
            req.setAttribute(Constants.KEY_EXCEPTION, ex);
            req.getRequestDispatcher(Constants.URI_ERROR_HANDLER).forward(req, resp);
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
