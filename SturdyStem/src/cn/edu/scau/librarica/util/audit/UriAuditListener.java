package cn.edu.scau.librarica.util.audit;

import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import cn.edu.scau.librarica.util.audit.UriAuditor;


public class UriAuditListener
    implements ServletRequestListener
{
    @Override
    public void requestInitialized(ServletRequestEvent sre)
    {
        try
        {
            HttpServletRequest req = (HttpServletRequest)sre.getServletRequest();
            UriAuditor.inc(req.getRequestURI());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre)
    {
        return;
    }
}

