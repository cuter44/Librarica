package cn.edu.scau.librarica.authorize.filter;

/* filter */
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.edu.scau.librarica.authorize.core.Authorizer;
import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

/** ����û� id �� session key �Ƿ�ƥ��
 * ����޷�ƥ��, ����������.
 * <br />
 * ��Ҫ�� web.xml ��������������:
 * userIdParamName ��ʾ���ڲ����û�ID�ļ���
 * sessionKeyParamName ��ʾ���ڲ����û�Session Key�ļ���
 *
 * <pre style="font-size:12px">
   <strong>����</strong>
   ������ȫʱ���� Bad Request(400): {"flag":"!parameter"}
   userId������ʱ, ���� Unauthorized(401): {"flag":"!notfound"}
   ���벻ƥ��ʱ, ���� Unauthorized(401): {"flag":"!incorrect"}
 * </pre>
 */
public class SessionKeyVerifier
    implements Filter
{
    private static final String USER_ID_PARAM_NAME = "userIdParamName";
    private static final String SESSION_KEY_PARAM_NAME = "sessionKeyParamName";
    private String UID;
    private String S;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.UID = conf.getInitParameter(USER_ID_PARAM_NAME);
        this.S = conf.getInitParameter(SESSION_KEY_PARAM_NAME);

        return;
    }

    @Override
    public void destroy()
    {
        return;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        this.doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
    }

    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
        throws IOException, ServletException
    {
        boolean flag = false;

        try
        {
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            byte[] skey = HttpUtil.getByteArrayParam(req, S);
            if (skey == null)
                throw(new MissingParameterException(S));

            HiberDao.begin();

            flag = Authorizer.verifySkey(uid, skey);

            HiberDao.commit();
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().println("{\"flag\":\"!parameter\"}");
            return;
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().println("{\"flag\":\"!notfound\"}");
            return;
        }
        catch (Exception ex)
        {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.context.log("", ex);
            return;
        }
        finally
        {
            HiberDao.close();
        }

        if (flag)
        {
            chain.doFilter(req, resp);
        }
        else
        {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().println("{\"flag\":\"!incorrect\"}");
            return;
        }

    }
}
