package com.github.cuter44.util.mail;

//import java.nio.charset.Charset;
import java.util.Properties;
import java.io.UnsupportedEncodingException;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Message.RecipientType;

/**
 * currently ssl not supported
 */
public class JavaMailUtil
{
    private String personal;
    private String address;

    private Session session;

  // CONSTRUCT
    private JavaMailUtil(String host, String port, String username, String password, String personalName, String fromAddress)
    {
        this.personal = personalName;
        this.address = fromAddress;

        final String u = username;
        final String p = password;

        //Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        Authenticator auth = new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return(
                        new PasswordAuthentication(u, p)
                    );
                }
            };

        Properties prop = new Properties();
        prop.setProperty("mail.smtp.host", host);
        prop.setProperty("mail.smtp.auth", "true");
        prop.setProperty("mail.smtp.port", port);

        this.session = Session.getDefaultInstance(prop, auth);
    }

    public static JavaMailUtil getInstance(String host, String port, String username, String password, String personalName, String fromAddress)
    {
        return(
            new JavaMailUtil(host, port, username, password, personalName, fromAddress)
        );
    }

    public static JavaMailUtil getInstance(String host, String username, String password, String personalName, String fromAddress)
    {
        return(
            new JavaMailUtil(host, "25", username, password, personalName, fromAddress)
        );
    }

  // MESSAGE
    public MimeMessage createMimeMessage()
        throws MessagingException, AddressException
    {
        try
        {
            MimeMessage msg = new MimeMessage(this.session);

            msg.setFrom(
                new InternetAddress(
                    this.address,
                    MimeUtility.encodeText(this.personal, "UTF-8", "B")
                )
            );

            return(msg);
        }
        catch (UnsupportedEncodingException ex)
        {
            // never occured
            return(null);
        }
    }

    public MimeMessage createMimeMessage(String to)
        throws MessagingException, AddressException
    {
        MimeMessage msg = this.createMimeMessage();

        msg.addRecipient(
            RecipientType.TO,
            new InternetAddress(to)
        );

        return(msg);
    }

    public void send(MimeMessage msg)
        throws MessagingException
    {
        Transport.send(msg);
    }

  // INSTANT SEND
    public void sendHTMLMail(String to, String subject, String content)
        throws MessagingException, AddressException
    {
            // CREATE
            MimeMessage msg = this.createMimeMessage(to);

            // SUBJECT
            msg.setSubject(subject);

            // BODY
            MimeMultipart multiPart = new MimeMultipart("alternative");

            MimeBodyPart part1 = new MimeBodyPart();
            part1.setText(content, "UTF-8");
            part1.setHeader("Content-Type","text/html; charset=\"utf-8\"");

            multiPart.addBodyPart(part1);

            msg.setContent(multiPart);

            // SEND
            this.send(msg);

            return;
    }
}
