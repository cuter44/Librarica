package cn.edu.scau.librarica.lend.filter;

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

import cn.edu.scau.librarica.lend.dao.BorrowSession;
import cn.edu.scau.librarica.lend.core.BorrowSessionMgr;

/** ����Ƿ� ������ �Ľ�ɫ
 * ����û�id��ָ���Ľ��ĻỰ���Ƿ���Ϊ������, �����������������
 * <br />
 * ��Ҫ�� web.xml ��������������:
 * borrowSessionIdParamName ��ʾ���ڲ��ҽ��ĻỰ id �ļ���
 * userIdParamName ��ʾ���ڲ����û� id �ļ���
 *
 * <pre style="font-size:12px">
   <strong>����</strong>
   ȱ�ٲ���ʱ���� Bad Request(400):{"flag":"!parameter"}
   ָ�����ĻỰ/�û�������/�û����ǽ�����ʱ���� Forbidden(403):{"flag":""}
 * </pre>
 */
public class IsBorrowerVerifier
    implements Filter
{
    private static final String BORROW_SESSION_ID_PARAM_NAME = "borrowSessionIdParamName";
    private static final String USER_ID_PARAM_NAME = "userIdParamName";
    private String BSID;
    private String UID;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.BSID = conf.getInitParameter(BORROW_SESSION_ID_PARAM_NAME);
        this.UID = conf.getInitParameter(USER_ID_PARAM_NAME);

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

            Long bsid = HttpUtil.getLongParam(req, BSID);
            if (bsid == null)
                throw(new MissingParameterException(BSID));

            HiberDao.begin();

            flag = BorrowSessionMgr.isBorrower(bsid, uid);

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
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().println("{\"flag\":\"\"}");
            return;
        }
    }
}
