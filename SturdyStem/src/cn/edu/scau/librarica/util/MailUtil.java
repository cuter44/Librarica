package cn.edu.scau.librarica.util;

import javax.mail.Session;
//import javax.mail.Message;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.InternetAddress;
import javax.mail.Message.RecipientType;

import javax.mail.Transport;
import javax.mail.SendFailedException;

import cn.edu.scau.librarica.util.Configurator;

/** 发邮件功能封装
 * <br />
 * 依赖 Configurator 提供参数
 */
public class MailUtil
{
    private static String MAIL_ADDRESS = "librarica.mail.address";
    private static String MAIL_PERSONAL = "librarica.mail.personal";

    private static String MAIL_USERNAME = "librarica.mail.username";
    private static String MAIL_PASSWORD = "librarica.mail.password";

    private Session session = null;

    private static class Singleton
    {
        private static MailUtil instance = new MailUtil();
    }

    private MailUtil()
    {
        Authenticator auth = new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    String name = Configurator.get(MAIL_USERNAME);
                    String pass = Configurator.get(MAIL_PASSWORD);

                    return(
                        new PasswordAuthentication(name, pass)
                    );
                }
            };

        this.session = Session.getDefaultInstance(
            Configurator.getProperties(),
            auth
        );
        this.session.setDebug(true);
    }

    public static String sendHTMLMail(String to, String subject, String content)
    {
        try
        {
            MimeMessage msg = new MimeMessage(Singleton.instance.session);
            String address = Configurator.get(MAIL_ADDRESS);
            String personal = Configurator.get(MAIL_PERSONAL);

            msg.setFrom(
                new InternetAddress(
                    address,
                    MimeUtility.encodeText(personal, "utf-8", "B")
                )
            );

            msg.addRecipient(
                RecipientType.TO,
                new InternetAddress(to)
            );

            msg.setSubject(subject);

            msg.setContent(content, "text/html; charset=UTF-8");

            Transport.send(msg);

            return("success");
        }
        catch (SendFailedException ex)
        {
            ex.printStackTrace();
            return("!failed");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return("!error");
        }
    }
}
