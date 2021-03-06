package cn.edu.scau.librarica.authorize.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.security.PrivateKey;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.github.cuter44.util.crypto.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 登出
 * 登出会清除所有终端上的操作凭证
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/logout

   <strong>参数</strong>
   uid:long, uid
   以下参数的其中一个:
   s:hex, session key
   pass:hex, RSA 加密的 UTF-8 编码的用户登录密码.

   <strong>响应</strong>
   application/json 对象:
   flag:string, 成功时返回 success

   <strong>例外</strong>
   找不到对应 RSA 私钥返回 Bad Request(400):{flag:"!parameter"}
   pass 不能正确地解密返回 Bad Request(400):{flag:"!parameter"}
   执行失败返回 Forbidden(403):{flag:"!fail"}
   uid 不存在返回 Forbidden(403):{flag:"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Logout extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String KEY = "key";
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            JSONObject json = new JSONObject();

            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            HiberDao.begin();

            // logout by session key
            byte[] skey = HttpUtil.getByteArrayParam(req, S);
            if (skey != null)
            {
                Authorizer.logoutViaSkey(uid, skey);

                HiberDao.commit();
                return;
            }

            // else
            // logout by password
            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass != null)
            {
                // key 检定
                PrivateKey key = RSAKeyCache.get(uid);
                if (key == null)
                    throw(new MissingParameterException(KEY));

                // pass 检定
                pass = CryptoUtil.RSADecrypt(pass, key);
                if (pass == null)
                    throw(new PasswordIncorrectException(PASS));

                Authorizer.logoutViaPass(uid, pass);

                HiberDao.commit();

                return;
            }
            // else
            // parameter missing
            throw(new MissingParameterException(""));
        }
        catch (PasswordIncorrectException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"flag\":\"!incorrect\"}");
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"flag\":\"!notfound\"}");

        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"flag\":\"!parameter\"}");
        }
        catch (Exception ex)
        {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.log("", ex);
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
