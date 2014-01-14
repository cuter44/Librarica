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
import java.security.PrivateKey;

import com.github.cuter44.util.crypto.*;
import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import cn.edu.scau.librarica.authorize.core.Authorizer;
import cn.edu.scau.librarica.authorize.core.RSAKeyCache;

/** ����û� id �͵�¼�����Ƿ�ƥ��
 * ����޷�ƥ��, ����������.
 * <br />
 * ��Ҫ�� web.xml ��������������:
 * userIdParamName ��ʾ���ڲ����û�ID�ļ���
 * passParamName ��ʾ���ڲ�������ļ���, ����Ӧ�ð���¼�ķ�ʽ���ܴ���
 *
 * <pre style="font-size:12px">
   <strong>����</strong>
   �޷�ͨ��������ʱ, ���� Unauthorized(401)
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

            byte[] pass = HttpUtil.getByteArrayParam(req, PASS);
            if (pass == null)
                throw(new MissingParameterException(PASS));

            PrivateKey key = RSAKeyCache.get(uid);
            if (key == null)
                throw(new EntityNotFoundException("RSA private key not found."));

            pass = CryptoUtil.RSADecrypt(pass, key);

            HiberDao.begin();

            flag = Authorizer.verifyPassword(uid, pass);

            HiberDao.commit();
        }
        catch (MissingParameterException ex)
        {
            flag = false;
        }
        catch (EntityNotFoundException ex)
        {
            flag = false;
        }
        catch (Exception ex)
        {
            flag = false;
            this.context.log("", ex);
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
