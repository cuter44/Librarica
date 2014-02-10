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

/** ����Ƿ� ���� �Ľ�ɫ
 * ����û�id��ָ���Ľ��ĻỰ���Ƿ���Ϊ����, �����������������
 * <br />
 * ��Ҫ�� web.xml ��������������:
 * buySessionIdParamName ��ʾ���ڲ��ҽ��ĻỰ id �ļ���
 * userIdParamName ��ʾ���ڲ����û� id �ļ���
 *
 * <pre style="font-size:12px">
   <strong>����</strong>
   �޷�ͨ��������ʱ, ���� Forbidden(403)
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
