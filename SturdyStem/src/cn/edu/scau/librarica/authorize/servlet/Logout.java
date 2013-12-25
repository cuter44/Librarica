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
   找不到对应 RSA 私钥返回 Bad Request(400):{flag:"!key"}
   pass 不能正确地解密返回 Bad Request(400):{flag:"!pass"}
   执行失败返回Bad Request(400):{flag:"!fail"}
   uid 不存在返回 Bad Request(400):{flag:"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Logout extends HttpServlet
{
    private static final String FLAG = "flag";
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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        JSONObject json = new JSONObject();

        try
        {
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            HiberDao.begin();

            // logout by session key
            byte[] skey = HttpUtil.getByteArrayParam(req, S);
            if (skey != null)
            {
                if (Authorizer.logoutViaSkey(uid, skey))
                {
                    json.put(FLAG, "success");
                }
                else
                {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.put(FLAG, "!fail");
                }

                HiberDao.commit();
                out.println(json.toJSONString());
                return;
            }

            // else
            // logout by password
            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass != null)
            {
                // key 不存在
                PrivateKey key = RSAKeyCache.get(uid);
                if (key == null)
                {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    json.put(FLAG, "!key");
                    out.println(json.toJSONString());

                    return;
                }
                // pass 不正确
                pass = CryptoUtil.RSADecrypt(pass, key);
                if (pass == null)
                {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    json.put(FLAG, "!pass");
                    out.println(json.toJSONString());

                    return;
                }
                // else
                if (Authorizer.logoutViaPass(uid, pass))
                {
                    json.put(FLAG, "success");
                }
                else
                {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    json.put(FLAG, "!fail");
                }

                HiberDao.commit();
                out.println(json.toJSONString());

                return;
            }
            // else
            // parameter missing
            throw(new MissingParameterException(""));
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!notfound");
            out.println(json.toJSONString());
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!parameter");
            out.println(json.toJSONString());
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
