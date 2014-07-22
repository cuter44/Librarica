package cn.edu.scau.librarica.util.mail;

import java.io.UnsupportedEncodingException;
import java.security.Security;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import com.github.cuter44.util.mail.*;

import cn.edu.scau.librarica.util.conf.Configurator;

/** 发邮件功能封装
 * <br />
 * 依赖 Configurator 提供参数
 */
public class MailUtil
{
    private static String MAIL_PERSONAL = "librarica.mail.personal";
    private static String MAIL_ADDRESS = "librarica.mail.address";

    private static String MAIL_USERNAME = "librarica.mail.username";
    private static String MAIL_PASSWORD = "librarica.mail.password";

    private JavaMailUtil core;

    private static class Singleton
    {
        private static MailUtil instance = new MailUtil();
    }

    private MailUtil()
    {
        this.core = JavaMailUtil.getInstance(
            Configurator.get("mail.smtp.host"),
            Configurator.get("mail.smtp.port"),
            Configurator.get(MAIL_USERNAME),
            Configurator.get(MAIL_PASSWORD),
            Configurator.get(MAIL_PERSONAL),
            Configurator.get(MAIL_ADDRESS)
        );
    }

    public static MimeMessage createMimeMessage()
        throws MessagingException
    {
        return(
            Singleton.instance.core.createMimeMessage()
        );
    }

    public static MimeMessage createMessage(String to)
        throws MessagingException, AddressException
    {
        return(
            Singleton.instance.core.createMimeMessage(to)
        );
    }

    public static void sendHTMLMail(String to, String subject, String content)
        throws MessagingException, AddressException
    {
        Singleton.instance.core.sendHTMLMail(to, subject, content);
    }
}
