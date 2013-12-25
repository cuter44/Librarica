package cn.edu.scau.librarica.util.mail;

import javax.servlet.http.HttpServletRequest;

public interface MailProvider
{
    public boolean sendMail(HttpServletRequest req);
}
