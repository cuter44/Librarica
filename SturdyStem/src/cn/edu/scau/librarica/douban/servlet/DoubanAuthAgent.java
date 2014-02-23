package cn.edu.scau.librarica.douban.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.douban.dao.*;
import cn.edu.scau.librarica.douban.core.*;

/** ���ܶ�����Ȩ
 * <pre style="font-size:12px">

   <strong>����</strong>
   GET /douban/auth

   <strong>����</strong>
   <i>���ڱ�����ص��Ľӿ�</i>
   code:hex, ������Ȩcode.
   state:json,URLEncoded:
     action:string="bind", Ŀǰֻ֧�ָò���, ��ʾ���������ʺ�
     id:long, ��������ϵͳ�ϵ�uid
     s:string, session key
     html:string, ��ѡ, Ϊtrue(���Դ�Сд)ʱ�����˻��Ѻõ�htmlҳ��(�ݲ�����)

   <strong>��Ӧ</strong>
   ����?code=..., �ɹ�ʱ��Ӧ������ /douban/get-token ����.

   <strong>����</strong>
   �����ڵ�һ��ʱ��Ȩʧ��, ���� Forbidden(403):{"flag":"!rejected","error":"${����ص���error�ֶ�}"}
   �����ڵڶ���ʱ��Ȩʧ��, ���� Forbidden(403):{"flag":"!rejected","error":"${���� errorCode �� toString()}"}
   ��Ҫ�� state ��ȫʱ���� Bad Request(400):{"flag":"parameter"}
   ָ���� id ������ʱ���� Forbidden(403):{"flag":"!notfound"}

   <strong>����</strong>����
 * </pre>
 *
 */
public class DoubanAuthAgent extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ERROR = "error";
    private static final String CODE = "code";
    private static final String STATE = "state";
    private static final String ID = "id";
    private static final String ACTION = "action";
    private static final String BIND = "bind";
    private static final String S = "s";

    public void doConn(HttpServletRequest req, HttpServletResponse resp, JSONObject state)
        throws MissingParameterException, EntityNotFoundException, Exception
    {
        String code = HttpUtil.getParam(req, CODE);
        if (code == null)
            throw(new MissingParameterException(CODE));

        DoubanTokenRetriever.viaCode(
            state.getLong(ID),
            code
        );
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json; charset=utf-8");
        PrintWriter out = resp.getWriter();

        String error = HttpUtil.getParam(req, ERROR);
        if (error != null)
        {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.println("{\"flag\":\"!reject\",\"error\":\""+error+"\"}");
        }

        try
        {

            HiberDao.begin();

            String state = HttpUtil.getParam(req,STATE);
            if (state == null)
                throw(new MissingParameterException(STATE));
            JSONObject stateJson = JSON.parseObject(state);
            String action = stateJson.getString(ACTION);

            if (BIND.equals(action))
                this.doConn(req, resp, stateJson);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.println("{\"flag\":\"!notfound\"}");
        }
        catch (IllegalStateException ex)
        {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.println("{\"flag\":\"!status\"}");
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"flag\":\"!parameter\"}");
        }
        catch (Exception ex)
        {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            this.log("", ex);
        }
        finally
        {
            HiberDao.close();
        }

        return;
    }
}
