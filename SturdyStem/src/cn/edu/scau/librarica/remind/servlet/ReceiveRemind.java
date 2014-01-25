package cn.edu.scau.librarica.remind.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.remind.dao.*;
import cn.edu.scau.librarica.remind.core.*;

/** 发送消息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /remind/receive

   <strong>参数</strong>
   uid:long, 表示收件人id
   wait:time-in-second, 表示在没有消息可用时要挂起的时间, 受服务器设置存在上限.
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json:
   List&lt;type, id&gt;
   其中,
   type:string, 事件类型, 通常是 POJO 的类名, 比如 BorrowSession, Msg
   value:string-of-decimal, id, 通常就是 POJO 的 id, 对于 Msg 则为收件人 id

   <strong>例外</strong>
   没有可用通知时返回 OK(200):{}
   uid 不正确时返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>
    GET /librarica/remind/receive?uid=4&wait=180 HTTP/1.1

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Content-Type: application/json;charset=utf-8
    Content-Length: 25
    Date: Sat, 25 Jan 2014 15:28:07 GMT

    [{"BorrowSession":"3"}]

 * </pre>
 *
 */
public class ReceiveRemind extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String WAIT = "wait";

    public static JSONObject jsonize(Remind r)
    {
        JSONObject json = new JSONObject();

        json.put(r.getT(), r.getV());

        return(json);
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
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try
        {
            Long uid = HttpUtil.getLongParam(req, UID);
            if (uid == null)
                throw(new MissingParameterException(UID));

            Integer wait = HttpUtil.getIntParam(req, WAIT);
            if (wait == null)
                wait = 0;

            HiberDao.begin();

            List<Remind> rl = RemindRouter.receive(uid, wait);

            HiberDao.commit();

            JSONArray json = new JSONArray();
            Iterator<Remind> itr = rl.iterator();
            while (itr.hasNext())
                json.add(jsonize(itr.next()));

            out.println(json.toJSONString());
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            out.println("{\"flag\":\"!notfound\"}");
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
