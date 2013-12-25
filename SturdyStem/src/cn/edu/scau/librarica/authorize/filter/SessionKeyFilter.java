package cn.edu.scau.librarica.authorize.filter;

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

import cn.edu.scau.librarica.authorize.core.Authorizer;
import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

/** 检查用户 id 和 session key 是否匹配
 * <br />
 * 需要在 web.xml 中配置两个参数:
 * userIdParamName 表示用于查找用户ID的键名
 * sessionKeyParamName 表示用于查找用户Session Key的键名
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   无法通过过滤器时, 返回 Unauthorized(401)
 * </pre>
 */
public class SessionKeyFilter
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

            byte[] skey = HttpUtil.getByteArrayParam(req, S);
            if (skey == null)
                throw(new MissingParameterException(S));

            HiberDao.begin();

            flag = Authorizer.verifySkey(uid, skey);

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
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
