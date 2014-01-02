package cn.edu.scau.librarica.spi.mailer;

import com.github.cuter44.util.crypto.*;
import cn.edu.scau.librarica.util.mail.*;
import cn.edu.scau.librarica.util.conf.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** 默认的注册邮件发送机
 * 可以参照这个类以实现自己的邮件内容.
 * 将类名注册在 librarica.xml 以在启动时注入监听器
 */
public class DefaultRegisterMailer
{
    private static String baseURL = Configurator.get("librarica.server.web.baseurl");

    // 注册监听器
    static
    {
        Authorizer.addListener(
            new Authorizer.StatusChangedListener()
            {
                @Override
                public void onStatusChanged(User u)
                {
                    if (User.REGISTERED.equals(u.getStatus()))
                        DefaultRegisterMailer.sendRegisteredMail(u);
                }
            }
        );
    }

    public static void sendRegisteredMail(User u)
    {
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
            ex.printStackTrace();
        }

        return;
    }
}

