package cn.edu.scau.librarica.authorize.filter;

/* filter */
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getByteArrayParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.core.Authorizer;
import cn.edu.scau.librarica.authorize.exception.*;

/** 检查用户 id 和 session key 是否匹配
 * 如果无法匹配, 则拦截请求.
 * <br />
 * 需要在 web.xml 中配置两个参数:
 * userIdParamName 表示用于查找用户ID的键名
 * sessionKeyParamName 表示用于查找用户Session Key的键名
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   uid/skey 缺失时会报错, 报什么错我也不知道...
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
            Long    uid =   (Long)  notNull(getLongParam(req, UID));
            byte[]  skey =  (byte[])notNull(getByteArrayParam(req, S));

            HiberDao.begin();

            if (!Authorizer.verifySkey(uid, skey))
                throw(new UnauthorizedException("Incorrect skey."));

            HiberDao.commit();
        }
        catch (Exception ex)
        {
            req.setAttribute(Constants.KEY_EXCEPTION, ex);
            req.getRequestDispatcher(Constants.URI_ERROR_HANDLER).forward(req, resp);
        }
        finally
        {
            HiberDao.close();
        }

        chain.doFilter(req, resp);
    }
}
