package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import java.security.PrivateKey;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.crypto.*;
import com.github.cuter44.util.servlet.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getByteArrayParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 登出
 * 登出会清除所有终端上的操作凭证
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/logout.api

   <strong>参数</strong>
   uid:long, uid
   以下参数的其中一个:
   s:hex, session key
   pass:hex, RSA 加密的 UTF-8 编码的用户登录密码.

   <strong>响应</strong>
    application/json class=authorize.dao.User(private)
    @see J#writeUserPrivate


   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Logout extends HttpServlet
{
    private static final String UID = "uid";
    private static final String PASS = "pass";
    private static final String S = "s";

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
            Long uid = (Long)notNull(getLongParam(req, UID));

            HiberDao.begin();

            // logout by session key
            byte[] skey = getByteArrayParam(req, S);
            if (skey != null)
            {
                Authorizer.logoutViaSkey(uid, skey);

                HiberDao.commit();

                J.writeUserPrivate(UserMgr.get(uid), resp);

                return;
            }

            // else
            // logout by password
            byte[] pass = getByteArrayParam(req, PASS);
            if (pass != null)
            {
                // key 检定
                PrivateKey key  = (PrivateKey)  notNull(RSAKeyCache.get(uid));
                pass            = (byte[])      notNull(CryptoUtil.RSADecrypt(pass, key));

                Authorizer.logoutViaPass(uid, pass);

                HiberDao.commit();

                J.writeUserPrivate(UserMgr.get(uid), resp);

                return;
            }
            // else
            // parameter missing
            throw(new MissingParameterException(""));
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
