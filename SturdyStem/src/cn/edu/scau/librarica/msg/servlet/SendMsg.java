package cn.edu.scau.librarica.msg.servlet;

import java.util.Arrays;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getParam;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.msg.dao.*;
import cn.edu.scau.librarica.msg.core.*;

/** 发送消息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /msg/send.api

   <strong>参数</strong>
   t:long, 必需, 对方的uid
   c:string, 必需, 消息内容
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json
   {"error":"ok"}

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class SendMsg extends HttpServlet
{
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
        boolean dbUsed = false;

        req.setCharacterEncoding("utf-8");

        try
        {
            Long    f = (Long)  notNull(getLongParam(req, F));
            Long    t = (Long)  notNull(getLongParam(req, T));
            String  c = (String)notNull(getParam(req, C));

            Msg msg = MsgMgr.createTransient(f, t, c);

            AsyncContext ctx = MsgWaiterPool.getInstance().remove(t);
            if (ctx != null)
            {
                J.write(Arrays.asList(msg), ctx.getResponse());
                ctx.complete();
            }
            else
            {
                HiberDao.begin();
                dbUsed = true;

                HiberDao.save(msg);

                HiberDao.commit();
            }

            J.writeOK(resp);
        }
        catch (Exception ex)
        {
            req.setAttribute(Constants.KEY_EXCEPTION, ex);
            req.getRequestDispatcher(Constants.URI_ERROR_HANDLER).forward(req, resp);
        }
        finally
        {
            if (dbUsed)
                HiberDao.close();
        }

        return;
    }
}
