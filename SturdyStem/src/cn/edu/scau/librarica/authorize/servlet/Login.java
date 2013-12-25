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

/** ��¼
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /user/login

   <strong>����</strong>
   uid:long, uid
   pass:hex, RSA ���ܵ� UTF-8 ������û���¼����.

   <strong>��Ӧ</strong>
   application/json ����:
   s:hex �ɹ�ʱ���� session key

   <strong>����</strong>
   �Ҳ�����Ӧ RSA ˽Կ���� Bad Request(400):{flag:"!key"}
   pass ������ȷ�ؽ��ܷ��� Bad Request(400):{flag:"!pass"}
   ִ��ʧ�ܷ���Bad Request(400):{flag:"!fail"}
   uid �����ڷ��� Bad Request(400):{flag:"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class Login extends HttpServlet
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

            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass == null)
                throw(new MissingParameterException(PASS));

            // key ������
            PrivateKey key = RSAKeyCache.get(uid);
            if (key == null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                json.put(FLAG, "!key");
                out.println(json.toJSONString());

                return;
            }
            // pass ����ȷ
            pass = CryptoUtil.RSADecrypt(pass, key);
            if (pass == null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                json.put(FLAG, "!pass");
                out.println(json.toJSONString());

                return;
            }

            HiberDao.begin();

            byte[] skey = Authorizer.login(uid, pass);
            if (skey == null)
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                json.put(FLAG, "!fail");
                out.println(json.toJSONString());

                return;
            }

            HiberDao.commit();

            json.put(S, CryptoUtil.byteToHex(skey));
            out.println(json.toJSONString());
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
