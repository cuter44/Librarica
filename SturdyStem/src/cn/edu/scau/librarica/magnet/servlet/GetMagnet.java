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

/** 取回对方的位置信息
 * 无论成功与否该方法都立即返回, 可能取不到数据或者取得过期的数据
 * 所以这方法更主要作用是确定对方的存活状态而已.
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /magnet/get

   <strong>参数</strong>
   op:long, 必需, 对方的uid
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json object:
   pos:base32-geohash, 仅在有可用数据时包含对方最后一次发送的geohash, 精度受对方的隐私策略影响.

   <strong>例外</strong>
   没有可用数据时返回 No Content(204), 没有响应正文或不需要考虑响应正文

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class GetMagnet extends HttpServlet
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

            String pos = MagnetCache.get(new PointLong(op, me));
            if (pos == null)
            {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
            // else
            JSONObject json = new JSONObject();
            json.put(POS, pos);
            out.println(json.toJSONString());
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
