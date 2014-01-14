package cn.edu.scau.librarica.shelf.filter;

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

import cn.edu.scau.librarica.shelf.dao.Book;
import cn.edu.scau.librarica.shelf.core.BookMgr;

/** 检查是否书籍的所有者, 如果是, 则拦截请求
 * <br />
 * 检查书籍id和用户id, 如用户id
 * 需要在 web.xml 中配置两个参数:
 * userIdParamName 表示用于查找所有者 id 的键名
 * bookIdParamName 表示用于查找书籍 id 的键名
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   无法通过过滤器时, 返回 Forbidden(403)
 * </pre>
 */
public class BookOwnerInterceptor
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
    {
        this.doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
    }

    public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
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

            flag = !BookMgr.isOwner(bid, uid);

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
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

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
