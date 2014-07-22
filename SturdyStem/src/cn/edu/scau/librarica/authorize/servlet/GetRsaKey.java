package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import java.util.Arrays;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.crypto.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** 获取 RSA 公钥
 * 生成的密钥是一次一密, 一人一密的, 生成的密钥有生存期(由服务器端配置决定)
 * 可以通过指定别人的 uid 扰乱他人的密钥, 这可能是一个漏洞.
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /security/get-rsa-key.api

   <strong>参数</strong>
   uid:long, uid, 可以指定一个负的随机数来为未注册用户保存一个key

   <strong>响应</strong>
   application/json 对象:
   m:hex, modulus
   e:hex, public exponent

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class GetRsaKey extends HttpServlet
{
    private static final String UID = "uid";
    private static final String M = "m";
    private static final String E = "e";

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
            Long uid = (Long)notNull(getLongParam(req, UID));

            RSAPublicKey pk = (RSAPublicKey)RSAKeyCache.genKey(uid);

            JSONObject json = new JSONObject();

            byte[] m = pk.getModulus().toByteArray();
            if (m[0] == 0)
                m = Arrays.copyOfRange(m, 1, m.length);
            byte[] e = pk.getPublicExponent().toByteArray();
            if (e[0] == 0)
                e = Arrays.copyOfRange(e, 1, e.length);

            json.put(M, CryptoUtil.byteToHex(m));
            json.put(E, CryptoUtil.byteToHex(e));

            out.println(json.toJSONString());
        }
        catch (Exception ex)
        {
            req.setAttribute(Constants.KEY_EXCEPTION, ex);
            req.getRequestDispatcher(Constants.URI_ERROR_HANDLER).forward(req, resp);
        }

        return;
    }
}
