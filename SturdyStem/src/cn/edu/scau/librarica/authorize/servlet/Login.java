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

/** 登录
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/login

   <strong>参数</strong>
   uid:long, uid
   pass:hex, RSA 加密的 UTF-8 编码的用户登录密码.

   <strong>响应</strong>
   application/json 对象:
   s:hex 成功时返回 session key

   <strong>例外</strong>
   找不到对应 RSA 私钥返回 Bad Request(400):{"flag":"!parameter"}
   pass 不能正确地解密返回 Forbidden(403):{"flag":"!incorrect"}
   密码不正确返回 Forbidden(403):{"flag":"!incorrect"}
   uid 不存在返回 Frobidden(403):{flag:"!notfound"}
   登录禁止返回 Forbidden(403):{"flag":"!blocked"}, 请通过 /user/search?uid=:uid 确定帐号状态

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Login extends HttpServlet
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
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            // key 检定
            PrivateKey key = RSAKeyCache.get(uid);
            if (key == null)
                throw(new MissingParameterException(KEY));

            // pass 检定
            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass == null)
                throw(new MissingParameterException(PASS));
            pass = CryptoUtil.RSADecrypt(pass, key);
            if (pass == null)
                throw(new PasswordIncorrectException("pass not decrypted."));

            HiberDao.begin();

            byte[] skey = Authorizer.login(uid, pass);

            HiberDao.commit();

            JSONObject json = new JSONObject();

            json.put(S, CryptoUtil.byteToHex(skey));
            out.println(json.toJSONString());
        }
        catch (PasswordIncorrectException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"flag\":\"!incorrect\"}");
            HiberDao.rollback();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"flag\":\"!notfound\"}");
            HiberDao.rollback();
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"flag\":\"!parameter\"}");
            HiberDao.rollback();
        }
        catch (Exception ex)
        {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.log("", ex);
            HiberDao.rollback();
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
