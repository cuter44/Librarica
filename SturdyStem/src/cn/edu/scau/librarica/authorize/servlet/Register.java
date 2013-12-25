package cn.edu.scau.librarica.authorize.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.github.cuter44.util.crypto.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.util.mail.MailProvider;
import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** ע��
 * <pre style="font-size:12px">

   <strong>����</strong>
   POST /user/register

   <strong>����</strong>
   mail:string, �ʼ���ַ

   <strong>��Ӧ</strong>
   application/json ����:
   mail:string, �ɹ�ʱ����ע����ʼ���ַ
   uid:long, �ɹ�ʱ���ط����UID

   <strong>����</strong>
   �ʼ���ַ�ѱ�ʹ��ʱ���� Bad Request(400): {"flag":"!duplicated"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class Register extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String MAIL = "mail";
    private static final String UID = "uid";

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
            String mail = HttpUtil.getParam(req, MAIL);
            if (mail == null)
                throw(new MissingParameterException(MAIL));

            HiberDao.begin();

            User u = Authorizer.register(mail);

            // �����ʼ�
            MailProvider m = (MailProvider)Class.forName(
                Configurator.get("librarica.mail.RegisterMailProvider")
            ).getConstructor().newInstance();
            m.sendMail(req);

            HiberDao.commit();

            json.put(MAIL, u.getMail());
            json.put(UID, u.getId());

            out.println(json.toJSONString());
        }
        catch (EntityDuplicatedException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!duplicated");
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