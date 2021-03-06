package cn.edu.scau.librarica.authorize.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.Arrays;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.github.cuter44.util.crypto.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** 获取 RSA 公钥
 * 生成的密钥是一次一密, 一人一密的, 生成的密钥有生存期(由服务器端配置决定)
 * 可以通过指定别人的 uid 扰乱他人的密钥, 这可能是一个漏洞.
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /security/get-rsa-key

   <strong>参数</strong>
   uid:long, uid

   <strong>响应</strong>
   application/json 对象:
   m:hex, modulus
   e:hex, public exponent

   <strong>例外</strong>
   uid不存在时返回Frobidden(403):{flag:"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class GetRsaKey extends HttpServlet
{
    private static final String FLAG = "flag";
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
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            RSAPublicKey pk = (RSAPublicKey)RSAKeyCache.genKey(uid);

            JSONObject json = new JSONObject();

            //this.getServletContext().log(pk.getPublicExponent().toString(16));
            //this.getServletContext().log(pk.getModulus().toString(16));
            //this.getServletContext().log(CryptoUtil.byteToHex(pk.getPublicExponent().toByteArray()));
            //this.getServletContext().log(CryptoUtil.byteToHex(pk.getModulus().toByteArray()));
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

        return;
    }
}
