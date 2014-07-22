package cn.edu.scau.librarica.authorize.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.crypto.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getParam;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** 注册
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /user/register.api

   <strong>参数</strong>
   mail:string(60), 邮件地址

   <strong>响应</strong>
   application/json class=authorize.dao.User(private)
   @see J#writeUserPrivate

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Register extends HttpServlet
{
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

        try
        {
            String mail = (String)notNull(getParam(req, MAIL));

            HiberDao.begin();

            User u = Authorizer.register(mail);

            HiberDao.commit();

            J.writeUserPrivate(u, resp);
        }
        catch (Exception ex)
        {
            req.setAttribute(Constants.KEY_EXCEPTION, ex);
            req.getRequestDispatcher(Constants.URI_ERROR_HANDLER).forward(req, resp);
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
