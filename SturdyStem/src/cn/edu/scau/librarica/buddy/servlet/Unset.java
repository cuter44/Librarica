package cn.edu.scau.librarica.buddy.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.github.cuter44.util.dao.*;
import static com.github.cuter44.util.servlet.HttpUtil.notNull;
import static com.github.cuter44.util.servlet.HttpUtil.getLongParam;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.Constants;
import cn.edu.scau.librarica.buddy.core.*;

/** 清除关注或黑名单
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /buddy/unset.api

   <strong>参数</strong>
   op:long, 必需, 对方的 uid
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   application/json class=buddy.dao.Buddy
   @see J#write

   <strong>例外</strong>
    通用, @see cn.edu.scau.librarica.sys.servlet.ExceptionHandler

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class Unset extends HttpServlet
{
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

        try
        {
            Long me = (Long)notNull(getLongParam(req, ME));
            Long op = (Long)notNull(getLongParam(req, OP));

            HiberDao.begin();

            BuddyMgr.setNull(me, op);

            HiberDao.commit();
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
