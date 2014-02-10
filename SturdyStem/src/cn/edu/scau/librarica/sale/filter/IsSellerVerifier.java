package cn.edu.scau.librarica.sale.filter;

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

import cn.edu.scau.librarica.sale.dao.BuySession;
import cn.edu.scau.librarica.sale.core.BuySessionMgr;

/** 检查是否 卖家 的角色
 * 检查用户id在指定的借阅会话中是否作为卖家, 如果不是则拦截请求
 * <br />
 * 需要在 web.xml 中配置两个参数:
 * buySessionIdParamName 表示用于查找借阅会话 id 的键名
 * userIdParamName 表示用于查找用户 id 的键名
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   无法通过过滤器时, 返回 Forbidden(403)
 * </pre>
 */
public class IsSellerVerifier
    implements Filter
{
    private static final String BUY_SESSION_ID_PARAM_NAME = "buySessionIdParamName";
    private static final String USER_ID_PARAM_NAME = "userIdParamName";
    private String BSID;
    private String UID;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.BSID = conf.getInitParameter(BUY_SESSION_ID_PARAM_NAME);
        this.UID = conf.getInitParameter(USER_ID_PARAM_NAME);

        //System.err.println(this.getClass().toString() + " inited.");

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

            Long bsid = HttpUtil.getLongParam(req, BSID);
            if (bsid == null)
                throw(new MissingParameterException(BSID));

            HiberDao.begin();

            flag = BuySessionMgr.isSeller(bsid, uid);

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
