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

/** �ǳ�
 * �ǳ�����������ն��ϵĲ���ƾ֤
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /user/logout

   <strong>����</strong>
   uid:long, uid
   ���²���������һ��:
   s:hex, session key
   pass:hex, RSA ���ܵ� UTF-8 ������û���¼����.

   <strong>��Ӧ</strong>
   application/json ����:
   flag:string, �ɹ�ʱ���� success

   <strong>����</strong>
   �Ҳ�����Ӧ RSA ˽Կ���� Bad Request(400):{flag:"!parameter"}
   pass ������ȷ�ؽ��ܷ��� Bad Request(400):{flag:"!parameter"}
   ִ��ʧ�ܷ��� Forbidden(403):{flag:"!fail"}
   uid �����ڷ��� Forbidden(403):{flag:"!notfound"}

   <strong>����</strong>����
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
                // key �춨
                PrivateKey key = RSAKeyCache.get(uid);
                if (key == null)
                    throw(new MissingParameterException(KEY));

                // pass �춨
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
