package cn.edu.scau.librarica.msg.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.msg.dao.*;
import cn.edu.scau.librarica.msg.core.*;

/** 发送消息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /msg/send

   <strong>参数</strong>
   t:long, 必需, 对方的uid
   c:string, 必需, 消息内容
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   成功则返回200(OK), 没有响应正文
   该状态码仅表明服务器正确接受消息, 而非将消息发送到对方客户端

   <strong>例外</strong>
   to不正确时返回 Bad Request(400):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SendMsg extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String F = "uid";
    private static final String T = "t";
    private static final String C = "c";

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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        JSONObject json = new JSONObject();

        try
        {
            Long f = HttpUtil.getLongParam(req, F);
            if (f == null)
                throw(new MissingParameterException(F));

            Long t = HttpUtil.getLongParam(req, T);
            if (t == null)
                throw(new MissingParameterException(T));

            String c = HttpUtil.getParam(req, C);
            if (c == null)
                throw(new MissingParameterException(C));


            HiberDao.begin();

            Msg msg = MsgMgr.createTransient(f, t, c);
            MsgRouter.send(msg);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            json.put(FLAG, "!notfound");
            out.println(json.toJSONString());
        }
        catch (MissingParameterException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            json.put(FLAG, "!parameter");
            out.println(json.toJSONString());
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
