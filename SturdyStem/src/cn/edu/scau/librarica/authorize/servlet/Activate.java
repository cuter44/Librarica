package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import java.security.PrivateKey;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.github.cuter44.util.crypto.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getByteArrayParam;
import com.alibaba.fastjson.*;

//import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 激活帐号
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/activate.api

   <strong>参数</strong>
   uid:long, uid
   code:hex, 激活码
   pass:hex, RSA 加密的 UTF-8 编码的用户登录密码.

   <strong>响应</strong>
   application/json, class=authorize.dao.User(private)
   @see J#writeUserPrivate

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Activate extends HttpServlet
{
    private static final String UID = "uid";
    private static final String CODE = "code";
    private static final String PASS = "pass";

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
            Long uid        = (Long)        notNull(getLongParam     (req, UID));
            byte[] code     = (byte[])      notNull(getByteArrayParam(req, CODE));
            byte[] pass     = (byte[])      notNull(getByteArrayParam(req, PASS));
            PrivateKey key  = (PrivateKey)  notNull(RSAKeyCache.get(uid));

            pass = CryptoUtil.RSADecrypt(pass, key);
            if (pass == null)
                throw(new UnauthorizedException("pass not decrypted."));

            HiberDao.begin();

            Authorizer.activate(uid, code, pass);

            HiberDao.commit();

            req.getRequestDispatcher("login.api").forward(req, resp);
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
