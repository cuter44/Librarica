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
{
    private static String baseURL = Configurator.get("librarica.server.web.baseurl");

    // ע�������
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
        String subject = "[Librarica]��ӭ�������ǵĶ���֮��";

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

