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

/** �޸�����
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /user/passwd

   <strong>����</strong>
   uid:long, uid
   pass:hex, RSA ���ܵ� UTF-8 ������û���¼����.
   newpass:hex, ʹ����ͬ key ���ܵ�������

   <strong>��Ӧ</strong>
   application/json; charset=utf-8:
   {
     "s":hexa, �µ� session key
   }
   <i>���뱻���Ϊ newpass</i>
   <i>ԭsession keyʧЧ</i>

   <strong>����</strong>
   �Ҳ�����Ӧ RSA ˽Կ���� Bad Request(400):{"flag":"!parameter"}
   pass ������ȷ�ؽ��ܷ��� Forbidden(403):{"flag":"!incorrect"}
   ���벻��ȷ���� Forbidden(403):{"flag":"!incorrect"}
   uid �����ڷ��� Frobidden(403):{flag:"!notfound"}
   ��¼��ֹ���� Forbidden(403):{"flag":"!blocked"}, ��ͨ�� /user/search?uid=:uid ȷ���ʺ�״̬
   <i>ֻҪ������������Ͳ�����</i>

   <strong>����</strong>����
 * </pre>
 *
 */
public class Passwd extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String KEY = "key";
    private static final String PASS = "pass";
    private static final String NEWPASS = "newpass";
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

            // key ���
            PrivateKey key = RSAKeyCache.get(uid);
            if (key == null)
                throw(new MissingParameterException(KEY));

            // pass ���
            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass == null)
                throw(new MissingParameterException(PASS));
            pass = CryptoUtil.RSADecrypt(pass, key);
            if (pass == null)
                throw(new PasswordIncorrectException("pass not decrypted."));

            // newpass ���
            byte[] newpass = HttpUtil.getByteArrayParam(req, NEWPASS);
            if (newpass == null)
                throw(new MissingParameterException(NEWPASS));
            newpass = CryptoUtil.RSADecrypt(newpass, key);
            if (newpass == null)
                throw(new PasswordIncorrectException("newpass not decrypted."));

            HiberDao.begin();

            Authorizer.passwd(uid, pass, newpass);

            byte[] skey = Authorizer.login(uid, newpass);

            HiberDao.commit();

            JSONObject json = new JSONObject();

            json.put(S, CryptoUtil.byteToHex(skey));
            out.println(json.toJSONString());
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
