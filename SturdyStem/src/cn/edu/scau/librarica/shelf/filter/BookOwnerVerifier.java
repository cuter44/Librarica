package cn.edu.scau.librarica.shelf.filter;

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

import cn.edu.scau.librarica.shelf.dao.Book;
import cn.edu.scau.librarica.shelf.core.BookMgr;

/** ����Ƿ��鼮��������, �������, ����������
 * <br />
 * ����鼮id���û�id, ���û�id
 * ��Ҫ�� web.xml ��������������:
 * userIdParamName ��ʾ���ڲ��������� id �ļ���
 * bookIdParamName ��ʾ���ڲ����鼮 id �ļ���
 *
 * <pre style="font-size:12px">
   <strong>����</strong>
   ȱ�ٲ���ʱ���� Bad Request(400):{"flag":"!parameter"}
   �鼮������ʱ���� Forbidden(403):{"flag":"!notfound"}
   �����鼮������ʱ���� Forbidden(403):{"flag":""}
 * </pre>
 */
public class BookOwnerVerifier
    implements Filter
{
    private static final String BOOK_ID_PARAM_NAME = "bookIdParamName";
    private static final String USER_ID_PARAM_NAME = "userIdParamName";
    private String UID;
    private String BID;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.BID = conf.getInitParameter(BOOK_ID_PARAM_NAME);
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

            Long bid = HttpUtil.getLongParam(req, BID);
            if (bid == null)
                throw(new MissingParameterException(BID));

            HiberDao.begin();

            flag = BookMgr.isOwner(bid, uid);

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
            resp.getWriter().println("{\"flag\":\"\"}");
            return;
        }
    }
}
