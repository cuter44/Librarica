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

/** 接受豆瓣授权
 * <pre style="font-size:12px">

   <strong>请求</strong>
   GET /douban/auth

   <strong>参数</strong>
   <i>用于被豆瓣回调的接口</i>
   code:hex, 豆瓣授权code.
   state:json,URLEncoded:
     action:string="bind", 目前只支持该操作, 表示关联豆瓣帐号
     id:long, 关联到本系统上的uid
     s:string, session key
     html:string, 可选, 为true(忽略大小写)时返回人机友好的html页面(暂不可用)

   <strong>响应</strong>
   对于?code=..., 成功时响应正文由 /douban/get-token 生成.

   <strong>例外</strong>
   对于在第一步时授权失败, 返回 Forbidden(403):{"flag":"!rejected","error":"${豆瓣回调的error字段}"}
   对于在第二步时授权失败, 返回 Forbidden(403):{"flag":"!rejected","error":"${豆瓣 errorCode 的 toString()}"}
   需要的 state 不全时返回 Bad Request(400):{"flag":"parameter"}
   指定的 id 不存在时返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>暂无
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
