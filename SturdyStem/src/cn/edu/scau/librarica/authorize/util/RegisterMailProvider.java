package cn.edu.scau.librarica.authorize.util;

import cn.edu.scau.librarica.util.mail.MailProvider;

import javax.servlet.http.*;
import com.github.cuter44.util.servlet.*;
import com.github.cuter44.util.crypto.*;
import cn.edu.scau.librarica.util.mail.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

public class RegisterMailProvider
    implements MailProvider
{
    private static String MAIL = "mail";

    @Override
    public boolean sendMail(HttpServletRequest req)
    {
        User u = UserMgr.forMail(HttpUtil.getParam(req, MAIL));

        StringBuffer reqURL = req.getRequestURL();
        int URLPart = reqURL.length();
        int URIPart = req.getRequestURI().length();
        int contextPart = req.getContextPath().length();

        String baseURL = reqURL.substring(0, URLPart-URIPart+contextPart);

        String activateURL =
            baseURL + "/user/activate.jsp?" +
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
        try
        {
            MailUtil.sendHTMLMail(recipient, subject, content);
        }
        catch (Exception ex)
        {
            return(false);
        }

        return(true);
    }
}

