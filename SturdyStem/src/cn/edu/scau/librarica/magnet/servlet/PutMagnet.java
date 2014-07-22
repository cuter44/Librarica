package cn.edu.scau.librarica.magnet.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.geom.PointLong;
import com.github.cuter44.util.servlet.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.magnet.core.*;

/** 共享地理位置信息
 * 地理位置信息在一定时间内存活(秒级别), 且只会被共享给指定的对方.
 * 在第一次请求这个接口时对方会收到通知(notify)
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /magnet/put

   <strong>参数</strong>
   op:long, 必需, 对方的uid
   pos:base32-geohash, 必需, 己方的geohsah.
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   成功则返回200(OK), 没有响应正文
   该状态码仅表明服务器正确接受消息, 而非将消息发送到对方客户端

   <strong>例外</strong>

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class PutMagnet extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ME = "uid";
    private static final String OP = "op";
    private static final String POS = "pos";

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

        try
        {
            Long me = HttpUtil.getLongParam(req, ME);
            if (me ==  null)
                throw(new MissingParameterException(ME));

            Long op = HttpUtil.getLongParam(req, OP);
            if (op == null)
                throw(new MissingParameterException(OP));

            String pos = HttpUtil.getParam(req, POS);
            if (pos == null)
                throw(new MissingParameterException(POS));

            MagnetCache.put(new PointLong(me, op), pos);

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

        return;
    }
}
