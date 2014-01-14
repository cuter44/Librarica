package cn.edu.scau.librarica.lend.filter;

/* filter */
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import cn.edu.scau.librarica.lend.dao.BorrowSession;
import cn.edu.scau.librarica.lend.core.BorrowSessionMgr;

/** 检查是否处于借阅事务中的过滤器
 * 检测书籍id, 若该书籍处于一个未完成的借阅事务中, 则拦截请求
 * <br />
 * 需要在 web.xml 中配置一个参数:
 * bookIdParamName 表示用于查找书籍 id 的键名
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   无法通过过滤器时, 返回 Forbidden(403)
 * </pre>
 */
public class IsBookInvolvedInterceptor
    implements Filter
{
    private static final String BOOK_ID_PARAM_NAME = "bookIdParamName";
    private String BID;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.BID = conf.getInitParameter(BOOK_ID_PARAM_NAME);

        return;
    }

    @Override
    public void destroy()
    {
        return;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
        this.doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
    }

    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
    {
        boolean flag = false;

        try
        {
            Long bid = HttpUtil.getLongParam(req, BID);
            if (bid == null)
                throw(new MissingParameterException(BID));

            HiberDao.begin();

            // invert
            flag = !BorrowSessionMgr.isBookInvolved(bid);

            HiberDao.commit();

        }
        catch (MissingParameterException ex)
        {
            flag = false;
        }
        catch (Exception ex)
        {
            flag = false;
            this.context.log("", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally
        {
            HiberDao.close();
        }

        try
        {
            if (flag)
            {
                chain.doFilter(req, resp);
            }
            else
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                //resp.setCharacterEncoding("utf-8");
                //resp.setContentType("application/json");
                //PrintWriter out = resp.getWriter();
                //out.println("{\"flag\":\"!notowner\"}");

                return;
            }
        }
        catch (Exception ex)
        {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            this.context.log("", ex);
            return;
        }

    }
}
