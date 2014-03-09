package cn.edu.scau.librarica.buddy.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.buddy.core.*;

/** 清除关注或黑名单
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /buddy/unset

   <strong>参数</strong>
   op:long, 必需, 对方的 uid
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   成功时返回 OK(200), 没有响应正文.

   <strong>例外</strong>
   無し(即使指定的 uid 不正确也会被接受)

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Unset extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String ME = "uid";
    private static final String OP = "op";

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
            if (me == null)
                throw(new MissingParameterException(ME));

            Long op = HttpUtil.getLongParam(req, OP);
            if (op == null)
                throw(new MissingParameterException(OP));

            HiberDao.begin();

            BuddyMgr.setNull(me, op);

            HiberDao.commit();
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
