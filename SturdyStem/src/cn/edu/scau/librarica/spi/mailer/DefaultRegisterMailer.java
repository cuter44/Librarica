package cn.edu.scau.librarica.spi.mailer;

import com.github.cuter44.util.crypto.*;
import cn.edu.scau.librarica.util.mail.*;
import cn.edu.scau.librarica.util.conf.*;

import cn.edu.scau.librarica.authorize.dao.*;
import cn.edu.scau.librarica.authorize.core.*;

/** Ĭ�ϵ�ע���ʼ����ͻ�
 * ���Բ����������ʵ���Լ����ʼ�����.
 * ������ע���� librarica.xml ��������ʱע�������
 */
public class DefaultRegisterMailer
    implements Authorizer.StatusChangedListener
{
    private static String baseURL = Configurator.get("librarica.server.web.baseurl");

    // ע�������
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
        String subject = "��ӭע��ľ��(�R���Q)�á�";

        String content =
            "<html>"+
             "<head>"+
              "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">"+
             "</head>"+
             "<body>"+
               "<p>"+
               "��ӭ����ľ��, һ���Ȱ�������罻Ӧ��."+
               "</p><p>"+
               "Ҫ��֤�ʼ���ַ����ʹ�� ( ��ա�)��"+
               "<a href=\"" +activateURL+"\">"+activateURL+"</a>"+
               "</p><p>"+
               "л��, ף����ÿ���~"+
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

