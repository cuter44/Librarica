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

//import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** 注册
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/register

   <strong>参数</strong>
   mail:string(60), 邮件地址

   <strong>响应</strong>
   application/json 对象:
   mail:string, 成功时返回注册的邮件地址
   uid:long, 成功时返回分配的UID

   <strong>例外</strong>
   邮件地址已被使用时返回 Forbidden(403): {"flag":"!duplicated"}

   <strong>样例</strong>暂无
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
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        try
        {
            String mail = HttpUtil.getParam(req, MAIL);
            if (mail == null)
                throw(new MissingParameterException(MAIL));

            HiberDao.begin();

            User u = Authorizer.register(mail);

            HiberDao.commit();

            JSONObject json = new JSONObject();
            json.put(MAIL, u.getMail());
            json.put(UID, u.getId());

            out.println(json.toJSONString());
        }
        catch (EntityDuplicatedException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!duplicated\"}");
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
