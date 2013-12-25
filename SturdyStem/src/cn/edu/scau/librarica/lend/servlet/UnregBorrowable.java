package cn.edu.scau.librarica.lend.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import com.github.cuter44.util.dao.*;
import com.github.cuter44.util.servlet.*;

import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.lend.dao.*;
import cn.edu.scau.librarica.lend.core.*;

/** 登记出借
 * 登记自身的藏书为可出借
 * <pre style="font-size:12px">

   <strong>请求</strong>
   POST /lend/unreg

   <strong>参数</strong>
   bid:long, 必需, 准备下架的书id
   <i>鉴权</i>
   uid:long, 必需, uid
   s:hex, 必需, session key

   <strong>响应</strong>
   成功时返回 OK(200), 没有响应正文.

   <strong>例外</strong>
   指定的 bid 不存在返回 Bad Request(400):{"flag":"!notfound"}

   <strong>样例</strong>暂无
 * </pre>
 *
 */
public class UnregBorrowable extends HttpServlet
{
    private static final String FLAG = "flag";
    private static final String UID = "uid";
    private static final String S = "s";
    private static final String BID = "bid";

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
            Long bid = HttpUtil.getLongParam(req, BID);
            if (bid == null)
                throw(new MissingParameterException(BID));

            HiberDao.begin();

            BorrowableBookMgr.remove(bid);

            HiberDao.commit();
        }
        catch (EntityNotFoundException ex)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);

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
