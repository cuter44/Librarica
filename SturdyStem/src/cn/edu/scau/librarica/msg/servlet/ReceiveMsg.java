package cn.edu.scau.librarica.msg.servlet;

import java.io.*;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

import com.github.cuter44.util.dao.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import static com.github.cuter44.util.servlet.HttpUtil.getIntParam;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.msg.dao.*;
import cn.edu.scau.librarica.msg.core.*;

/** 发送消息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /msg/receive.api

   <strong>参数</strong>
   uid:long, 表示收件人id
   wait:time-in-second, 表示在没有消息可用时要挂起的时间, 受服务器设置存在上限.
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json array class=msg.dao.Msg
   @see J#write

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>

 * </pre>
 *
 */
public class ReceiveMsg extends HttpServlet
{
    private static final String T = "uid";
    private static final String WAIT = "wait";

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

        try
        {
            Long    t       = (Long)    notNull(getLongParam(req, T));
            Integer wait    =                   getIntParam(req, WAIT);
            if (wait == null)
                wait = 0;

            HiberDao.begin();

            List<Msg> l = MsgMgr.retrieve(t);
            if (l.size() != 0)
            {
                J.write(l, resp);
            }
            else
            {
                // else wait async
                AsyncContext ctx = req.startAsync();
                ctx.setTimeout(wait*1000L);
                MsgWaiterPool.getInstance().put(t, ctx);

                ctx.start(
                    new Runnable()
                    {
                        @Override
                        public void run() { return; }
                    }
                );
            }

            HiberDao.commit();

            return;
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

        return;
    }
}
