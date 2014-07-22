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

/** 检查是否被对方拉黑
 * 检查己方的uid和对方的uid, 如果对方将己方列为黑名单, 则拦截请求.
 * <br />
 * 需要在 web.xml 中配置两个参数:
 * meIdParamName 表示己方uid的键名
 * opIdParamName 表示对方uid的键名
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   被对方拉黑时返回 Forbidden(403): {"flag":"!hated"}
   没有相应记录/uid不存在时放行请求
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
