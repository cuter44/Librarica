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
    implements Authorizer.StatusChangedListener
{
    private static String baseURL = Configurator.get("librarica.server.web.baseurl");

    // 注册监听器
    static
    {
        Authorizer.addListener(
            new DefaultRegisterMailer()
        );
    }

    @Override
    public void onStatusChanged(User u)
    {
        if (User.REGISTERED.equals(u.getStatus()))
            this.sendRegisteredMail(u);
    }

    public void sendRegisteredMail(User u)
    {
        String activateURL =
            baseURL + "/user/activate.jsp?" +
            "uid=" + u.getId() + "&" +
            "code=" + CryptoUtil.byteToHex(u.getPass());

        String recipient = u.getMail();
        String subject = "欢迎注册木瓜(≧▽≦)っ□";

        String content =
            "<html>"+
             "<head>"+
              "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">"+
             "</head>"+
             "<body>"+
               "<p>"+
               "欢迎加入木瓜, 一个热爱读书的社交应用."+
               "</p><p>"+
               "要验证邮件地址才能使用 ( °д°)→"+
               "<a href=\"" +activateURL+"\">"+activateURL+"</a>"+
               "</p><p>"+
               "谢啦, 祝你玩得开心~"+
               "</p>"+
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

