package cn.edu.scau.librarica.authorize.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebServlet;

import com.github.cuter44.util.dao.HiberDao;
import com.github.cuter44.util.servlet.HttpUtil;
import com.github.cuter44.util.servlet.MissingParameterException;

import com.alibaba.fastjson.*;

import org.apache.log4j.Logger;

import cn.edu.scau.librarica.authorize.dao.User;
import cn.edu.scau.librarica.authorize.core.UserMgr;
import cn.edu.scau.librarica.authorize.core.Authorizer;
import cn.edu.scau.librarica.util.MailUtil;
import com.github.cuter44.util.crypto.CryptoUtil;

/**
 * ````
 * <br />
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET/POST /???URL

   <strong>参数</strong>

   <strong>响应</strong>
   application/json 对象:

   <strong>例外</strong>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Register extends HttpServlet
{
    private static String FLAG = "flag";
    private static String MAIL = "mail";

    public static String sendActivateMail(User u, HttpServletRequest req)
    {
        StringBuffer reqURL = req.getRequestURL();
        int URLPart = reqURL.length();
        int URIPart = req.getRequestURI().length();
        int contextPart = req.getContextPath().length();

        String baseURL = reqURL.substring(0, URLPart-URIPart+contextPart);

        String activateURL =
            baseURL + "/authorize/user/activate.jsp?" +
            "id=" + u.getId() + "&" +
            "activateCode=" + CryptoUtil.byteToHex(u.getPass());

        String recipient = u.getMail();
        String subject = "[Librarica]欢迎加入我们的读书之旅";

        String content =
            "<html>"+
            "<head>"+
            "<meta http-equiv=\"content-type\" content=\"text/html\">"+
            "</head>"+
            "<body>"+
            "Please visit <a href=\"" + activateURL + "\">"+
            activateURL +
            "</a>"+
            " to activate your account."+
            "<br />"+
            "Or type in " + CryptoUtil.byteToHex(u.getPass()) + " to your app as activate code."+
            "</body>"+
            "</html>";

        String flag = MailUtil.sendHTMLMail(recipient, subject, content);

        if (flag.equals("!failed"))
            return("mailfail");
        else
            return(flag);
    }

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
        Logger logger = Logger.getLogger(Register.class);

        // Encoding Configuration
        // encode of req effects post method only
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");

        // Dequote if pend to use session
        //HttpSession session = req.getSession();

	    // Dequote if pend to write binary
        //resp.setContentType("???MIME");
        //OutputStream out = resp.getOutputStream();

        // Dequote if pend to write chars
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        // output buffer prepare
        JSONObject json = new JSONObject();

        try
        {
            String mail = HttpUtil.getParam(req, MAIL);
            if (mail == null)
                throw(new MissingParameterException(MAIL));

            HiberDao.begin();

            String flag = Authorizer.register(mail);

            if (flag == "success")
            {
                User u = UserMgr.forMail(mail);

                flag = sendActivateMail(u, req);
                json.put(FLAG, flag);

                HiberDao.commit();
                return;
            }
            else
            {
                HiberDao.rollback();
                json.put(FLAG, flag);
            }
        }
        catch (MissingParameterException ex)
        {
            ex.printStackTrace();
            json.put(FLAG, "!parameter");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            logger.error("Servlet failed:", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            out.println(json.toJSONString());
            HiberDao.close();
        }

        return;
    }
}
