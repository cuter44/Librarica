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

//import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** 注册
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/activate

   <strong>参数</strong>
   uid:long, uid
   code:hex, 激活码
   pass:hex, RSA 加密的 UTF-8 编码的用户登录密码.

   <strong>响应</strong>
   application/json 对象:
   s:hex, 成功时返回 session key

   <strong>例外</strong>
   找不到对应 RSA 私钥返回 Bad Request(400):{flag:"!parameter"}
   pass 不能正确地解密返回 Bad Request(400):{flag:"!parameter"}
   非可激活状态(包括已激活状态)返回 Conflict(409):{flag:"!status"}
   code不正确返回 Frobidden(403):{flag:"!fail"}, 密码/帐户状态不会变动
   uid 不存在返回 Frobidden(403):{flag:"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Activate extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String CODE = "code";
    private static final String PASS = "pass";
    private static final String S = "s";
    private static final String KEY = "key";

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

            byte[] code = HttpUtil.getByteArrayParam(req, CODE);
            if (code == null)
                throw(new MissingParameterException(CODE));

            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass == null)
                throw(new MissingParameterException(PASS));

            // key 不存在
            PrivateKey key = RSAKeyCache.get(uid);
            if (key == null)
                throw(new MissingParameterException(KEY));

            // pass 不正确
            pass = CryptoUtil.RSADecrypt(pass, key);
            if (pass == null)
                throw(new MissingParameterException(PASS));

            HiberDao.begin();

            if (!Authorizer.activate(uid, code, pass))
            {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.println("{\"flag\":\"!fail\"}");
                return;
            }
            // else

            byte[] skey = Authorizer.login(uid, pass);

            HiberDao.commit();

            JSONObject json = new JSONObject();

            json.put(S, CryptoUtil.byteToHex(skey));
            out.println(json.toJSONString());
        }
        catch (IllegalStateException ex)
        {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);

            out.println("{\"flag\":\"!status\"}");
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
