package cn.edu.scau.librarica.msg.servlet;

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

import cn.edu.scau.librarica.msg.dao.*;
import cn.edu.scau.librarica.msg.core.*;

/** 发送消息
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /msg/receive

   <strong>参数</strong>
   uid:long, 表示收件人id
   wait:time-in-second, 表示在没有消息可用时要挂起的时间, 受服务器设置存在上限.
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json:
   压缩的 json 对象, 结构如下所列:
   Map&lt;fromId, Map&lt;timestamp, content&gt;&gt;
   其中,
   fromId:long, 表示发送者id.
   timestamp:unix-time-in-second, 发送时间戳
   content:string, 消息正文
   因为 fastjson 的特性, timestamp:content 对会被按字典序升序排列, 这在大部分情况下不会构成问题, 所以不会被修正.

   <strong>例外</strong>
   没有可用消息时返回 OK(200):{}
   uid 不正确时返回 Forbidden(403):{"flag":"!notfound"}

   <strong>样例</strong>
    GET /librarica/msg/receive?uid=4&s=f00a551d&wait=100

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Content-Type: application/json;charset=utf-8
    Content-Length: 84
    Date: Sun, 19 Jan 2014 15:12:14 GMT

    {
      "1":{
        "1390144276":"blabla3",
        "1390144286":"blabla4"
      },
      "2":{
        "1390144320":"blabla4"
      }
    }


 * </pre>
 *
 */
public class ReceiveMsg extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String T = "uid";
    private static final String WAIT = "wait";

    public static JSONObject jsonize(List<Msg> l)
    {
        JSONObject packed = new JSONObject();

        Iterator<Msg> i = l.iterator();
        while (i.hasNext())
        {
            Msg m = i.next();
            String uid = m.getF().getId().toString();

            JSONObject grouped = packed.getJSONObject(uid);
            if (grouped == null)
            {
                grouped = new JSONObject();
                packed.put(uid, grouped);
            }

            grouped.put(
                String.format("%ts", m.getM()),
                m.getC()
            );
        }

        return(packed);
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
            Long t = HttpUtil.getLongParam(req, T);
            if (t == null)
                throw(new MissingParameterException(T));

            Integer wait = HttpUtil.getIntParam(req, WAIT);
            if (wait == null)
                wait = 0;

            HiberDao.begin();

            List<Msg> ml = MsgRouter.receive(t, wait);

            HiberDao.commit();

            JSONObject json = jsonize(ml);

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
