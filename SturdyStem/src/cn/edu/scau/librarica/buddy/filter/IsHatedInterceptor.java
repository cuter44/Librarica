package cn.edu.scau.librarica.buddy.filter;

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

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import cn.edu.scau.librarica.buddy.core.BuddyMgr;

/** ����Ƿ񱻶Է�����
 * ��鼺����uid�ͶԷ���uid, ����Է���������Ϊ������, ����������.
 * <br />
 * ��Ҫ�� web.xml ��������������:
 * meIdParamName ��ʾ����uid�ļ���
 * opIdParamName ��ʾ�Է�uid�ļ���
 *
 * <pre style="font-size:12px">
   <strong>����</strong>
   ���Է�����ʱ���� Forbidden(403): {"flag":"!hated"}
   û����Ӧ��¼/uid������ʱ��������
 * </pre>
 */
public class IsHatedInterceptor
    implements Filter
{
    private static final String ME_ID_PARAM_NAME = "meIdParamName";
    private static final String OP_ID_PARAM_NAME = "opIdParamName";
    private String MEID;
    private String OPID;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.MEID = conf.getInitParameter(ME_ID_PARAM_NAME);
        this.OPID = conf.getInitParameter(OP_ID_PARAM_NAME);

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
            Long meId = HttpUtil.getLongParam(req, MEID);
            if (meId == null)
                throw(new MissingParameterException(MEID));

            Long opId = HttpUtil.getLongParam(req, OPID);
            if (opId == null)
                throw(new MissingParameterException(OPID));

            HiberDao.begin();

            flag = BuddyMgr.isHated(meId, opId);

            HiberDao.commit();

        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().println("{\"flag\":\"!parameter\"}");
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
            resp.getWriter().println("{\"flag\":\"!hated\"}");
            return;
        }

    }
}
