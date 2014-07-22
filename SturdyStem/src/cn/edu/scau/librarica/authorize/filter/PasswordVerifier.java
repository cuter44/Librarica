package cn.edu.scau.librarica.authorize.filter;

/* filter */
import java.io.*;
import java.security.PrivateKey;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.crypto.*;
import com.github.cuter44.util.servlet.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getByteArrayParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.authorize.exception.*;

/** 检查用户 id 和登录密码是否匹配
 * 如果无法匹配, 则拦截请求.
 * <br />
 * 需要在 web.xml 中配置两个参数:
 * userIdParamName 表示用于查找用户ID的键名
 * passParamName 表示用于查找密码的键名, 密码应该按登录的方式加密传输
 *
 * <pre style="font-size:12px">
   <strong>例外</strong>
   uid/pass 缺失/错误时会报错, 报什么错我也不知道...
 * </pre>
 */
public class PasswordVerifier
    implements Filter
{
    private static final String USER_ID_PARAM_NAME = "userIdParamName";
    private static final String PASS_PARAM_NAME = "passParamName";
    private String UID;
    private String PASS;

    private ServletContext context;

    @Override
    public void init(FilterConfig conf)
    {
        this.context = conf.getServletContext();

        this.UID = conf.getInitParameter(USER_ID_PARAM_NAME);
        this.PASS = conf.getInitParameter(PASS_PARAM_NAME);

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
            Long        uid =   (Long)      notNull(getLongParam(req, UID));
            byte[]      pass =  (byte[])    notNull(getByteArrayParam(req, PASS));
            PrivateKey  key =   (PrivateKey)notNull(RSAKeyCache.get(uid));

            pass = CryptoUtil.RSADecrypt(pass, key);

            HiberDao.begin();

            if (!Authorizer.verifyPassword(uid, pass))
                throw(new UnauthorizedException("Incorrect password."));

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
